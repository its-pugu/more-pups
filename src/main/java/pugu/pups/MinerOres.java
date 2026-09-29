package pugu.pups;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Set;

public class MinerOres {
    private static final Set<Block> COAL = Set.of(Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE);
    private static final Set<Block> IRON_COPPER = Set.of(
            Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE,
            Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE);
    private static final Set<Block> GOLD = Set.of(
            Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE, Blocks.NETHER_GOLD_ORE);
    private static final Set<Block> DIAMOND = Set.of(
            Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE);
    private static final Set<Block> LAPIS_REDSTONE = Set.of(
            Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE,
            Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE);
    private static final Set<Block> EMERALD_DEBRIS = Set.of(
            Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE, Blocks.ANCIENT_DEBRIS);

    public static Set<Block> detectableWith(ItemStack pickaxe) {
        if (pickaxe.is(Items.WOODEN_PICKAXE)) {
            return COAL;
        }

        if (pickaxe.is(Items.STONE_PICKAXE)) {
            return IRON_COPPER;
        }

        if (pickaxe.is(Items.COPPER_PICKAXE)) {
            return GOLD;
        }

        if (pickaxe.is(Items.IRON_PICKAXE)) {
            return DIAMOND;
        }

        if (pickaxe.is(Items.GOLDEN_PICKAXE)) {
            return LAPIS_REDSTONE;
        }

        if (pickaxe.is(Items.DIAMOND_PICKAXE) || pickaxe.is(Items.NETHERITE_PICKAXE)) {
            return EMERALD_DEBRIS;
        }

        return Set.of();
    }
}