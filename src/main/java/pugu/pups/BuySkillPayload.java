package pugu.pups;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record BuySkillPayload(int entityId, DogSkill skill) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<BuySkillPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MorePups.MOD_ID, "buy_skill"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BuySkillPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, BuySkillPayload::entityId,
            DogSkill.STREAM_CODEC, BuySkillPayload::skill,
            BuySkillPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}