package pugu.pups;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.wolf.Wolf;

import java.util.function.Function;

public enum DogScreenTab {
    STATS("stats_tab", "Stats", DogStateScreen::new),
    SKILLS("skills_tab", "Skill Tree", DogSkillScreen::new);

    private final Identifier texture;
    private final String title;
    private final Function<Wolf, AbstractDogScreen> factory;

    DogScreenTab(String texture, String title, Function<Wolf, AbstractDogScreen> factory) {
        this.texture = Identifier.fromNamespaceAndPath(MorePups.MOD_ID, "textures/gui/" + texture + ".png");
        this.title = title;
        this.factory = factory;
    }

    public Identifier texture() {
        return this.texture;
    }

    public String title() {
        return this.title;
    }

    public AbstractDogScreen create(Wolf dog) {
        return this.factory.apply(dog);
    }
}