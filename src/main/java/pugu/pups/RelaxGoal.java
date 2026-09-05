package pugu.pups;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.wolf.Wolf;

import java.util.EnumSet;

public class RelaxGoal extends Goal {
    private static final int WILD_RADIUS = 6;

    private final Wolf wolf;
    private int idleTimer;

    public RelaxGoal(Wolf wolf) {
        this.wolf = wolf;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.wolf.isInSittingPose()) {
            return false;
        }

        if (!this.wolf.isTame()) {
            return true;
        }

        return this.wolf.getAttachedOrElse(ModAttachments.DOG_STATE, DogBehaviorState.FOLLOW) == DogBehaviorState.RELAX
                && this.wolf.hasHome();
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
        if (--this.idleTimer > 0 || !this.wolf.getNavigation().isDone()) {
            return;
        }

        this.idleTimer = 10 + this.wolf.getRandom().nextInt(60);

        BlockPos origin = this.wolf.hasHome() ? this.wolf.getHomePosition() : this.wolf.blockPosition();
        int radius = this.wolf.hasHome() ? this.wolf.getHomeRadius() : WILD_RADIUS;

        double targetX = origin.getX() + 0.5D + this.wolf.getRandom().nextInt(radius * 2 + 1) - radius;
        double targetZ = origin.getZ() + 0.5D + this.wolf.getRandom().nextInt(radius * 2 + 1) - radius;

        double speed = this.wolf.getRandom().nextBoolean() ? 0.8D : 1.0D;

        this.wolf.getNavigation().moveTo(targetX, origin.getY(), targetZ, speed);
    }
}