package pugu.pups;

import com.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class DachshundEntityRenderer extends GeoEntityRenderer<PupEntity, LivingEntityRenderState> {
    public DachshundEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new DachshundGeoModel());
    }

}