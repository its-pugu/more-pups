package pugu.pups;

import java.util.HashMap;
import java.util.Map;

public class PetAnimations {
    public static final int DURATION = 10;

    private static final Map<Integer, Long> PETTED = new HashMap<>();

    public static void record(int entityId, long gameTime) {
        PETTED.put(entityId, gameTime);
    }

    public static float progress(int entityId, long gameTime, float partialTick) {
        Long start = PETTED.get(entityId);

        if (start == null) {
            return 0.0F;
        }

        float elapsed = (gameTime - start) + partialTick;

        if (elapsed < 0.0F || elapsed > DURATION) {
            PETTED.remove(entityId);
            return 0.0F;
        }

        return elapsed / DURATION;
    }
}