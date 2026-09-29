package pugu.pups;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

import java.util.List;

public enum DogSkill implements StringRepresentable {
    VITALITY("vitality", null, "Vitality", "", "+10% health", 1, 43),
    AGILITY("agility", VITALITY, "Agility", "", "+5% speed", 3, 43),
    RESILIENCE("resilience", AGILITY, "Resilience", "", "+5% armour", 2, 43),

    FIGHTER("fighter", RESILIENCE, "The Fighter",
            "Give your pup a sword and battle alongside them!",
            "Increases damage and resistance", 0, 15),
    MINER("miner", RESILIENCE, "The Miner",
            "Hand your pup a pickaxe and gain a mining companion!",
            "Barks when an ore is within 5 blocks", 4, 43),
    EXPLORER("explorer", RESILIENCE, "The Explorer",
            "Hand this pup a map and explore the world with your bestie!",
            "Has a chance of finding hidden treasure", 5, 71);

    public static final Codec<DogSkill> CODEC = StringRepresentable.fromEnum(DogSkill::values);

    public static final StreamCodec<ByteBuf, DogSkill> STREAM_CODEC =
            ByteBufCodecs.idMapper(i -> DogSkill.values()[i], Enum::ordinal);

    private final String name;
    private final @Nullable DogSkill parent;
    private final String title;
    private final String description;
    private final String effect;
    private final int column;
    private final int x;

    DogSkill(String name, @Nullable DogSkill parent, String title, String description,
             String effect, int column, int x) {
        this.name = name;
        this.parent = parent;
        this.title = title;
        this.description = description;
        this.effect = effect;
        this.column = column;
        this.x = x;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public @Nullable DogSkill parent() {
        return this.parent;
    }

    public String title() {
        return this.title;
    }

    public String description() {
        return this.description;
    }

    public String effect() {
        return this.effect;
    }

    public int column() {
        return this.column;
    }

    public int x() {
        return this.x;
    }

    public int tier() {
        return this.parent == null ? 0 : this.parent.tier() + 1;
    }

    public boolean isBranch() {
        return this.parent == RESILIENCE;
    }

    public boolean accepts(ItemStack stack) {
        return switch (this) {
            case FIGHTER -> stack.is(ItemTags.SWORDS);
            case MINER -> stack.is(ItemTags.PICKAXES);
            case EXPLORER -> stack.is(Items.MAP) || stack.is(Items.FILLED_MAP);
            default -> false;
        };
    }

    public static boolean isVisible(DogSkill skill, List<DogSkill> unlocked) {
        return unlocked.contains(skill)
                || skill.parent == null
                || unlocked.contains(skill.parent);
    }
}