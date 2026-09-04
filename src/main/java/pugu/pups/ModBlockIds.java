package pugu.pups;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;

public class ModBlockIds {
    public static ResourceKey<Block> create(String name) {
        return ResourceKey.create(Registries.BLOCK, MorePups.id(name));
    }
}