package pugu.pups;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

import java.util.List;

public class DogWhistleItem extends Item {

    public DogWhistleItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            for (Wolf dog : serverPlayer.level().getEntitiesOfClass(Wolf.class,
                    player.getBoundingBox().inflate(128.0D),
                    candidate -> candidate.isTame() && candidate.isOwnedBy(player))) {
                DogTracking.refresh(dog);
            }

            List<DogRecord> dogs = player.getAttachedOrElse(ModAttachments.OWNED_DOGS, List.of());
            ServerPlayNetworking.send(serverPlayer, new DogListPayload(dogs));
        }

        return InteractionResult.SUCCESS;
    }
}