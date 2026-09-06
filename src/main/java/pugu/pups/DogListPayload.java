package pugu.pups;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

public record DogListPayload(List<DogRecord> dogs) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<DogListPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MorePups.MOD_ID, "dog_list"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DogListPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(DogRecord.LIST_CODEC), DogListPayload::dogs,
            DogListPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}