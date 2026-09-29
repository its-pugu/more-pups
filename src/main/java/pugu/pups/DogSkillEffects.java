package pugu.pups;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.wolf.Wolf;

import java.util.List;

public class DogSkillEffects {

    public static void apply(Wolf dog) {
        List<DogSkill> unlocked = dog.getAttachedOrElse(ModAttachments.UNLOCKED_SKILLS, List.of());

        float previousMax = dog.getMaxHealth();
        float ratio = previousMax > 0.0F ? dog.getHealth() / previousMax : 1.0F;

        set(dog, Attributes.MAX_HEALTH, DogSkill.VITALITY, 0.10D, unlocked);
        set(dog, Attributes.MOVEMENT_SPEED, DogSkill.AGILITY, 0.05D, unlocked);
        set(dog, Attributes.ATTACK_DAMAGE, DogSkill.FIGHTER, 0.05D, unlocked);
        set(dog, Attributes.ARMOR, DogSkill.RESILIENCE, 0.05D, unlocked);

        dog.setHealth(Math.min(dog.getMaxHealth(), dog.getMaxHealth() * ratio));
    }

    private static void set(Wolf dog, Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
                            DogSkill skill, double amount, List<DogSkill> unlocked) {
        AttributeInstance instance = dog.getAttribute(attribute);

        if (instance == null) {
            return;
        }

        Identifier id = MorePups.id("skill_" + skill.getSerializedName());

        instance.removeModifier(id);

        if (unlocked.contains(skill)) {
            instance.addPermanentModifier(new AttributeModifier(
                    id, amount, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
    }
}