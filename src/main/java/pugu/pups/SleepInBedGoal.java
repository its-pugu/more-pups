package pugu.pups;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.level.Level;

import java.util.EnumSet;

public class SleepInBedGoal extends Goal {
    private static final int SEARCH_RADIUS = 16;
    private static final double ARRIVE_DISTANCE_SQR = 1.5D;

    private final Wolf wolf;
    private BlockPos bedPos;
    private int retryTimer;

    public SleepInBedGoal(Wolf wolf) {
        this.wolf = wolf;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.wolf.getAttachedOrElse(ModAttachments.DOG_STATE, DogBehaviorState.FOLLOW) != DogBehaviorState.RELAX
                || this.wolf.isInSittingPose()
                || !this.wolf.level().isDarkOutside()) {
            return false;
        }

        if (--this.retryTimer > 0) {
            return false;
        }

        this.retryTimer = 100;
        this.bedPos = this.findBed();
        return this.bedPos != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.bedPos != null
                && this.wolf.getAttachedOrElse(ModAttachments.DOG_STATE, DogBehaviorState.FOLLOW) == DogBehaviorState.RELAX
                && this.wolf.level().isDarkOutside()
                && this.isUsableBed(this.bedPos);
    }

    @Override
    public void start() {
        this.wolf.getNavigation().moveTo(
                this.bedPos.getX() + 0.5D, this.bedPos.getY(), this.bedPos.getZ() + 0.5D, 1.0D);
    }

    @Override
    public void stop() {
        if (this.wolf instanceof PupEntity pup) {
            pup.setSleeping(false);
        }

        this.bedPos = null;
        this.wolf.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.bedPos == null) {
            return;
        }

        if (this.wolf.blockPosition().distSqr(this.bedPos) <= ARRIVE_DISTANCE_SQR) {
            this.claimAndSleep();
        } else if (this.wolf.getNavigation().isDone()) {
            this.wolf.getNavigation().moveTo(
                    this.bedPos.getX() + 0.5D, this.bedPos.getY(), this.bedPos.getZ() + 0.5D, 1.0D);
        }
    }

    private void claimAndSleep() {
        if (this.wolf.level().getBlockEntity(this.bedPos) instanceof DogBedBlockEntity bed) {
            bed.claim(this.wolf.getUUID());
            this.wolf.setAttached(ModAttachments.DOG_BED_POS, this.bedPos);
            this.wolf.getNavigation().stop();

            this.wolf.snapTo(this.bedPos.getX() + 0.5D, this.bedPos.getY() + 0.4D,
                    this.bedPos.getZ() + 0.5D, this.wolf.getYRot(), 0.0F);

            if (this.wolf instanceof PupEntity pup) {
                pup.setSleeping(true);
            } else {
                this.wolf.setInSittingPose(true);
            }
        }
    }

    private BlockPos findBed() {
        BlockPos claimed = this.wolf.getAttachedOrElse(ModAttachments.DOG_BED_POS, null);

        if (claimed != null && this.isUsableBed(claimed)) {
            return claimed;
        }

        Level level = this.wolf.level();
        BlockPos origin = this.wolf.blockPosition();
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;

        for (BlockPos candidate : BlockPos.betweenClosed(
                origin.offset(-SEARCH_RADIUS, -4, -SEARCH_RADIUS),
                origin.offset(SEARCH_RADIUS, 4, SEARCH_RADIUS))) {

            if (level.getBlockEntity(candidate) instanceof DogBedBlockEntity bed
                    && (bed.isUnclaimed() || bed.isClaimedBy(this.wolf.getUUID()))) {

                double distance = origin.distSqr(candidate);

                if (distance < bestDistance) {
                    bestDistance = distance;
                    best = candidate.immutable();
                }
            }
        }

        return best;
    }

    private boolean isUsableBed(BlockPos pos) {
        return this.wolf.level().getBlockEntity(pos) instanceof DogBedBlockEntity bed
                && (bed.isUnclaimed() || bed.isClaimedBy(this.wolf.getUUID()));
    }
}