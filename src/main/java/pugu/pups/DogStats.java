package pugu.pups;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.wolf.Wolf;

public class DogStats {
    private static final int INTERVAL_TICKS = 600;
    private static final int FOOD_DECAY = 1;
    private static final int FOOD_WEIGHT = 50;

    private static int timer;

    public static void tick(ServerLevel level) {
        if (--timer > 0) {
            return;
        }

        timer = INTERVAL_TICKS;

        for (ServerPlayer player : level.players()) {
            for (Wolf dog : level.getEntitiesOfClass(Wolf.class,
                    player.getBoundingBox().inflate(128.0D),
                    candidate -> candidate.isTame() && candidate.isOwnedBy(player))) {
                decay(dog);
            }
        }
    }

    private static void decay(Wolf dog) {
        int food = dog.getAttachedOrElse(ModAttachments.FOOD, 100);
        dog.setAttached(ModAttachments.FOOD, Math.max(0, food - FOOD_DECAY));
    }

    public static int happiness(Wolf dog) {
        int food = dog.getAttachedOrElse(ModAttachments.FOOD, 100);

        return Mth.clamp(food * FOOD_WEIGHT / 100, 0, 100);
    }

    public static float xpMultiplier(Wolf dog) {
        return 0.5F + happiness(dog) / 100.0F;
    }

    public static void feed(Wolf dog, int amount) {
        int food = dog.getAttachedOrElse(ModAttachments.FOOD, 100);
        dog.setAttached(ModAttachments.FOOD, Math.min(100, food + amount));
    }
}