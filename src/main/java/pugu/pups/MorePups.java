package pugu.pups;

import net.fabricmc.api.ModInitializer;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.wolf.Wolf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;

public class MorePups implements ModInitializer {
	public static final String MOD_ID = "more-pups";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("WOOF!");
		ModItems.initialize();
		ModBlocks.initialize();
		ModBlockEntities.initialize();
		ModEntityTypes.initialize();
		ModSounds.initialize();
		ModCreativeTabs.initialize();
		ModDataComponents.initialize();
		ModRecipes.initialize();
		ModAttachments.initialize();
		DogInteractionHandler.initialize();
		PayloadTypeRegistry.serverboundPlay().register(SetDogStatePayload.TYPE, SetDogStatePayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(SetDogStatePayload.TYPE, (payload, context) -> {
			if (context.player().level().getEntity(payload.entityId()) instanceof Wolf wolf
					&& wolf.isOwnedBy(context.player())) {
				wolf.setAttached(ModAttachments.DOG_STATE, payload.state());
				wolf.setAttached(ModAttachments.FOLLOW_DISTANCE, Mth.clamp(payload.followDistance(), 2, 12));
				wolf.setAttached(ModAttachments.GUARD_RADIUS, Mth.clamp(payload.guardRadius(), 2, 32));

				wolf.setAttached(ModAttachments.RELAX_RADIUS, Mth.clamp(payload.relaxRadius(), 2, 32));

				if (payload.state() == DogBehaviorState.FOLLOW) {
					wolf.clearHome();
				} else if (payload.state() == DogBehaviorState.GUARD) {
					wolf.setHomeTo(wolf.blockPosition(), wolf.getAttachedOrElse(ModAttachments.GUARD_RADIUS, 8));
				} else if (payload.state() == DogBehaviorState.RETURN_TO_BED) {
					BlockPos bed = wolf.getAttached(ModAttachments.DOG_BED_POS);

					if (bed != null) {
						wolf.setHomeTo(bed, wolf.getAttachedOrElse(ModAttachments.RELAX_RADIUS, 16));
					}
				} else {
					wolf.setHomeTo(wolf.blockPosition(), wolf.getAttachedOrElse(ModAttachments.RELAX_RADIUS, 16));
				}

			}
		});
		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if (entity instanceof Wolf wolf) {
				wolf.removeAllGoals(goal -> goal instanceof FollowOwnerGoal || goal instanceof WaterAvoidingRandomStrollGoal);

				wolf.getGoalSelector().addGoal(6, new FollowCloselyGoal(wolf));
				wolf.getGoalSelector().addGoal(6, new GuardGoal(wolf));
				wolf.getGoalSelector().addGoal(5, new SleepInBedGoal(wolf));
				wolf.getGoalSelector().addGoal(6, new RelaxGoal(wolf));
				wolf.getGoalSelector().addGoal(6, new ReturnToBedGoal(wolf));
			}
		});
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}