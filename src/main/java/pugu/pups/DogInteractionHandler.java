package pugu.pups;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.wolf.Wolf;

public class DogInteractionHandler {
    public static void initialize() {
        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            if (!(entity instanceof Wolf wolf)) {
                return InteractionResult.PASS;
            }

            if (!level.isClientSide() && wolf.isTame() && wolf.isOwnedBy(player)
                    && player.getItemInHand(hand).is(ModItems.DOG_TREAT)) {
                DogStats.feed(wolf, 40);
            }

            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "Food: " + wolf.getAttachedOrElse(ModAttachments.FOOD, 100)));

            if (!player.isSecondaryUseActive()) {
                return InteractionResult.PASS;
            }


            if (!player.isSecondaryUseActive()) {
                return InteractionResult.PASS;
            }

            if (!wolf.isTame() || !wolf.isOwnedBy(player)) {
                return InteractionResult.PASS;
            }

            if (level.isClientSide()) {
                return InteractionResult.PASS;
            }

            return InteractionResult.SUCCESS;
        });
    }
}