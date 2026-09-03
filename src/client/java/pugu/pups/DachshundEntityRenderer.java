package pugu.pups;

import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.RenderPassInfo;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;

public class DachshundEntityRenderer extends GeoEntityRenderer<PupEntity, LivingEntityRenderState> {
    public DachshundEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new DachshundGeoModel());
        withRenderLayer(new DachshundBallRenderLayer(context, this));
    }

    @Override
    public float getMotionAnimThreshold(PupEntity animatable) {
        return 0.015f;
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<LivingEntityRenderState> renderPassInfo, BoneSnapshots snapshots) {
        super.adjustModelBonesForRender(renderPassInfo, snapshots);

        LivingEntityRenderState renderState = renderPassInfo.renderState();

        snapshots.ifPresent("head", boneSnapshot -> {
            boneSnapshot.setRotY(-renderState.yRot * Mth.DEG_TO_RAD);
            boneSnapshot.setRotX(-renderState.xRot * Mth.DEG_TO_RAD);
        });
    }
}