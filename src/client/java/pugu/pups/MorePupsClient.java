package pugu.pups;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.wolf.Wolf;

public class MorePupsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRenderers.register(ModEntityTypes.DACHSHUND, DachshundEntityRenderer::new);
        EntityRenderers.register(ModEntityTypes.BALL, ThrownItemRenderer::new);

        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            if (!level.isClientSide()) {
                return InteractionResult.PASS;
            }

            if (player.isSecondaryUseActive() && entity instanceof Wolf wolf
                    && wolf.isTame() && wolf.isOwnedBy(player)) {
                Minecraft.getInstance().gui.setScreen(new DogStateScreen(wolf.getId()));
                return InteractionResult.SUCCESS;
            }

            return InteractionResult.PASS;
        });
    }
}