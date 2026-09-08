package pugu.pups;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ForgetDogBedPayload(int entityId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ForgetDogBedPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MorePups.MOD_ID, "forget_dog_bed"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ForgetDogBedPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ForgetDogBedPayload::entityId,
            ForgetDogBedPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}