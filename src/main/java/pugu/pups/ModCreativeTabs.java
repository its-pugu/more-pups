package pugu.pups;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModCreativeTabs {

    public static final ResourceKey<CreativeModeTab> MORE_PUPS_TAB_KEY = ResourceKey.create(
            BuiltInRegistries.CREATIVE_MODE_TAB.key(), MorePups.id("more_pups_tab"));

    public static final CreativeModeTab MORE_PUPS_TAB = FabricCreativeModeTab.builder()
            .icon(() -> new ItemStack(ModItems.DOG_TREAT))
            .title(Component.translatable("creativeTab.more-pups"))
            .displayItems((params, output) -> {
                output.accept(ModItems.DOG_TREAT);
                output.accept(ModItems.BALL);
            })
            .build();

    public static void initialize() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, MORE_PUPS_TAB_KEY, MORE_PUPS_TAB);
    }
}