package pugu.pups;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.Block;

import java.util.EnumSet;
import java.util.Set;

public class MinerGoal extends Goal {
    private static final int RADIUS = 5;
    private static final int REPEAT_AFTER = 4;

    private final PupEntity dog;

    private int cooldown;
    private int staleCount;
    private BlockPos lastBarkedAt;

    public MinerGoal(PupEntity dog) {
        this.dog = dog;
    }

    @Override
    public boolean canUse() {
        return this.dog.isTame()
                && DogStats.classOf(this.dog) == DogSkill.MINER
                && !this.dog.getMainHandItem().isEmpty();
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void tick() {
        if (--this.cooldown > 0) {
            return;
        }

        Set<Block> wanted = MinerOres.detectableWith(this.dog.getMainHandItem());

        if (wanted.isEmpty()) {
            this.cooldown = 100;
            return;
        }

        BlockPos origin = this.dog.blockPosition();
        BlockPos found = null;
        double bestDistance = Double.MAX_VALUE;

        for (BlockPos candidate : BlockPos.betweenClosed(
                origin.offset(-RADIUS, -RADIUS, -RADIUS),
                origin.offset(RADIUS, RADIUS, RADIUS))) {

            if (wanted.contains(this.dog.level().getBlockState(candidate).getBlock())) {
                double distance = origin.distSqr(candidate);

                if (distance < bestDistance) {
                    bestDistance = distance;
                    found = candidate.immutable();
                }
            }
        }

        this.cooldown = 100 + this.dog.getRandom().nextInt(200);

        if (found == null) {
            return;
        }

        if (found.equals(this.lastBarkedAt)) {
            this.staleCount++;

            if (this.staleCount < REPEAT_AFTER) {
                return;
            }
        }

        this.staleCount = 0;
        this.lastBarkedAt = found;

        this.dog.getLookControl().setLookAt(
                found.getX() + 0.5D, found.getY() + 0.5D, found.getZ() + 0.5D);

        this.dog.level().playSound(null, this.dog.blockPosition(),
                barkFor(this.dog), SoundSource.NEUTRAL, 1.0F, 1.5F);

        if (this.dog.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    this.dog.getX(), this.dog.getY() + this.dog.getBbHeight(), this.dog.getZ(),
                    5, 0.3D, 0.2D, 0.3D, 0.0D);
        }
    }

    private static SoundEvent barkFor(PupEntity dog) {
        return switch (dog.getBreed()) {
            case DACHSHUND -> ModSounds.DACHSHUND_BARK;
            case PUG -> ModSounds.PUG_BARK;
            case LABRADOR -> ModSounds.LABRADOR_BARK;
        };
    }
}