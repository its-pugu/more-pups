package pugu.pups;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class ExplorerGoal extends Goal {
    private static final double MIN_SEPARATION_SQR = 2500.0D;
    private static final float TRIGGER_CHANCE = 0.5F;
    private static final int CHECK_INTERVAL = 100;
    private static final double ARRIVE_SQR = 4.0D;
    private static final double PLAYER_NEAR_SQR = 12.0D;
    private static final double ABANDON_SQR = 400.0D;
    private static final int WAIT_TICKS = 100;
    private static final int DIG_TICKS = 40;

    private static final ResourceKey<LootTable> TREASURE =
            ResourceKey.create(Registries.LOOT_TABLE, MorePups.id("gameplay/dog_treasure"));

    private final PupEntity dog;

    private BlockPos digSpot;
    private BlockPos lastDigSpot;
    private int checkTimer;
    private int waitTimer;
    private int digTimer;

    public ExplorerGoal(PupEntity dog) {
        this.dog = dog;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!this.dog.isTame() || DogStats.classOf(this.dog) != DogSkill.EXPLORER
                || this.dog.getMainHandItem().isEmpty()) {
            return false;
        }

        if (--this.checkTimer > 0) {
            return false;
        }

        this.checkTimer = CHECK_INTERVAL;

        if (this.lastDigSpot != null
                && this.dog.blockPosition().distSqr(this.lastDigSpot) < MIN_SEPARATION_SQR) {
            return false;
        }

        if (this.dog.getRandom().nextFloat() > TRIGGER_CHANCE) {
            return false;
        }

        this.digSpot = this.findSpot();

        return this.digSpot != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.digSpot != null && this.dog.isTame();
    }

    @Override
    public void start() {
        this.waitTimer = WAIT_TICKS;
        this.digTimer = 0;

        this.dog.level().playSound(null, this.dog.blockPosition(),
                barkFor(this.dog), SoundSource.NEUTRAL, 1.0F, 1.6F);

        this.dog.getNavigation().moveTo(
                this.digSpot.getX() + 0.5D, this.digSpot.getY(), this.digSpot.getZ() + 0.5D, 1.2D);
    }

    @Override
    public void stop() {
        this.digSpot = null;
        this.digTimer = 0;
        this.dog.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.digSpot == null) {
            return;
        }

        LivingEntity owner = this.dog.getOwner();

        if (owner != null && owner.distanceToSqr(this.dog) > ABANDON_SQR) {
            this.digSpot = null;
            return;
        }

        if (this.digTimer > 0) {
            this.dig();
            return;
        }

        if (this.dog.blockPosition().distSqr(this.digSpot) > ARRIVE_SQR) {
            if (this.dog.getNavigation().isDone()) {
                this.dog.getNavigation().moveTo(
                        this.digSpot.getX() + 0.5D, this.digSpot.getY(), this.digSpot.getZ() + 0.5D, 1.2D);
            }

            return;
        }

        if (owner != null && owner.blockPosition().distSqr(this.digSpot) <= PLAYER_NEAR_SQR) {
            this.digTimer = DIG_TICKS;
            return;
        }

        if (owner != null) {
            this.dog.getLookControl().setLookAt(owner, 30.0F, 30.0F);
        }

        if (--this.waitTimer <= 0) {
            this.digSpot = null;
        }
    }

    private void dig() {
        if (!(this.dog.level() instanceof ServerLevel level)) {
            return;
        }

        this.dog.getLookControl().setLookAt(
                this.digSpot.getX() + 0.5D, this.digSpot.getY() - 0.5D, this.digSpot.getZ() + 0.5D);

        BlockState below = level.getBlockState(this.digSpot.below());

        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, below),
                this.digSpot.getX() + 0.5D, this.digSpot.getY() + 0.1D, this.digSpot.getZ() + 0.5D,
                6, 0.2D, 0.1D, 0.2D, 0.05D);

        if (this.digTimer % 8 == 0) {
            level.playSound(null, this.digSpot, SoundEvents.COMPOSTER_FILL,
                    SoundSource.NEUTRAL, 0.8F, 1.2F);
        }

        if (--this.digTimer <= 0) {
            this.dropTreasure(level);
            this.lastDigSpot = this.digSpot;
            this.digSpot = null;
        }
    }

    private void dropTreasure(ServerLevel level) {
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(this.digSpot))
                .withParameter(LootContextParams.THIS_ENTITY, this.dog)
                .create(LootContextParamSets.GIFT);

        for (ItemStack stack : level.getServer().reloadableRegistries()
                .getLootTable(TREASURE).getRandomItems(params)) {
            this.dog.spawnAtLocation(level, stack);
        }
        if (this.dog.getOwner() instanceof ServerPlayer owner) {
            ModTriggers.DOG_ACTION.fire(owner, "find_treasure");
        }
    }

    private BlockPos findSpot() {
        Vec3 forward = this.dog.getLookAngle();

        for (int attempt = 0; attempt < 8; attempt++) {
            double distance = 8.0D + this.dog.getRandom().nextInt(10);
            double spread = (this.dog.getRandom().nextDouble() - 0.5D) * 8.0D;

            int x = (int) (this.dog.getX() + forward.x * distance + spread);
            int z = (int) (this.dog.getZ() + forward.z * distance + spread);
            int y = this.dog.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);

            BlockPos candidate = new BlockPos(x, y, z);

            if (this.dog.level().getBlockState(candidate.below()).is(ModTags.DIGGABLE)) {
                return candidate;
            }
        }

        return null;
    }

    private static SoundEvent barkFor(PupEntity dog) {
        return switch (dog.getBreed()) {
            case DACHSHUND -> ModSounds.DACHSHUND_BARK;
            case PUG -> ModSounds.PUG_BARK;
            case LABRADOR -> ModSounds.LABRADOR_BARK;
        };
    }
}