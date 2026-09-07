package pugu.pups;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

import java.util.Optional;

public class VillageDogSpawner {
    private static final int INTERVAL_TICKS = 600;
    private static final int SEARCH_RADIUS = 96;
    private static final int DOGS_PER_VILLAGE = 2;

    private static int timer;

    public static void tick(ServerLevel level) {
        if (--timer > 0) {
            return;
        }

        timer = INTERVAL_TICKS;

        for (ServerPlayer player : level.players()) {
            trySpawnNear(level, player.blockPosition());
        }
    }

    private static void trySpawnNear(ServerLevel level, BlockPos origin) {
        Optional<BlockPos> nearestBed = level.getPoiManager().findClosest(
                poi -> poi.is(PoiTypes.HOME), origin, SEARCH_RADIUS, PoiManager.Occupancy.ANY);

        if (nearestBed.isEmpty()) {
            return;
        }

        BlockPos villageCentre = nearestBed.get();

        if (!villageHasDogs(villageCentre)) {
            return;
        }

        long existing = level.getEntitiesOfClass(PupEntity.class,
                new AABB(villageCentre).inflate(48.0D)).size();

        if (existing >= DOGS_PER_VILLAGE) {
            return;
        }

        DogBreed breed = breedFor(villageCentre);

        for (int i = 0; i < DOGS_PER_VILLAGE; i++) {
            spawnOne(level, villageCentre, breed);
        }
    }

    private static boolean villageHasDogs(BlockPos villageCentre) {
        int hash = villageCentre.getX() >> 7;
        int hashZ = villageCentre.getZ() >> 7;

        return Math.floorMod(hash * 31 + hashZ * 17, 4) == 0;
    }

    private static DogBreed breedFor(BlockPos villageCentre) {
        int hash = (villageCentre.getX() >> 7) * 31 + (villageCentre.getZ() >> 7) * 17;

        return DogBreed.values()[Math.floorMod(hash / 4, DogBreed.values().length)];
    }

    private static void spawnOne(ServerLevel level, BlockPos villageCentre, DogBreed breed) {
        int x = villageCentre.getX() + level.getRandom().nextInt(17) - 8;
        int z = villageCentre.getZ() + level.getRandom().nextInt(17) - 8;
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);

        PupEntity dog = ModEntityTypes.PUP.create(level, EntitySpawnReason.NATURAL);

        if (dog == null) {
            return;
        }

        dog.snapTo(x + 0.5D, y, z + 0.5D, level.getRandom().nextFloat() * 360.0F, 0.0F);
        dog.setBreed(breed);
        level.addFreshEntity(dog);
    }
}