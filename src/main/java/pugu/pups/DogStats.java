package pugu.pups;

import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.wolf.Wolf;

import java.util.List;

public class DogStats {
    private static final int INTERVAL_TICKS = 600;

    private static final int FOOD_DECAY = 1;
    private static final int PLAY_DECAY = 1;
    private static final int SLEEP_DECAY = 1;

    private static final int SLEEP_DECAY_EVERY = 4;

    private static final int FOOD_WEIGHT = 50;
    private static final int PLAY_WEIGHT = 30;
    private static final int SLEEP_WEIGHT = 20;

    private static int timer;
    private static int sleepCounter;

    public static void tick(ServerLevel level) {
        if (--timer > 0) {
            return;
        }

        timer = INTERVAL_TICKS;

        boolean decaySleep = --sleepCounter <= 0;

        if (decaySleep) {
            sleepCounter = SLEEP_DECAY_EVERY;
        }

        for (ServerPlayer player : level.players()) {
            for (Wolf dog : level.getEntitiesOfClass(Wolf.class,
                    player.getBoundingBox().inflate(128.0D),
                    candidate -> candidate.isTame() && candidate.isOwnedBy(player))) {
                decay(dog, decaySleep);
            }
        }
    }

    private static void decay(Wolf dog, boolean decaySleep) {
        adjust(dog, ModAttachments.FOOD, -FOOD_DECAY);
        adjust(dog, ModAttachments.PLAY, -PLAY_DECAY);

        if (decaySleep) {
            adjust(dog, ModAttachments.SLEEP, -SLEEP_DECAY);
        }
    }

    private static void adjust(Wolf dog, AttachmentType<Integer> type, int delta) {
        int current = dog.getAttachedOrElse(type, 100);
        dog.setAttached(type, Mth.clamp(current + delta, 0, 100));
    }

    public static int happiness(Wolf dog) {
        int food = dog.getAttachedOrElse(ModAttachments.FOOD, 100);
        int play = dog.getAttachedOrElse(ModAttachments.PLAY, 100);
        int sleep = dog.getAttachedOrElse(ModAttachments.SLEEP, 100);

        return (food * FOOD_WEIGHT + play * PLAY_WEIGHT + sleep * SLEEP_WEIGHT) / 100;
    }

    public static int xpForNextLevel(Wolf dog) {
        return dog.getAttachedOrElse(ModAttachments.LEVEL, 1) * 100;
    }

    public static void awardXp(Wolf dog, int baseAmount) {
        int amount = Math.max(1, Math.round(baseAmount * xpMultiplier(dog)));
        int xp = dog.getAttachedOrElse(ModAttachments.XP, 0) + amount;
        int level = dog.getAttachedOrElse(ModAttachments.LEVEL, 1);
        while (xp >= level * 100) {
            xp -= level * 100;
            level++;
            dog.setAttached(ModAttachments.SKILL_POINTS,
                    dog.getAttachedOrElse(ModAttachments.SKILL_POINTS, 0) + 1);
        }
        dog.setAttached(ModAttachments.XP, xp);
        dog.setAttached(ModAttachments.LEVEL, level);
    }

    public static DogSkill classOf(Wolf dog) {
        return dog.getAttachedOrElse(ModAttachments.UNLOCKED_SKILLS, List.<DogSkill>of())
                .stream().filter(DogSkill::isBranch).findFirst().orElse(null);
    }

    public static float xpMultiplier(Wolf dog) {
        return 0.5F + happiness(dog) / 100.0F;
    }

    public static void feed(Wolf dog, int amount) {
        adjust(dog, ModAttachments.FOOD, amount);
    }

    public static void play(Wolf dog, int amount) {
        adjust(dog, ModAttachments.PLAY, amount);
    }

    public static void rest(Wolf dog, int amount) {
        adjust(dog, ModAttachments.SLEEP, amount);
    }
}