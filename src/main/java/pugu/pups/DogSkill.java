package pugu.pups;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.Nullable;

import java.util.List;

public enum DogSkill implements StringRepresentable {
    VITALITY("vitality", null, "Vitality", "+10% health", 1, 43),
    AGILITY("agility", VITALITY, "Agility", "+5% speed", 3, 43),
    FEROCITY("ferocity", AGILITY, "Ferocity", "+5% damage", 0, 43),

    FIGHTER("fighter", FEROCITY, "Fighter", "+5% armour", 2, 15),
    MINER("miner", FEROCITY, "Miner", "Carries blocks for you", 2, 43),
    EXPLORER("explorer", FEROCITY, "Explorer", "Finds buried treasure", 2, 71);

    public static final Codec<DogSkill> CODEC = StringRepresentable.fromEnum(DogSkill::values);

    public static final StreamCodec<ByteBuf, DogSkill> STREAM_CODEC =
            ByteBufCodecs.idMapper(i -> DogSkill.values()[i], Enum::ordinal);

    private final String name;
    private final @Nullable DogSkill parent;
    private final String title;
    private final String description;
    private final int column;
    private final int x;

    DogSkill(String name, @Nullable DogSkill parent, String title, String description, int column, int x) {
        this.name = name;
        this.parent = parent;
        this.title = title;
        this.description = description;
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
        return this.parent == FEROCITY;
    }

    public static boolean isVisible(DogSkill skill, List<DogSkill> unlocked) {
        return unlocked.contains(skill)
                || skill.parent == null
                || unlocked.contains(skill.parent);
    }
}