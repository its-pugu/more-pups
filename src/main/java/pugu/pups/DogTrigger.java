package pugu.pups;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Optional;

public class DogTrigger extends SimpleCriterionTrigger<DogTrigger.TriggerInstance> {

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void fire(ServerPlayer player, String action) {
        this.trigger(player, instance -> instance.matches(action));
    }

    public record TriggerInstance(Optional<Holder<LootItemCondition>> player, String action)
            implements SimpleCriterionTrigger.SimpleInstance {

        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                LootItemCondition.CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                Codec.STRING.fieldOf("action").forGetter(TriggerInstance::action)
        ).apply(instance, TriggerInstance::new));

        public boolean matches(String action) {
            return this.action.equals(action);
        }
    }
}