package pugu.pups;

import java.util.function.Function;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

public class ModItems {

    public static final ResourceKey<Item> DOG_TREAT_ID = ModItemIds.create("dog_treat");
    public static final Item DOG_TREAT = register(DOG_TREAT_ID, Item::new, new Item.Properties());
    public static final ResourceKey<Item> BALL_ID = ModItemIds.create("ball");
    public static final Item BALL = register(BALL_ID, BallItem::new, new Item.Properties().stacksTo(16));

    public static Item register(ResourceKey<Item> itemKey, Function<Item.Properties, Item> itemFactory, Item.Properties settings) {
        Item item = itemFactory.apply(settings.setId(itemKey));
        Registry.register(BuiltInRegistries.ITEM, itemKey, item);
        return item;
    }

    public static void initialize() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS)
                .register((creativeTab) -> creativeTab.accept(DOG_TREAT));
    }
}