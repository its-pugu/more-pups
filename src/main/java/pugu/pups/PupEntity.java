package pugu.pups;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.RawAnimation;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class PupEntity extends Wolf implements GeoEntity {
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation SIT_ANIM = RawAnimation.begin().thenPlayAndHold("sit");
    private static final RawAnimation HEAD_TILT_ANIM = RawAnimation.begin().thenPlayAndHold("head_tilt");
    private static final RawAnimation HEAD_NEUTRAL_ANIM = RawAnimation.begin().thenPlayAndHold("head_neutral");
    private static final RawAnimation SLEEP_ANIM = RawAnimation.begin().thenPlayAndHold("sleep");

    public static final DataTicket<Boolean> SLEEPING_TICKET =
            DataTicket.create("more_pups_sleeping", Boolean.class);
    public static final DataTicket<DogBreed> BREED_TICKET =
            DataTicket.create("more_pups_breed", DogBreed.class);
    public static final DataTicket<Boolean> TAMED_TICKET =
            DataTicket.create("more_pups_tamed", Boolean.class);
    public static final DataTicket<Integer> COLLAR_TICKET =
            DataTicket.create("more_pups_collar", Integer.class);
    public static final DataTicket<Boolean> BABY_TICKET =
            DataTicket.create("more_pups_baby", Boolean.class);

    private static final EntityDataAccessor<Integer> DATA_BREED =
            SynchedEntityData.defineId(PupEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<ItemStack> DATA_CARRIED_BALL =
            SynchedEntityData.defineId(PupEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Boolean> DATA_SLEEPING =
            SynchedEntityData.defineId(PupEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDimensions DACHSHUND_DIMENSIONS = EntityDimensions.scalable(0.5F, 0.5F);
    private static final EntityDimensions PUG_DIMENSIONS = EntityDimensions.scalable(0.6F, 0.6F);
    private static final EntityDimensions LABRADOR_DIMENSIONS = EntityDimensions.scalable(0.8F, 0.85F);

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        EntityDimensions dimensions = switch (this.getBreed()) {
            case DACHSHUND -> DACHSHUND_DIMENSIONS;
            case PUG -> PUG_DIMENSIONS;
            case LABRADOR -> LABRADOR_DIMENSIONS;
        };

        return this.isBaby() ? dimensions.scale(0.5F) : dimensions;
    }

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public PupEntity(EntityType<? extends Wolf> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_CARRIED_BALL, ItemStack.EMPTY);
        builder.define(DATA_SLEEPING, false);
        builder.define(DATA_BREED, 0);
    }

    public DogBreed getBreed() {
        return DogBreed.values()[this.entityData.get(DATA_BREED)];
    }

    public void setBreed(DogBreed breed) {
        this.entityData.set(DATA_BREED, breed.ordinal());
        this.refreshDimensions();
    }

    public boolean isCarryingBall() {
        return !this.entityData.get(DATA_CARRIED_BALL).isEmpty();
    }

    public ItemStack getCarriedBall() {
        return this.entityData.get(DATA_CARRIED_BALL);
    }

    public void setCarriedBall(ItemStack stack) {
        this.entityData.set(DATA_CARRIED_BALL, stack);
    }

    public boolean isSleeping() {
        return this.entityData.get(DATA_SLEEPING);
    }

    public void setSleeping(boolean sleeping) {
        this.entityData.set(DATA_SLEEPING, sleeping);
    }

    @Override
    public Component getName() {
        if (this.hasCustomName()) {
            return super.getName();
        }

        return Component.translatable("entity." + MorePups.MOD_ID + ".pup." + this.getBreed().getSerializedName());
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (DATA_BREED.equals(key)) {
            this.refreshDimensions();
        }

        super.onSyncedDataUpdated(key);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("breed", DogBreed.CODEC, this.getBreed());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setBreed(input.read("breed", DogBreed.CODEC).orElse(DogBreed.DACHSHUND));
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(4, new ComeToOwnerWithBallGoal(this));
        this.goalSelector.addGoal(3, new FetchBallGoal(this));
        this.goalSelector.addGoal(2, new ReturnBallToOwnerGoal(this));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<PupEntity>("Walking", 5, this::walkAnimController));
        controllers.add(new AnimationController<PupEntity>("HeadTilt", 5, this::headTiltAnimController));
        controllers.add(new AnimationController<PupEntity>("Sitting", 0, this::sitAnimController));
        controllers.add(new AnimationController<PupEntity>("Sleeping", 0, this::sleepAnimController));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return switch (this.getBreed()) {
            case DACHSHUND -> ModSounds.DACHSHUND_BARK;
            case PUG -> ModSounds.PUG_BARK;
            case LABRADOR -> ModSounds.LABRADOR_BARK;
        };
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return switch (this.getBreed()) {
            case DACHSHUND -> ModSounds.DACHSHUND_HURT;
            case PUG -> ModSounds.PUG_HURT;
            case LABRADOR -> ModSounds.LABRADOR_HURT;
        };
    }

    @Override
    protected SoundEvent getDeathSound() {
        return switch (this.getBreed()) {
            case DACHSHUND -> ModSounds.DACHSHUND_DEATH;
            case PUG -> ModSounds.PUG_DEATH;
            case LABRADOR -> ModSounds.LABRADOR_DEATH;
        };
    }

    @Override
    public int getAmbientSoundInterval() {
        return 400;
    }

    @Override
    public @Nullable PupEntity getBreedOffspring(ServerLevel level, AgeableMob partner) {
        PupEntity baby = ModEntityTypes.PUP.create(level, EntitySpawnReason.BREEDING);

        if (baby == null) {
            return null;
        }

        if (partner instanceof PupEntity partnerPup) {
            baby.setBreed(this.random.nextBoolean() ? this.getBreed() : partnerPup.getBreed());
        } else {
            baby.setBreed(this.getBreed());
        }

        if (this.isTame()) {
            baby.setOwnerReference(this.getOwnerReference());
            baby.setTame(true, true);
        }

        return baby;
    }

    private <E extends PupEntity> PlayState walkAnimController(AnimationTest<E> animTest) {
        double horizontalSpeedSq = animTest.animatable().getDeltaMovement().horizontalDistanceSqr();

        if (horizontalSpeedSq > 0.0025) {
            return animTest.setAndContinue(WALK_ANIM);
        }

        animTest.controller().reset();

        return PlayState.STOP;
    }

    private <E extends PupEntity> PlayState sitAnimController(AnimationTest<E> animTest) {
        if (animTest.animatable().isInSittingPose() && !animTest.animatable().isSleeping()) {
            return animTest.setAndContinue(SIT_ANIM);
        }

        animTest.controller().reset();

        return PlayState.STOP;
    }

    private <E extends PupEntity> PlayState headTiltAnimController(AnimationTest<E> animTest) {
        Player nearestPlayer = animTest.animatable().level().getNearestPlayer(animTest.animatable(), 6.0);

        if (nearestPlayer != null && isFood(nearestPlayer.getMainHandItem())) {
            return animTest.setAndContinue(HEAD_TILT_ANIM);
        }

        return animTest.setAndContinue(HEAD_NEUTRAL_ANIM);
    }

    private <E extends PupEntity> PlayState sleepAnimController(AnimationTest<E> animTest) {
        if (animTest.animatable().isSleeping()) {
            return animTest.setAndContinue(SLEEP_ANIM);
        }

        animTest.controller().reset();

        return PlayState.STOP;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}