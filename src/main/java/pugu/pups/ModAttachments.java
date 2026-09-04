package pugu.pups;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

public class ModAttachments {
    public static final AttachmentType<DogBehaviorState> DOG_STATE =
            AttachmentRegistry.createPersistent(MorePups.id("dog_state"), DogBehaviorState.CODEC);

    public static final AttachmentType<Integer> FOLLOW_DISTANCE =
            AttachmentRegistry.createPersistent(MorePups.id("follow_distance"), Codec.INT);

    public static final AttachmentType<Integer> GUARD_RADIUS =
            AttachmentRegistry.createPersistent(MorePups.id("guard_radius"), Codec.INT);

    public static final AttachmentType<Integer> RELAX_RADIUS =
            AttachmentRegistry.createPersistent(MorePups.id("relax_radius"), Codec.INT);

    public static void initialize() {
    }
}