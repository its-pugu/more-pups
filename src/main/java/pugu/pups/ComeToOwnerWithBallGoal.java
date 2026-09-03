package pugu.pups;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class ComeToOwnerWithBallGoal extends Goal {
    private final PupEntity dog;
    private LivingEntity owner;

    public ComeToOwnerWithBallGoal(PupEntity dog) {
        this.dog = dog;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!this.dog.isTame() || this.dog.isInSittingPose()) {
            return false;
        }

        LivingEntity possibleOwner = this.dog.getOwner();

        if (possibleOwner == null || !possibleOwner.getMainHandItem().is(ModItems.BALL)) {
            return false;
        }

        this.owner = possibleOwner;
        return this.dog.distanceToSqr(this.owner) > 9.0;
    }

    @Override
    public boolean canContinueToUse() {
        return this.owner != null
                && this.owner.getMainHandItem().is(ModItems.BALL)
                && !this.dog.isInSittingPose()
                && this.dog.distanceToSqr(this.owner) > 4.0;
    }

    private int recalculatePathCooldown = 0;

    @Override
    public void tick() {
        this.dog.getLookControl().setLookAt(this.owner, 10.0F, this.dog.getMaxHeadXRot());

        if (this.recalculatePathCooldown <= 0) {
            this.dog.getNavigation().moveTo(this.owner, 1.2);
            this.recalculatePathCooldown = 10;
        } else {
            this.recalculatePathCooldown--;
        }
    }

    @Override
    public void stop() {
        this.owner = null;
    }
}