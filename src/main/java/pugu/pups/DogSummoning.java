package pugu.pups;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public class DogSummoning {

    public static void summon(ServerPlayer player, DogRecord record) {
        ServerLevel level = player.level();

        player.sendSystemMessage(Component.literal(arrivalMessage(record)).withStyle(ChatFormatting.YELLOW));

        Entity entity = level.getEntityInAnyDimension(record.dogId());
        ChunkPos forced = null;

        if (entity == null) {
            forced = new ChunkPos(
                    SectionPos.blockToSectionCoord(record.lastKnownPos().getX()),
                    SectionPos.blockToSectionCoord(record.lastKnownPos().getZ()));

            level.setChunkForced(forced.x(), forced.z(), true);
            level.waitForEntities(forced, 1);

            entity = level.getEntityInAnyDimension(record.dogId());
        }

        if (!(entity instanceof Wolf dog)) {
            DogTracking.forget(player, record.dogId());
            player.sendSystemMessage(Component.literal("That dog could not be found.")
                    .withStyle(ChatFormatting.RED));

            unforce(level, forced);
            return;
        }

        if (dog.distanceToSqr(player) > 1024.0D) {
            Vec3 target = findArrivalSpot(player);
            dog.snapTo(target.x, target.y, target.z, dog.getYRot(), 0.0F);
        }

        dog.setOrderedToSit(false);
        dog.setInSittingPose(false);
        dog.setAttached(ModAttachments.DOG_STATE, DogBehaviorState.FOLLOW);
        dog.clearHome();

        if (dog instanceof PupEntity pup) {
            pup.setSleeping(false);
        }

        unforce(level, forced);
    }

    private static void unforce(ServerLevel level, ChunkPos forced) {
        if (forced != null) {
            level.setChunkForced(forced.x(), forced.z(), false);
        }
    }

    private static String arrivalMessage(DogRecord record) {
        if (record.name().isEmpty()) {
            return "Your dog is on their way...";
        }

        return record.name() + " is on their way...";
    }

    private static Vec3 findArrivalSpot(ServerPlayer player) {
        double angle = player.getRandom().nextDouble() * Math.PI * 2.0D;
        double distance = 30.0D + player.getRandom().nextDouble() * 4.0D;

        double x = player.getX() + Math.cos(angle) * distance;
        double z = player.getZ() + Math.sin(angle) * distance;
        double y = player.level().getHeightmapPos(
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                BlockPos.containing(x, player.getY(), z)).getY();

        return new Vec3(x, y, z);
    }
}