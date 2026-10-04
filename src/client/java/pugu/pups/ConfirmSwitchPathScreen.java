package pugu.pups;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfirmSwitchPathScreen extends Screen {
    private final DogSkillScreen parent;
    private final DogSkill skill;

    public ConfirmSwitchPathScreen(DogSkillScreen parent, DogSkill skill) {
        super(Component.literal("Switch to " + skill.title() + "?"));
        this.parent = parent;
        this.skill = skill;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = this.height / 2 + 10;

        this.addRenderableWidget(Button.builder(Component.literal("Yes"),
                        button -> this.parent.switchPath(this.skill))
                .bounds(centerX - 52, y, 50, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("No"), button -> this.onClose())
                .bounds(centerX + 2, y, 50, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);

        graphics.text(this.font, this.title,
                this.width / 2 - this.font.width(this.title) / 2, this.height / 2 - 30, 0xFFFFFFFF);

        Component warning = Component.literal("Your current path and all skill points will be lost.")
                .withStyle(ChatFormatting.RED);

        graphics.text(this.font, warning,
                this.width / 2 - this.font.width(warning) / 2, this.height / 2 - 16, 0xFFFFFFFF);
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }
}