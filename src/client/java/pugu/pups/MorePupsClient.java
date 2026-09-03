package pugu.pups;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

public class MorePupsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRenderers.register(ModEntityTypes.DACHSHUND, DachshundEntityRenderer::new);
        EntityRenderers.register(ModEntityTypes.BALL, ThrownItemRenderer::new);
    }
}