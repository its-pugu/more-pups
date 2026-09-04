package pugu.pups;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.wolf.Wolf;

import java.util.EnumSet;

public class FollowCloselyGoal extends Goal {
    private final Wolf wolf;
    private LivingEntity owner;
    private int pathTimer;

    public FollowCloselyGoal(Wolf wolf) {
        this.wolf = wolf;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.wolf.getAttachedOrElse(ModAttachments.DOG_STATE, DogBehaviorState.FOLLOW) != DogBehaviorState.FOLLOW) {
            return false;
        }

        LivingEntity owner = this.wolf.getOwner();

        if (owner == null || owner.isSpectator() || this.wolf.isInSittingPose()) {
            return false;
        }

        this.owner = owner;
        return this.wolf.distanceToSqr(owner) > this.startDistanceSqr();
    }

    @Override
    public boolean canContinueToUse() {
        return this.owner != null
                && !this.wolf.isInSittingPose()
                && this.wolf.getAttachedOrElse(ModAttachments.DOG_STATE, DogBehaviorState.FOLLOW) == DogBehaviorState.FOLLOW
                && this.wolf.distanceToSqr(this.owner) > this.stopDistanceSqr();
    }

    private double startDistanceSqr() {
        int distance = this.wolf.getAttachedOrElse(ModAttachments.FOLLOW_DISTANCE, 3);
        return distance * distance;
    }

    private double stopDistanceSqr() {
        int distance = Math.max(this.wolf.getAttachedOrElse(ModAttachments.FOLLOW_DISTANCE, 3) - 1, 1);
        return distance * distance;
    }

    @Override
    public void start() {
        this.pathTimer = 0;
    }

    @Override
    public void stop() {
        this.owner = null;
        this.wolf.getNavigation().stop();
    }

    @Override
    public void tick() {
        this.wolf.getLookControl().setLookAt(this.owner, 10.0F, this.wolf.getMaxHeadXRot());

        if (--this.pathTimer <= 0) {
            this.pathTimer = 10;
            this.wolf.getNavigation().moveTo(this.owner, 1.0D);
        }
    }
}