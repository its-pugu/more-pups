package pugu.pups;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.wolf.Wolf;

import java.util.EnumSet;

public class ReturnToBedGoal extends Goal {
    private static final double TELEPORT_DISTANCE_SQR = 1024.0D;

    private final Wolf wolf;
    private BlockPos bedPos;

    public ReturnToBedGoal(Wolf wolf) {
        this.wolf = wolf;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.wolf.getAttachedOrElse(ModAttachments.DOG_STATE, DogBehaviorState.FOLLOW) != DogBehaviorState.RETURN_TO_BED
                || this.wolf.isInSittingPose()) {
            return false;
        }

        this.bedPos = this.wolf.getAttached(ModAttachments.DOG_BED_POS);

        return this.bedPos != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void start() {
        if (this.wolf.blockPosition().distSqr(this.bedPos) > TELEPORT_DISTANCE_SQR) {
            this.wolf.snapTo(this.bedPos.getX() + 0.5D, this.bedPos.getY() + 1.0D,
                    this.bedPos.getZ() + 0.5D, this.wolf.getYRot(), 0.0F);
        }

        this.wolf.setHomeTo(this.bedPos, this.wolf.getAttachedOrElse(ModAttachments.RELAX_RADIUS, 16));
    }

    @Override
    public void stop() {
        this.bedPos = null;
        this.wolf.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.bedPos == null) {
            return;
        }

        if (this.wolf.blockPosition().distSqr(this.bedPos) > (double) this.wolf.getHomeRadius() * this.wolf.getHomeRadius()) {
            if (this.wolf.getNavigation().isDone()) {
                this.wolf.getNavigation().moveTo(this.bedPos.getX() + 0.5D, this.bedPos.getY(),
                        this.bedPos.getZ() + 0.5D, 1.0D);
            }

            return;
        }

        this.wolf.setAttached(ModAttachments.DOG_STATE, DogBehaviorState.RELAX);
    }
}