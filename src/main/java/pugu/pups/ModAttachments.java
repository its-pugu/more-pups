package pugu.pups;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;

import java.util.List;

public class ModAttachments {
    public static final AttachmentType<DogBehaviorState> DOG_STATE = AttachmentRegistry.create(
            MorePups.id("dog_state"),
            builder -> builder
                    .initializer(() -> DogBehaviorState.FOLLOW)
                    .persistent(DogBehaviorState.CODEC)
                    .syncWith(DogBehaviorState.STREAM_CODEC, AttachmentSyncPredicate.all()));

    public static final AttachmentType<Integer> FOLLOW_DISTANCE = AttachmentRegistry.create(
            MorePups.id("follow_distance"),
            builder -> builder
                    .initializer(() -> 3)
                    .persistent(Codec.INT)
                    .syncWith(ByteBufCodecs.VAR_INT, AttachmentSyncPredicate.all()));

    public static final AttachmentType<Integer> GUARD_RADIUS = AttachmentRegistry.create(
            MorePups.id("guard_radius"),
            builder -> builder
                    .initializer(() -> 8)
                    .persistent(Codec.INT)
                    .syncWith(ByteBufCodecs.VAR_INT, AttachmentSyncPredicate.all()));

    public static final AttachmentType<Integer> RELAX_RADIUS = AttachmentRegistry.create(
            MorePups.id("relax_radius"),
            builder -> builder
                    .initializer(() -> 16)
                    .persistent(Codec.INT)
                    .syncWith(ByteBufCodecs.VAR_INT, AttachmentSyncPredicate.all()));

    public static final AttachmentType<BlockPos> DOG_BED_POS = AttachmentRegistry.create(
            MorePups.id("dog_bed_pos"),
            builder -> builder
                    .persistent(BlockPos.CODEC)
                    .syncWith(BlockPos.STREAM_CODEC, AttachmentSyncPredicate.all()));
    public static final AttachmentType<List<DogRecord>> OWNED_DOGS = AttachmentRegistry.create(
            MorePups.id("owned_dogs"),
            builder -> builder
                    .initializer(List::of)
                    .persistent(DogRecord.LIST_CODEC));

    public static void initialize() {
    }
}