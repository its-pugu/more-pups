package pugu.pups;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.wolf.Wolf;

import java.util.EnumSet;

public class DogAttackGoal extends Goal {
    private static final double REACH_BONUS = 1.5D;
    private static final int ATTACK_COOLDOWN = 15;

    private final Wolf dog;
    private final double speed;

    private int cooldown;
    private int pathTimer;

    public DogAttackGoal(Wolf dog, double speed) {
        this.dog = dog;
        this.speed = speed;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.dog.getTarget();

        return target != null && target.isAlive() && !this.dog.isInSittingPose();
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void stop() {
        this.dog.getNavigation().stop();
        this.dog.setAggressive(false);
    }

    @Override
    public void start() {
        this.dog.setAggressive(true);
        this.pathTimer = 0;
    }

    @Override
    public void tick() {
        LivingEntity target = this.dog.getTarget();

        if (target == null) {
            return;
        }

        this.dog.getLookControl().setLookAt(target, 30.0F, 30.0F);

        if (--this.pathTimer <= 0) {
            this.pathTimer = 10;
            this.dog.getNavigation().moveTo(target, this.speed);
        }

        if (--this.cooldown > 0) {
            return;
        }

        if (this.dog.distanceToSqr(target) <= this.attackRangeSqr(target)) {
            this.cooldown = ATTACK_COOLDOWN;
            this.dog.swing(net.minecraft.world.InteractionHand.MAIN_HAND);

            if (this.dog.level() instanceof ServerLevel serverLevel) {
                this.dog.doHurtTarget(serverLevel, target);
            }
        }
    }

    private double attackRangeSqr(LivingEntity target) {
        double reach = this.dog.getBbWidth() + target.getBbWidth() + REACH_BONUS;

        return reach * reach;
    }
}