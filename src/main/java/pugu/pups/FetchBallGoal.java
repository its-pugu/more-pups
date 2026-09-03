package pugu.pups;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;

public class FetchBallGoal extends Goal {
    private final PupEntity dog;
    private Entity targetBall;

    public FetchBallGoal(PupEntity dog) {
        this.dog = dog;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!this.dog.isTame() || this.dog.isInSittingPose() || this.dog.isCarryingBall()) {
            return false;
        }

        AABB searchArea = this.dog.getBoundingBox().inflate(64.0);

        List<BallEntity> flyingBalls = this.dog.level().getEntitiesOfClass(BallEntity.class, searchArea);

        if (!flyingBalls.isEmpty()) {
            this.targetBall = flyingBalls.get(0);
            return true;
        }

        List<ItemEntity> droppedBalls = this.dog.level().getEntitiesOfClass(ItemEntity.class, searchArea,
                item -> item.getItem().is(ModItems.BALL));

        if (!droppedBalls.isEmpty()) {
            this.targetBall = droppedBalls.get(0);
            return true;
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.targetBall != null && this.targetBall.isAlive()
                && !this.dog.isCarryingBall() && !this.dog.isInSittingPose();
    }

    @Override
    public void tick() {
        this.dog.getNavigation().moveTo(this.targetBall, 1.3);
        this.dog.getLookControl().setLookAt(this.targetBall, 10.0F, this.dog.getMaxHeadXRot());

        if (this.dog.distanceToSqr(this.targetBall) < 2.25) {
            ItemStack carried;

            if (this.targetBall instanceof BallEntity ball) {
                carried = ball.getItem().copy();
            } else if (this.targetBall instanceof ItemEntity itemEntity) {
                carried = itemEntity.getItem().copy();
            } else {
                carried = new ItemStack(ModItems.BALL);
            }

            this.targetBall.discard();
            this.dog.setCarriedBall(carried);
        }
    }

    @Override
    public void stop() {
        this.targetBall = null;
    }
}