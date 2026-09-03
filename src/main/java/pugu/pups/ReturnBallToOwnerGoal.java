package pugu.pups;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.util.EnumSet;

public class ReturnBallToOwnerGoal extends Goal {
    private final PupEntity dog;

    public ReturnBallToOwnerGoal(PupEntity dog) {
        this.dog = dog;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return this.dog.isCarryingBall() && this.dog.getOwner() != null && !this.dog.isInSittingPose();
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void tick() {
        LivingEntity owner = this.dog.getOwner();

        if (owner == null) {
            return;
        }

        this.dog.getNavigation().moveTo(owner, 1.3);
        this.dog.getLookControl().setLookAt(owner, 10.0F, this.dog.getMaxHeadXRot());

        if (this.dog.distanceToSqr(owner) < 6.25) {
            if (!this.dog.level().isClientSide()) {
                ItemEntity droppedBall = new ItemEntity(this.dog.level(), owner.getX(), owner.getY(), owner.getZ(), new ItemStack(ModItems.BALL));
                this.dog.level().addFreshEntity(droppedBall);
            }

            this.dog.setCarryingBall(false);
        }
    }
}