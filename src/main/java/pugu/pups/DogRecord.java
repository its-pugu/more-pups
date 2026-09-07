package pugu.pups;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record DogRecord(UUID dogId, ResourceKey<Level> dimension, BlockPos lastKnownPos,
                        Optional<DogBreed> breed, String name) {

    public static final Codec<DogRecord> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("dog_id").forGetter(DogRecord::dogId),
            ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(DogRecord::dimension),
            BlockPos.CODEC.fieldOf("last_known_pos").forGetter(DogRecord::lastKnownPos),
            DogBreed.CODEC.optionalFieldOf("breed").forGetter(DogRecord::breed),
            Codec.STRING.fieldOf("name").forGetter(DogRecord::name)
    ).apply(instance, DogRecord::new));

    public static final Codec<List<DogRecord>> LIST_CODEC = CODEC.listOf();
}