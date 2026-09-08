package pugu.pups;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfirmForgetBedScreen extends Screen {
    private final DogStateScreen parent;

    public ConfirmForgetBedScreen(DogStateScreen parent) {
        super(Component.literal("Are you sure?"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = this.height / 2;

        this.addRenderableWidget(Button.builder(Component.literal("Yes"), button -> this.parent.forgetBed())
                .bounds(centerX - 52, y, 50, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("No"), button -> this.onClose())
                .bounds(centerX + 2, y, 50, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);

        graphics.text(this.font, this.title,
                this.width / 2 - this.font.width(this.title) / 2, this.height / 2 - 25, 0xFFFFFFFF);
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }
}