package pugu.pups;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class MorePups implements ModInitializer {
	public static final String MOD_ID = "more-pups";

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
		ModCommands.initialize();
		DogInteractionHandler.initialize();

		PayloadTypeRegistry.serverboundPlay().register(SetDogStatePayload.TYPE, SetDogStatePayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(DogListPayload.TYPE, DogListPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(SummonDogPayload.TYPE, SummonDogPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(ForgetDogBedPayload.TYPE, ForgetDogBedPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(BuySkillPayload.TYPE, BuySkillPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(DropDogItemPayload.TYPE, DropDogItemPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(PetDogPayload.TYPE, PetDogPayload.CODEC);

		ServerTickEvents.END_LEVEL_TICK.register(VillageDogSpawner::tick);
		ServerTickEvents.END_LEVEL_TICK.register(DogStats::tick);

		ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
			if (entity instanceof Wolf wolf) {
				DogTracking.refresh(wolf);
			}
		});

		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if (entity instanceof Wolf wolf) {
				if (wolf.getAttachedOrElse(ModAttachments.GOALS_APPLIED, false)) {
					return;
				}

				wolf.setAttached(ModAttachments.GOALS_APPLIED, true);

				wolf.removeAllGoals(goal -> goal instanceof FollowOwnerGoal
						|| goal instanceof WaterAvoidingRandomStrollGoal
						|| goal instanceof MeleeAttackGoal
						|| goal instanceof FollowCloselyGoal
						|| goal instanceof GuardGoal
						|| goal instanceof RelaxGoal
						|| goal instanceof ReturnToBedGoal
						|| goal instanceof SleepInBedGoal
						|| goal instanceof DogAttackGoal
						|| goal instanceof MinerGoal
						|| goal instanceof ExplorerGoal);

				wolf.getGoalSelector().addGoal(6, new FollowCloselyGoal(wolf));
				wolf.getGoalSelector().addGoal(6, new GuardGoal(wolf));
				wolf.getGoalSelector().addGoal(6, new RelaxGoal(wolf));
				wolf.getGoalSelector().addGoal(6, new ReturnToBedGoal(wolf));
				wolf.getGoalSelector().addGoal(5, new SleepInBedGoal(wolf));
				wolf.getGoalSelector().addGoal(4, new DogAttackGoal(wolf, 1.2D));

				if (wolf instanceof PupEntity pup) {
					pup.getGoalSelector().addGoal(8, new MinerGoal(pup));
					pup.getGoalSelector().addGoal(5, new ExplorerGoal(pup));
				}

				DogSkillEffects.apply(wolf);
			}
		});

		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
			if (entity instanceof Wolf wolf && wolf.getOwner() instanceof Player owner) {
				DogTracking.forget(owner, wolf.getUUID());
			}

			if (source.getEntity() instanceof Wolf killer && killer.isTame()) {
				DogStats.awardXp(killer, entity instanceof Enemy ? 5 : 2);
			}
		});

		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseAmount, amount, blocked) -> {
			if (source.getEntity() instanceof Wolf wolf && wolf.isTame()) {
				DogStats.awardXp(wolf, entity instanceof Enemy ? 2 : 1);

				if (wolf instanceof PupEntity pup && !pup.getMainHandItem().isEmpty()) {
					pup.getMainHandItem().hurtAndBreak(1, pup, EquipmentSlot.MAINHAND);
				}
			}
		});

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

		ServerPlayNetworking.registerGlobalReceiver(SummonDogPayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			DogRecord record = null;

			for (DogRecord candidate : player.getAttachedOrElse(ModAttachments.OWNED_DOGS, List.of())) {
				if (candidate.dogId().equals(payload.dogId())) {
					record = candidate;
					break;
				}
			}

			if (record == null || !record.dimension().equals(player.level().dimension())) {
				return;
			}

			DogSummoning.summon(player, record);
		});

		ServerPlayNetworking.registerGlobalReceiver(ForgetDogBedPayload.TYPE, (payload, context) -> {
			if (context.player().level().getEntity(payload.entityId()) instanceof Wolf wolf
					&& wolf.isOwnedBy(context.player())) {

				BlockPos bedPos = wolf.getAttached(ModAttachments.DOG_BED_POS);

				if (bedPos != null
						&& wolf.level().getBlockEntity(bedPos) instanceof DogBedBlockEntity bed
						&& bed.isClaimedBy(wolf.getUUID())) {
					bed.clearClaim();
				}

				wolf.removeAttached(ModAttachments.DOG_BED_POS);

				if (wolf.getAttachedOrElse(ModAttachments.DOG_STATE, DogBehaviorState.FOLLOW) == DogBehaviorState.RETURN_TO_BED) {
					wolf.setAttached(ModAttachments.DOG_STATE, DogBehaviorState.FOLLOW);
					wolf.clearHome();
				}
			}
		});

		ServerPlayNetworking.registerGlobalReceiver(BuySkillPayload.TYPE, (payload, context) -> {
			if (!(context.player().level().getEntity(payload.entityId()) instanceof Wolf wolf)
					|| !wolf.isOwnedBy(context.player())) {
				return;
			}

			DogSkill skill = payload.skill();
			List<DogSkill> unlocked = wolf.getAttachedOrElse(ModAttachments.UNLOCKED_SKILLS, List.of());
			int points = wolf.getAttachedOrElse(ModAttachments.SKILL_POINTS, 0);

			if (points < 1 || unlocked.contains(skill)) {
				return;
			}

			if (skill.parent() != null && !unlocked.contains(skill.parent())) {
				return;
			}

			List<DogSkill> updated = new ArrayList<>(unlocked);

			boolean switching = skill.parent() != null
					&& updated.removeIf(existing -> existing != skill
					&& existing.parent() == skill.parent());

			if (switching) {
				ItemStack held = wolf.getMainHandItem();

				if (!held.isEmpty() && wolf.level() instanceof ServerLevel serverLevel) {
					wolf.spawnAtLocation(serverLevel, held.copy());
					wolf.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
				}

				wolf.setAttached(ModAttachments.SKILL_POINTS, 0);
			} else {
				wolf.setAttached(ModAttachments.SKILL_POINTS, points - 1);
			}

			updated.add(skill);

			wolf.setAttached(ModAttachments.UNLOCKED_SKILLS, List.copyOf(updated));

			DogSkillEffects.apply(wolf);
		});

		ServerPlayNetworking.registerGlobalReceiver(BuySkillPayload.TYPE, (payload, context) -> {
			if (!(context.player().level().getEntity(payload.entityId()) instanceof Wolf wolf)
					|| !wolf.isOwnedBy(context.player())) {
				return;
			}

			DogSkill skill = payload.skill();
			List<DogSkill> unlocked = wolf.getAttachedOrElse(ModAttachments.UNLOCKED_SKILLS, List.of());
			int points = wolf.getAttachedOrElse(ModAttachments.SKILL_POINTS, 0);


			if (points < 1 || unlocked.contains(skill)) {
				return;
			}

			if (skill.parent() != null && !unlocked.contains(skill.parent())) {
				return;
			}

			List<DogSkill> updated = new ArrayList<>(unlocked);

			if (skill.isBranch()) {
				updated.removeIf(DogSkill::isBranch);
				ItemStack held = wolf.getMainHandItem();

				if (!held.isEmpty() && wolf.level() instanceof ServerLevel serverLevel) {
					wolf.spawnAtLocation(serverLevel, held.copy());
					wolf.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
				}

				wolf.setAttached(ModAttachments.SKILL_POINTS, 0);
			} else {
				wolf.setAttached(ModAttachments.SKILL_POINTS, points - 1);
			}

			updated.add(skill);

			wolf.setAttached(ModAttachments.UNLOCKED_SKILLS, List.copyOf(updated));

			DogSkillEffects.apply(wolf);
		});

		ServerPlayNetworking.registerGlobalReceiver(DropDogItemPayload.TYPE, (payload, context) -> {
			if (context.player().level().getEntity(payload.entityId()) instanceof PupEntity pup
					&& pup.isOwnedBy(context.player())) {

				ItemStack held = pup.getMainHandItem();

				if (!held.isEmpty()) {
					pup.spawnAtLocation((ServerLevel) pup.level(), held.copy());
					pup.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
				}
			}
		});

		ServerPlayNetworking.registerGlobalReceiver(PetDogPayload.TYPE, (payload, context) -> {
			if (context.player().level().getEntity(payload.entityId()) instanceof Wolf wolf
					&& wolf.isOwnedBy(context.player())
					&& wolf.distanceToSqr(context.player()) < 25.0D) {

				long now = wolf.level().getGameTime();
				long last = wolf.getAttachedOrElse(ModAttachments.LAST_PET, 0L);

				if (now - last < 100) {
					return;
				}

				wolf.setAttached(ModAttachments.LAST_PET, now);

				ServerLevel level = context.player().level();

				level.sendParticles(ParticleTypes.HEART,
						wolf.getX(), wolf.getY() + wolf.getBbHeight(), wolf.getZ(),
						3, 0.3D, 0.3D, 0.3D, 0.0D);

				SoundEvent sound = wolf instanceof PupEntity pup
						? switch (pup.getBreed()) {
					case DACHSHUND -> ModSounds.DACHSHUND_BARK;
					case PUG -> ModSounds.PUG_BARK;
					case LABRADOR -> ModSounds.LABRADOR_BARK;
				}
						: ModSounds.DACHSHUND_BARK;

				level.playSound(null, wolf.blockPosition(), sound,
						SoundSource.NEUTRAL, 0.6F, 1.2F);

				DogStats.play(wolf, 3);
			}
		});
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}