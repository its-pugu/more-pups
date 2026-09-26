package pugu.pups;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.wolf.Wolf;

public class DogSkillScreen extends AbstractDogScreen {
    private static final Identifier SKILL_BACKGROUND =
            Identifier.fromNamespaceAndPath(MorePups.MOD_ID, "textures/gui/skill_tree.png");

    public DogSkillScreen(Wolf dog) {
        super(dog, Component.literal("Skill Tree"));
    }

    @Override
    protected DogScreenTab tab() {
        return DogScreenTab.SKILLS;
    }

    @Override
    protected Identifier sidePanelTexture() {
        return SKILL_BACKGROUND;
    }

    @Override
    protected void renderSidePanel(GuiGraphicsExtractor graphics, int sideLeft, int sideTop) {
        graphics.text(this.font, Component.literal("Skills:"),
                sideLeft + 3, sideTop + 8, 0xFFFFFFFF);
    }
}