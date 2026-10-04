package pugu.pups;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ModTags {
    public static final TagKey<Block> DIGGABLE =
            TagKey.create(Registries.BLOCK, MorePups.id("diggable"));
}