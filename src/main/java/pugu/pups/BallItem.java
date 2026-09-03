package pugu.pups;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BallItem extends Item {
    public BallItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.5F, 1.0F);

        if (!level.isClientSide()) {
            BallEntity ball = new BallEntity(ModEntityTypes.BALL, level);
            ball.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
            ball.setItem(stack.copyWithCount(1));
            ball.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            level.addFreshEntity(ball);
        }

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public Component getName(ItemStack stack) {
        DyeColor color = stack.get(ModDataComponents.BALL_DYE_COLOR);

        if (color == null) {
            return super.getName(stack);
        }

        return Component.translatable("item.more-pups.ball.dyed", Component.translatable("color.minecraft." + color.getName()));
    }
}