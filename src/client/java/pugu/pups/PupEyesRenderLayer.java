package pugu.pups;

import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.layer.builtin.TextureLayerGeoLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public class PupEyesRenderLayer extends TextureLayerGeoLayer<PupEntity, Void, LivingEntityRenderState> {
    private static final Identifier CLOSED_EYES =
            Identifier.fromNamespaceAndPath(MorePups.MOD_ID, "textures/entity/dachshund_eyes_closed.png");

    public PupEyesRenderLayer(GeoRenderer<PupEntity, Void, LivingEntityRenderState> renderer) {
        super(renderer, MissingTextureAtlasSprite.getLocation());
    }

    @Override
    protected Identifier getTextureResource(LivingEntityRenderState renderState) {
        return Identifier.fromNamespaceAndPath(MorePups.MOD_ID,
                PupGeoModel.texturePrefix(renderState) + "_eyes_closed.png");
    }

    @Override
    public void addRenderData(PupEntity animatable, Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
        renderState.addGeckolibData(PupEntity.SLEEPING_TICKET, animatable.isSleeping());
    }



    @Override
    protected @Nullable RenderType getRenderType(LivingEntityRenderState renderState) {
        if (!renderState.getOrDefaultGeckolibData(PupEntity.SLEEPING_TICKET, false)) {
            return null;
        }

        return super.getRenderType(renderState);
    }
}