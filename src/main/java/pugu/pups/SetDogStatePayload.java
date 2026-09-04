package pugu.pups;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SetDogStatePayload(int entityId, DogBehaviorState state, int followDistance, int guardRadius,
                                 int relaxRadius) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SetDogStatePayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MorePups.MOD_ID, "set_dog_state"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SetDogStatePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SetDogStatePayload::entityId,
            DogBehaviorState.STREAM_CODEC, SetDogStatePayload::state,
            ByteBufCodecs.VAR_INT, SetDogStatePayload::followDistance,
            ByteBufCodecs.VAR_INT, SetDogStatePayload::guardRadius,
            ByteBufCodecs.VAR_INT, SetDogStatePayload::relaxRadius,
            SetDogStatePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}