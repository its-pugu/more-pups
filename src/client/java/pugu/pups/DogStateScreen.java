package pugu.pups;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.wolf.Wolf;

public class DogStateScreen extends AbstractDogScreen {
    private static final int BAR_WIDTH = 102;
    private static final int BAR_HEIGHT = 5;
    private static final int BAR_U = 120;
    private static final int EMPTY_BAR_V = 20;

    private static final int FOOD_FILL_V = 26;
    private static final int PLAY_FILL_V = 32;
    private static final int SLEEP_FILL_V = 38;
    private static final int XP_FILL_V = 44;
    private static final int HAPPINESS_FILL_V = 50;

    private static final int FIRST_BAR_Y = 40;
    private static final int BAR_SPACING = 32;

    private static final Identifier STATS_BACKGROUND =
            Identifier.fromNamespaceAndPath(MorePups.MOD_ID, "textures/gui/dog_state_screen_stats.png");

    public DogStateScreen(Wolf dog) {
        super(dog, Component.literal("Dog Behavior"));
    }

    @Override
    protected DogScreenTab tab() {
        return DogScreenTab.STATS;
    }

    @Override
    protected Identifier sidePanelTexture() {
        return STATS_BACKGROUND;
    }

    @Override
    protected void renderSidePanel(GuiGraphicsExtractor graphics, int sideLeft, int sideTop) {
        graphics.text(this.font, Component.literal("Stats:"),
                sideLeft + 3, sideTop + 8, 0xFFFFFFFF);

        int xp = this.dog.getAttachedOrElse(ModAttachments.XP, 0);
        int level = this.dog.getAttachedOrElse(ModAttachments.LEVEL, 1);
        int needed = Math.max(1, level * 100);

        this.drawBar(graphics, sideLeft, sideTop, 0, "Food",
                this.dog.getAttachedOrElse(ModAttachments.FOOD, 100), FOOD_FILL_V);

        this.drawBar(graphics, sideLeft, sideTop, 1, "Play",
                this.dog.getAttachedOrElse(ModAttachments.PLAY, 100), PLAY_FILL_V);

        this.drawBar(graphics, sideLeft, sideTop, 2, "Sleep",
                this.dog.getAttachedOrElse(ModAttachments.SLEEP, 100), SLEEP_FILL_V);

        this.drawBar(graphics, sideLeft, sideTop, 3, "Happiness",
                DogStats.happiness(this.dog), HAPPINESS_FILL_V);

        this.drawBar(graphics, sideLeft, sideTop, 4, "Lv " + level,
                xp * 100 / needed, XP_FILL_V);
    }

    private void drawBar(GuiGraphicsExtractor graphics, int sideLeft, int sideTop,
                         int index, String label, int value, int fillV) {
        int barX = sideLeft + 3;
        int barY = sideTop + FIRST_BAR_Y + index * BAR_SPACING;

        graphics.text(this.font, Component.literal(label), barX, barY - 12, 0xFFFFFFFF);

        graphics.blit(RenderPipelines.GUI_TEXTURED, STATS_BACKGROUND,
                barX, barY, (float) BAR_U, (float) EMPTY_BAR_V,
                BAR_WIDTH, BAR_HEIGHT, 256, 256);

        int filled = BAR_WIDTH * Mth.clamp(value, 0, 100) / 100;

        if (filled > 0) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, STATS_BACKGROUND,
                    barX, barY, (float) BAR_U, (float) fillV,
                    filled, BAR_HEIGHT, 256, 256);
        }
    }
}