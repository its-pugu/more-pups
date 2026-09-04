package pugu.pups;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class GuardGoal extends Goal {
    private static final double POST_TOLERANCE_SQR = 4.0D;

    private final Wolf wolf;
    private int scanTimer;

    public GuardGoal(Wolf wolf) {
        this.wolf = wolf;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return this.wolf.getAttachedOrElse(ModAttachments.DOG_STATE, DogBehaviorState.FOLLOW) == DogBehaviorState.GUARD
                && !this.wolf.isInSittingPose();
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void stop() {
        this.wolf.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (--this.scanTimer > 0) {
            return;
        }

        this.scanTimer = 20;

        if (this.wolf.getTarget() == null) {
            this.findTarget();
        }

        if (this.wolf.getTarget() == null && this.wolf.hasHome()
                && this.wolf.blockPosition().distSqr(this.wolf.getHomePosition()) > POST_TOLERANCE_SQR) {
            this.wolf.getNavigation().moveTo(
                    this.wolf.getHomePosition().getX() + 0.5D,
                    this.wolf.getHomePosition().getY(),
                    this.wolf.getHomePosition().getZ() + 0.5D,
                    1.0D);
        }
    }

    private void findTarget() {
        double scanRadius = this.wolf.getAttachedOrElse(ModAttachments.GUARD_RADIUS, 8);
        AABB scanBox = this.wolf.getBoundingBox().inflate(scanRadius, 4.0D, scanRadius);

        List<LivingEntity> candidates = this.wolf.level().getEntitiesOfClass(
                LivingEntity.class, scanBox,
                candidate -> candidate instanceof Enemy && candidate.isAlive() && this.wolf.hasLineOfSight(candidate));

        candidates.stream()
                .min(Comparator.comparingDouble(this.wolf::distanceToSqr))
                .ifPresent(this.wolf::setTarget);
    }
}