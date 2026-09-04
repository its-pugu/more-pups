package pugu.pups;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum DogBreed implements StringRepresentable {
    DACHSHUND("dachshund"),
    PUG("pug"),
    LABRADOR("labrador");

    public static final Codec<DogBreed> CODEC = StringRepresentable.fromEnum(DogBreed::values);

    public static final StreamCodec<ByteBuf, DogBreed> STREAM_CODEC =
            ByteBufCodecs.idMapper(i -> DogBreed.values()[i], Enum::ordinal);

    private final String name;

    DogBreed(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}