package pugu.pups;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {

    public static final BlockEntityType<DogBedBlockEntity> DOG_BED = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            MorePups.id("dog_bed"),
            FabricBlockEntityTypeBuilder.create(DogBedBlockEntity::new, ModBlocks.DOG_BED).build());

    public static void initialize() {
    }
}