package pugu.pups;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class ModRecipes {
    public static final RecipeSerializer<BallDyeRecipe> BALL_DYE_SERIALIZER = new RecipeSerializer<>(
            MapCodec.unit(BallDyeRecipe::new),
            StreamCodec.unit(new BallDyeRecipe())
    );

    public static final RecipeSerializer<BedDyeRecipe> BED_DYE_SERIALIZER = new RecipeSerializer<>(
            MapCodec.unit(BedDyeRecipe::new),
            StreamCodec.unit(new BedDyeRecipe())
    );

    public static void initialize() {
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, MorePups.id("ball_dye"), BALL_DYE_SERIALIZER);
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, MorePups.id("bed_dye"), BED_DYE_SERIALIZER);
    }
}