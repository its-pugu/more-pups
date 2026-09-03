package pugu.pups;

import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.layer.builtin.BlockAndItemGeoLayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;

import java.util.List;

public class DachshundBallRenderLayer extends BlockAndItemGeoLayer<PupEntity, Void, LivingEntityRenderState> {
    public DachshundBallRenderLayer(EntityRendererProvider.Context context, GeoRenderer<PupEntity, Void, LivingEntityRenderState> renderer) {
        super(context, renderer);
    }

    @Override
    protected List<RenderData> getRelevantBones(PupEntity animatable, Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
        if (!animatable.isCarryingBall()) {
            return List.of();
        }

        ItemStackRenderState stackState = new ItemStackRenderState();
        this.itemModelResolver.updateForNonLiving(stackState, animatable.getCarriedBall(), ItemDisplayContext.GROUND, animatable);

        return List.of(RenderData.item("mouth", ItemDisplayContext.GROUND, stackState));
    }

    @Override
    public void addRenderData(PupEntity animatable, Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
        renderState.addGeckolibData(CONTENTS, getRelevantBones(animatable, relatedObject, renderState, partialTick));
    }
}