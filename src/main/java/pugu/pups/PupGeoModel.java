package pugu.pups;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class PupGeoModel extends GeoModel<PupEntity> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(MorePups.MOD_ID,
                "entity/" + breedName(renderState) + suffix(renderState));
    }

    @Override
    public Identifier getAnimationResource(PupEntity animatable) {
        return Identifier.fromNamespaceAndPath(MorePups.MOD_ID,
                "entity/" + animatable.getBreed().getSerializedName()
                        + (animatable.isBaby() ? "_baby" : ""));
    }

    public static String suffix(GeoRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(PupEntity.BABY_TICKET, false) ? "_baby" : "";
    }

    public static String texturePrefix(GeoRenderState renderState) {
        return "textures/entity/" + breedName(renderState) + suffix(renderState);
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(MorePups.MOD_ID,
                "textures/entity/" + breedName(renderState) + suffix(renderState) + ".png");
    }

    public static String breedName(GeoRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(PupEntity.BREED_TICKET, DogBreed.DACHSHUND)
                .getSerializedName();
    }
}