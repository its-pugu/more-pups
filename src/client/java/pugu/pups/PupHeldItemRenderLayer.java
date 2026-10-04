package pugu.pups;

import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.layer.builtin.BlockAndItemGeoLayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class PupHeldItemRenderLayer extends BlockAndItemGeoLayer<PupEntity, Void, LivingEntityRenderState> {
    public PupHeldItemRenderLayer(EntityRendererProvider.Context context,
                                  GeoRenderer<PupEntity, Void, LivingEntityRenderState> renderer) {
        super(context, renderer);
    }

    @Override
    protected List<RenderData> getRelevantBones(PupEntity animatable, Void relatedObject,
                                                LivingEntityRenderState renderState, float partialTick) {
        boolean carryingBall = animatable.isCarryingBall();

        ItemStack held = carryingBall ? animatable.getCarriedBall() : animatable.getMainHandItem();

        if (held.isEmpty()) {
            return List.of();
        }

        boolean useMouthBone = carryingBall || held.is(Items.MAP) || held.is(Items.FILLED_MAP);

        ItemStackRenderState stackState = new ItemStackRenderState();
        this.itemModelResolver.updateForNonLiving(stackState, held, ItemDisplayContext.GROUND, animatable);

        return List.of(RenderData.item(useMouthBone ? "mouth" : "mouth2",
                ItemDisplayContext.GROUND, stackState));
    }

    @Override
    public void addRenderData(PupEntity animatable, Void relatedObject,
                              LivingEntityRenderState renderState, float partialTick) {
        renderState.addGeckolibData(CONTENTS,
                getRelevantBones(animatable, relatedObject, renderState, partialTick));
    }
}