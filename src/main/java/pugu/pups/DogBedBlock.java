package pugu.pups;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static net.minecraft.util.ARGB.color;

public class DogBedBlock extends Block implements EntityBlock {
    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D);

    public DogBedBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DogBedBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        DyeColor color = stack.get(ModDataComponents.BED_DYE_COLOR);

        if (color != null && level.getBlockEntity(pos) instanceof DogBedBlockEntity bed) {
            bed.setColor(color);
        }
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);

        if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof DogBedBlockEntity bed
                && bed.getColor() != DyeColor.WHITE) {

            for (ItemStack drop : drops) {
                drop.set(ModDataComponents.BED_DYE_COLOR, bed.getColor());
                drop.set(DataComponents.DYED_COLOR, new DyedItemColor(bed.getColor().getTextureDiffuseColor()));
            }
        }

        return drops;
    }
    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);

        for (Wolf wolf : level.getEntitiesOfClass(Wolf.class, new AABB(pos).inflate(32.0D))) {
            BlockPos claimed = wolf.getAttachedOrElse(ModAttachments.DOG_BED_POS, null);

            if (pos.equals(claimed)) {
                wolf.removeAttached(ModAttachments.DOG_BED_POS);

                if (wolf instanceof PupEntity pup) {
                    pup.setSleeping(false);
                }
            }
        }
    }
}