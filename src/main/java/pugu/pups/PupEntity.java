package pugu.pups;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;

public class PupEntity extends Wolf implements GeoEntity {
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation SIT_ANIM = RawAnimation.begin().thenPlayAndHold("sit");
    private static final RawAnimation HEAD_TILT_ANIM = RawAnimation.begin().thenPlayAndHold("head_tilt");
    private static final RawAnimation HEAD_NEUTRAL_ANIM = RawAnimation.begin().thenPlayAndHold("head_neutral");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public PupEntity(EntityType<? extends Wolf> entityType, Level world) {
        super(entityType, world);
    }

    private static final EntityDataAccessor<Boolean> DATA_CARRYING_BALL =
            SynchedEntityData.defineId(PupEntity.class, EntityDataSerializers.BOOLEAN);

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_CARRYING_BALL, false);
    }

    public boolean isCarryingBall() {
        return this.entityData.get(DATA_CARRYING_BALL);
    }

    public void setCarryingBall(boolean carrying) {
        this.entityData.set(DATA_CARRYING_BALL, carrying);
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
        if (animTest.animatable().isInSittingPose()) {
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

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

}