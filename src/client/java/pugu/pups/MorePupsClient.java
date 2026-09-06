package pugu.pups;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class MorePupsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRenderers.register(ModEntityTypes.PUP, PupEntityRenderer::new);
        EntityRenderers.register(ModEntityTypes.BALL, ThrownItemRenderer::new);

        BlockColorRegistry.register(List.of(new BlockTintSource() {
            @Override
            public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
                if (level.getBlockEntity(pos) instanceof DogBedBlockEntity bed) {
                    return ARGB.opaque(bed.getColor().getTextureDiffuseColor());
                }

                return ARGB.opaque(DyeColor.WHITE.getTextureDiffuseColor());
            }

            @Override
            public int color(BlockState state) {
                return ARGB.opaque(DyeColor.WHITE.getTextureDiffuseColor());
            }
        }), ModBlocks.DOG_BED);

        ClientPlayNetworking.registerGlobalReceiver(DogListPayload.TYPE, (payload, context) ->
                Minecraft.getInstance().gui.setScreen(new DogWhistleScreen(payload.dogs())));

        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            if (!level.isClientSide()) {
                return InteractionResult.PASS;
            }

            if (player.isSecondaryUseActive() && entity instanceof Wolf wolf
                    && wolf.isTame() && wolf.isOwnedBy(player)) {
                Minecraft.getInstance().gui.setScreen(new DogStateScreen(wolf));
                return InteractionResult.SUCCESS;
            }

            return InteractionResult.PASS;
        });
    }
}