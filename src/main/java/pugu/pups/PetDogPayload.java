package pugu.pups;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record PetDogPayload(int entityId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PetDogPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MorePups.MOD_ID, "pet_dog"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PetDogPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, PetDogPayload::entityId,
            PetDogPayload::new
    );



    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}