package pugu.pups;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.wolf.Wolf;

import java.util.function.Function;

public enum DogScreenTab {
    STATS("stats_tab", DogStateScreen::new),
    SKILLS("skills_tab", DogSkillScreen::new);

    private final Identifier texture;
    private final Function<Wolf, AbstractDogScreen> factory;

    DogScreenTab(String texture, Function<Wolf, AbstractDogScreen> factory) {
        this.texture = Identifier.fromNamespaceAndPath(MorePups.MOD_ID, "textures/gui/" + texture + ".png");
        this.factory = factory;
    }

    public Identifier texture() {
        return this.texture;
    }

    public AbstractDogScreen create(Wolf dog) {
        return this.factory.apply(dog);
    }
}