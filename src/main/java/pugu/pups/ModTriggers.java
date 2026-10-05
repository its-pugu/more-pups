package pugu.pups;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class ModTriggers {
    public static final DogTrigger DOG_ACTION = Registry.register(
            BuiltInRegistries.TRIGGER_TYPES, MorePups.id("dog_action"), new DogTrigger());

    public static void initialize() {
    }
}