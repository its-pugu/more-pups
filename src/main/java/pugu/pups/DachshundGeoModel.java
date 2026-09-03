package pugu.pups;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class DachshundGeoModel extends GeoModel<PupEntity> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(MorePups.MOD_ID, "entity/dachshund");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(MorePups.MOD_ID, "textures/entity/dachshund.png");
    }

    @Override
    public Identifier getAnimationResource(PupEntity animatable) {
        return Identifier.fromNamespaceAndPath(MorePups.MOD_ID, "entity/dachshund");
    }
}