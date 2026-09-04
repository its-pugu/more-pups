package pugu.pups;

import java.util.function.Function;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class ModBlocks {

    public static final ResourceKey<Block> DOG_BED_ID = ModBlockIds.create("dog_bed");
    public static final Block DOG_BED = register(DOG_BED_ID, DogBedBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOL)
                    .strength(2.0F)
                    .sound(SoundType.WOOL)
                    .noOcclusion());

    public static Block register(ResourceKey<Block> blockKey, Function<BlockBehaviour.Properties, Block> blockFactory,
                                 BlockBehaviour.Properties settings) {
        Block block = blockFactory.apply(settings.setId(blockKey));
        Registry.register(BuiltInRegistries.BLOCK, blockKey, block);

        ResourceKey<Item> itemKey = ModItemIds.create(blockKey.identifier().getPath());
        Item blockItem = new BlockItem(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix());
        Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem);

        return block;
    }

    public static void initialize() {
    }
}