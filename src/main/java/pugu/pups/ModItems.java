package pugu.pups;

import java.util.function.Function;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TypedEntityData;

public class ModItems {

    public static final ResourceKey<Item> DOG_TREAT_ID = ModItemIds.create("dog_treat");
    public static final Item DOG_TREAT = register(DOG_TREAT_ID, Item::new, new Item.Properties());
    public static final ResourceKey<Item> BALL_ID = ModItemIds.create("ball");
    public static final Item BALL = register(BALL_ID, BallItem::new, new Item.Properties().stacksTo(16));
    public static final ResourceKey<Item> DACHSHUND_SPAWN_EGG_ID = ModItemIds.create("dachshund_spawn_egg");
    public static final Item DACHSHUND_SPAWN_EGG = register(DACHSHUND_SPAWN_EGG_ID, SpawnEggItem::new,
            new Item.Properties()
                    .spawnEgg(ModEntityTypes.PUP)
                    .component(DataComponents.ENTITY_DATA,
                            TypedEntityData.of(ModEntityTypes.PUP, breedTag(DogBreed.DACHSHUND))));
    public static final ResourceKey<Item> PUG_SPAWN_EGG_ID = ModItemIds.create("pug_spawn_egg");
    public static final Item PUG_SPAWN_EGG = register(PUG_SPAWN_EGG_ID, SpawnEggItem::new,
            new Item.Properties()
                    .spawnEgg(ModEntityTypes.PUP)
                    .component(DataComponents.ENTITY_DATA,
                            TypedEntityData.of(ModEntityTypes.PUP, breedTag(DogBreed.PUG))));
    public static final ResourceKey<Item> LABRADOR_SPAWN_EGG_ID = ModItemIds.create("labrador_spawn_egg");
    public static final Item LABRADOR_SPAWN_EGG = register(LABRADOR_SPAWN_EGG_ID, SpawnEggItem::new,
            new Item.Properties()
                    .spawnEgg(ModEntityTypes.PUP)
                    .component(DataComponents.ENTITY_DATA,
                            TypedEntityData.of(ModEntityTypes.PUP, breedTag(DogBreed.LABRADOR))));

    public static Item register(ResourceKey<Item> itemKey, Function<Item.Properties, Item> itemFactory, Item.Properties settings) {
        Item item = itemFactory.apply(settings.setId(itemKey));
        Registry.register(BuiltInRegistries.ITEM, itemKey, item);
        return item;
    }

    private static CompoundTag breedTag(DogBreed breed) {
        CompoundTag tag = new CompoundTag();
        tag.putString("breed", breed.getSerializedName());
        return tag;
    }

    public static void initialize() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS)
                .register((creativeTab) -> creativeTab.accept(DOG_TREAT));
    }
}