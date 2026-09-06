package pugu.pups;

import com.geckolib.constant.DataTickets;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.builtin.TextureLayerGeoLayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.jspecify.annotations.Nullable;

public class PupCollarRenderLayer extends TextureLayerGeoLayer<PupEntity, Void, LivingEntityRenderState> {
    public PupCollarRenderLayer(GeoRenderer<PupEntity, Void, LivingEntityRenderState> renderer) {
        super(renderer, MissingTextureAtlasSprite.getLocation());
    }

    @Override
    public void addRenderData(PupEntity animatable, Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
        renderState.addGeckolibData(PupEntity.TAMED_TICKET, animatable.isTame());
        renderState.addGeckolibData(PupEntity.COLLAR_TICKET,
                ARGB.opaque(animatable.getCollarColor().getTextureDiffuseColor()));
    }

    @Override
    protected Identifier getTextureResource(LivingEntityRenderState renderState) {
        return Identifier.fromNamespaceAndPath(MorePups.MOD_ID,
                PupGeoModel.texturePrefix(renderState) + "_collar.png");
    }

    @Override
    protected @Nullable RenderType getRenderType(LivingEntityRenderState renderState) {
        if (!renderState.getOrDefaultGeckolibData(PupEntity.TAMED_TICKET, false)) {
            return null;
        }

        return super.getRenderType(renderState);
    }

    @Override
    public void submitRenderTask(RenderPassInfo<LivingEntityRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
        final int previousColor = renderPassInfo.renderColor();

        renderPassInfo.renderState().addGeckolibData(DataTickets.RENDER_COLOR,
                renderPassInfo.getOrDefaultGeckolibData(PupEntity.COLLAR_TICKET, 0xFFFFFFFF));
        super.submitRenderTask(renderPassInfo, renderTasks);
        renderPassInfo.renderState().addGeckolibData(DataTickets.RENDER_COLOR, previousColor);
    }
}