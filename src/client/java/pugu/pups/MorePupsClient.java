package pugu.pups;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.client.KeyMapping;
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
import org.lwjgl.glfw.GLFW;

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

        KeyMapping.Category category = KeyMapping.Category.register(MorePups.id("general"));


        KeyMapping petKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.more-pups.pet_dog",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_P,
                category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (petKey.consumeClick()) {
                if (client.crosshairPickEntity instanceof Wolf wolf
                        && wolf.isTame() && wolf.isOwnedBy(client.player)) {
                    ClientPlayNetworking.send(new PetDogPayload(wolf.getId()));
                }
            }
        });

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