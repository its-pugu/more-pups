package pugu.pups;

import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DogTracking {

    public static void record(Player owner, Wolf dog) {
        List<DogRecord> current = owner.getAttachedOrElse(ModAttachments.OWNED_DOGS, List.of());
        List<DogRecord> updated = new ArrayList<>(current.size() + 1);

        for (DogRecord existing : current) {
            if (!existing.dogId().equals(dog.getUUID())) {
                updated.add(existing);
            }
        }

        updated.add(new DogRecord(
                dog.getUUID(),
                dog.level().dimension(),
                dog.blockPosition(),
                dog instanceof PupEntity pup ? Optional.of(pup.getBreed()) : Optional.empty(),
                dog.hasCustomName() ? dog.getCustomName().getString() : ""));

        owner.setAttached(ModAttachments.OWNED_DOGS, List.copyOf(updated));
    }

    public static void forget(Player owner, UUID dogId) {
        List<DogRecord> current = owner.getAttachedOrElse(ModAttachments.OWNED_DOGS, List.of());
        List<DogRecord> updated = new ArrayList<>(current.size());

        for (DogRecord existing : current) {
            if (!existing.dogId().equals(dogId)) {
                updated.add(existing);
            }
        }

        owner.setAttached(ModAttachments.OWNED_DOGS, List.copyOf(updated));
    }

    public static void refresh(Wolf dog) {
        if (dog.isAlive() && dog.isTame() && dog.getOwner() instanceof Player owner) {
            record(owner, dog);
        }
    }
}