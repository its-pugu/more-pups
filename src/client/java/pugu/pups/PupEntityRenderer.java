package pugu.pups;

import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.RenderPassInfo;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;

public class PupEntityRenderer extends GeoEntityRenderer<PupEntity, LivingEntityRenderState> {
    public PupEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new PupGeoModel());
        withRenderLayer(new PupHeldItemRenderLayer(context, this));
        withRenderLayer(new PupEyesRenderLayer(this));
        withRenderLayer(new PupCollarRenderLayer(this));
    }

    @Override
    public float getMotionAnimThreshold(PupEntity animatable) {
        return 0.015f;
    }

    @Override
    public void addRenderData(PupEntity animatable, Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
        super.addRenderData(animatable, relatedObject, renderState, partialTick);
        renderState.addGeckolibData(PupEntity.BREED_TICKET, animatable.getBreed());
        renderState.addGeckolibData(PupEntity.BABY_TICKET, animatable.isBaby());
        renderState.addGeckolibData(PupEntity.PET_TICKET,
                PetAnimations.progress(animatable.getId(), animatable.level().getGameTime(), partialTick));
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
    @Override
    public void scaleModelForRender(RenderPassInfo<LivingEntityRenderState> renderPassInfo,
                                    float widthScale, float heightScale) {
        float progress = renderPassInfo.renderState()
                .getOrDefaultGeckolibData(PupEntity.PET_TICKET, 0.0F);

        if (progress > 0.0F) {
            float squash = (float) Math.sin(progress * Math.PI) * 0.15F;

            widthScale *= 1.0F + squash;
            heightScale *= 1.0F - squash;
        }

        super.scaleModelForRender(renderPassInfo, widthScale, heightScale);
    }


}