package pugu.pups;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {

    public static final SoundEvent DACHSHUND_BARK = register("dachshund_bark");
    public static final SoundEvent DACHSHUND_HURT = register("dachshund_hurt");
    public static final SoundEvent DACHSHUND_DEATH = register("dachshund_death");

    public static final SoundEvent PUG_BARK = register("pug_bark");
    public static final SoundEvent PUG_HURT = register("pug_hurt");
    public static final SoundEvent PUG_DEATH = register("pug_death");

    public static final SoundEvent LABRADOR_BARK = register("labrador_bark");
    public static final SoundEvent LABRADOR_HURT = register("labrador_hurt");
    public static final SoundEvent LABRADOR_DEATH = register("labrador_death");

    private static SoundEvent register(String name) {
        Identifier id = MorePups.id(name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static void initialize() {
    }
}