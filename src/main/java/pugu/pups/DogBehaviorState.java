package pugu.pups;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public enum DogBehaviorState implements StringRepresentable {
    FOLLOW("follow"),
    GUARD("guard"),
    RELAX("relax");

    public static final Codec<DogBehaviorState> CODEC = StringRepresentable.fromEnum(DogBehaviorState::values);
    public static final StreamCodec<ByteBuf, DogBehaviorState> STREAM_CODEC =
            ByteBufCodecs.idMapper(i -> DogBehaviorState.values()[i], Enum::ordinal);


    private final String name;

    DogBehaviorState(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public DogBehaviorState next() {
        return switch (this) {
            case FOLLOW -> GUARD;
            case GUARD -> RELAX;
            case RELAX -> FOLLOW;
        };
    }
}