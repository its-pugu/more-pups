package pugu.pups;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.wolf.Wolf;

import java.util.EnumSet;

public class RelaxGoal extends Goal {
    private final Wolf wolf;
    private int idleTimer;

    public RelaxGoal(Wolf wolf) {
        this.wolf = wolf;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return this.wolf.getAttachedOrElse(ModAttachments.DOG_STATE, DogBehaviorState.FOLLOW) == DogBehaviorState.RELAX
                && !this.wolf.isInSittingPose()
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

        this.idleTimer = 40 + this.wolf.getRandom().nextInt(80);

        BlockPos home = this.wolf.getHomePosition();
        int radius = this.wolf.getHomeRadius();

        double targetX = home.getX() + 0.5D + this.wolf.getRandom().nextInt(radius * 2 + 1) - radius;
        double targetZ = home.getZ() + 0.5D + this.wolf.getRandom().nextInt(radius * 2 + 1) - radius;

        this.wolf.getNavigation().moveTo(targetX, home.getY(), targetZ, 1.0D);
    }
}