package pugu.pups;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.UUID;

public record SummonDogPayload(UUID dogId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SummonDogPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MorePups.MOD_ID, "summon_dog"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SummonDogPayload> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, SummonDogPayload::dogId,
            SummonDogPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}