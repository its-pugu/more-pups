package pugu.pups;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.DyeColor;

public class ModDataComponents {
    public static final DataComponentType<DyeColor> BALL_DYE_COLOR = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            MorePups.id("ball_dye_color"),
            DataComponentType.<DyeColor>builder()
                    .persistent(DyeColor.CODEC)
                    .networkSynchronized(ByteBufCodecs.idMapper(DyeColor::byId, DyeColor::getId))
                    .build()
    );
    public static final DataComponentType<DyeColor> BED_DYE_COLOR = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            MorePups.id("bed_dye_color"),
            DataComponentType.<DyeColor>builder()
                    .persistent(DyeColor.CODEC)
                    .networkSynchronized(ByteBufCodecs.idMapper(DyeColor::byId, DyeColor::getId))
                    .build()
    );

    public static void initialize() {
    }
}