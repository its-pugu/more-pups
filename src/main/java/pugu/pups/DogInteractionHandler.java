package pugu.pups;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.item.ItemStack;

public class DogInteractionHandler {
    public static void initialize() {
        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            if (!(entity instanceof Wolf wolf)) {
                return InteractionResult.PASS;
            }

            if (level.isClientSide()) {
                return InteractionResult.PASS;
            }

            if (!wolf.isTame() || !wolf.isOwnedBy(player)) {
                return InteractionResult.PASS;
            }

            long now = level.getGameTime();
            long lastPet = wolf.getAttachedOrElse(ModAttachments.LAST_PET, 0L);

            if (now - lastPet < 3) {
                return InteractionResult.SUCCESS;
            }

            ItemStack held = player.getItemInHand(hand);

            if (wolf instanceof PupEntity pup && pup.getMainHandItem().isEmpty()) {
                DogSkill dogClass = DogStats.classOf(pup);

                if (dogClass != null && dogClass.accepts(held)) {
                    ItemStack single = held.copyWithCount(1);

                    pup.setItemSlot(EquipmentSlot.MAINHAND, single);
                    pup.setGuaranteedDrop(EquipmentSlot.MAINHAND);
                    held.shrink(1);

                    return InteractionResult.SUCCESS;
                }
            }

            if (held.is(ModItems.DOG_TREAT)) {
                DogStats.feed(wolf, 40);
            }

            if (!player.isSecondaryUseActive()) {
                return InteractionResult.PASS;
            }

            return InteractionResult.SUCCESS;
        });
    }
}