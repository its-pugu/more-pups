package pugu.pups;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record DropDogItemPayload(int entityId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<DropDogItemPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MorePups.MOD_ID, "drop_dog_item"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DropDogItemPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, DropDogItemPayload::entityId,
            DropDogItemPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}