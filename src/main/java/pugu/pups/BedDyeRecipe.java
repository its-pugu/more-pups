package pugu.pups;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class BedDyeRecipe extends CustomRecipe {
    @Override
    public boolean matches(CraftingInput input, Level level) {

        boolean foundBed = false;
        boolean foundDye = false;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);

            if (stack.isEmpty()) {
                continue;
            }

            if (stack.is(ModBlocks.DOG_BED.asItem())) {
                if (foundBed) {
                    return false;
                }
                foundBed = true;
            } else if (stack.getItem() instanceof DyeItem) {
                if (foundDye) {
                    return false;
                }
                foundDye = true;
            } else {
                return false;
            }
        }

        return foundBed && foundDye;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        DyeColor color = null;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);

            if (stack.getItem() instanceof DyeItem) {
                color = stack.get(DataComponents.DYE);
            }
        }

        if (color == null) {
            return ItemStack.EMPTY;
        }

        ItemStack result = new ItemStack(ModBlocks.DOG_BED.asItem());
        result.set(DataComponents.DYED_COLOR, new DyedItemColor(color.getTextureDiffuseColor()));
        result.set(ModDataComponents.BED_DYE_COLOR, color);

        return result;
    }

    @Override
    public RecipeSerializer<BedDyeRecipe> getSerializer() {
        return ModRecipes.BED_DYE_SERIALIZER;
    }
}