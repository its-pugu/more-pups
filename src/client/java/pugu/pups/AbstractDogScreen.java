package pugu.pups;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.wolf.Wolf;

import java.util.function.IntConsumer;

public abstract class AbstractDogScreen extends Screen {
    protected static final int FOLLOW_MIN = 2;
    protected static final int FOLLOW_MAX = 12;
    protected static final int RADIUS_MIN = 2;
    protected static final int RADIUS_MAX = 32;

    protected static final int PANEL_WIDTH = 118;
    protected static final int PANEL_HEIGHT = 206;
    protected static final int TEXTURE_V = 14;

    protected static final int SIDE_WIDTH = 111;
    protected static final int SIDE_HEIGHT = 193;
    protected static final int SIDE_V = 20;
    protected static final int SIDE_OFFSET_X = 118;
    protected static final int SIDE_OFFSET_Y = 6;

    protected static final int TAB_WIDTH = 22;
    protected static final int TAB_HEIGHT = 23;
    protected static final int TAB_OVERLAP = 3;
    protected static final int FIRST_TAB_Y = 4;

    protected static final int HEALTH_U = 119;
    protected static final int HEALTH_FILL_V = 14;
    protected static final int HEALTH_EMPTY_V = 18;
    protected static final int HEALTH_WIDTH = 90;
    protected static final int HEALTH_HEIGHT = 4;
    protected static final int HEALTH_X = 14;
    protected static final int HEALTH_Y = 68;
    protected static final int NAME_Y = 12;

    protected static final int TOOLTIP_COLOUR = 0xFFFFFFFF;
    protected static final int TOOLTIP_DIM = 0xFFAAAAAA;

    protected static final Identifier BACKGROUND =
            Identifier.fromNamespaceAndPath(MorePups.MOD_ID, "textures/gui/dog_state_screen.png");

    protected final Wolf dog;
    protected final int dogEntityId;
    protected final boolean hasBed;

    protected DogBehaviorState selectedState;
    protected int followDistance;
    protected int guardRadius;
    protected int relaxRadius;

    protected int left;
    protected int top;
    protected float mouseXPos;
    protected float mouseYPos;

    protected String hoverTitle;
    protected String hoverDescription;

    protected AbstractDogScreen(Wolf dog, Component title) {
        super(title);

        this.dog = dog;
        this.dogEntityId = dog.getId();
        this.hasBed = dog.getAttached(ModAttachments.DOG_BED_POS) != null;
        this.selectedState = dog.getAttachedOrElse(ModAttachments.DOG_STATE, DogBehaviorState.FOLLOW);
        this.followDistance = dog.getAttachedOrElse(ModAttachments.FOLLOW_DISTANCE, 3);
        this.guardRadius = dog.getAttachedOrElse(ModAttachments.GUARD_RADIUS, 8);
        this.relaxRadius = dog.getAttachedOrElse(ModAttachments.RELAX_RADIUS, 16);
    }

    protected abstract DogScreenTab tab();

    protected abstract Identifier sidePanelTexture();

    protected abstract void renderSidePanel(GuiGraphicsExtractor graphics, int sideLeft, int sideTop);

    @Override
    protected void init() {
        this.left = (this.width - (PANEL_WIDTH + SIDE_WIDTH + TAB_WIDTH - TAB_OVERLAP)) / 2;
        this.top = (this.height - PANEL_HEIGHT) / 2;

        int rowX = this.left + 10;
        int rowWidth = 98;
        int firstRowY = this.top + 81;

        this.addRow(rowX, firstRowY, rowWidth, DogBehaviorState.FOLLOW, "Follow",
                "Distance", "How close the dog stays to you",
                this.followDistance, FOLLOW_MIN, FOLLOW_MAX,
                value -> this.followDistance = value);

        this.addRow(rowX, firstRowY + 24, rowWidth, DogBehaviorState.GUARD, "Guard",
                "Radius", "How far the dog will chase from its post",
                this.guardRadius, RADIUS_MIN, RADIUS_MAX,
                value -> this.guardRadius = value);

        this.addRow(rowX, firstRowY + 48, rowWidth, DogBehaviorState.RELAX, "Relax",
                "Radius", "How far the dog wanders from where you set it",
                this.relaxRadius, RADIUS_MIN, RADIUS_MAX,
                value -> this.relaxRadius = value);

        Button bed = this.addRenderableWidget(Button.builder(Component.literal("Return to Bed"),
                        button -> this.select(DogBehaviorState.RETURN_TO_BED))
                .bounds(rowX, firstRowY + 72, this.hasBed ? 78 : rowWidth, 20).build());

        bed.active = this.hasBed && this.selectedState != DogBehaviorState.RETURN_TO_BED;

        if (this.hasBed) {
            bed.setTooltip(Tooltip.create(Component.literal("Send the dog back to its bed")));

            Button forget = this.addRenderableWidget(Button.builder(Component.literal("\u274C"),
                            button -> this.minecraft.gui.setScreen(new ConfirmForgetBedScreen(this)))
                    .bounds(rowX + 82, firstRowY + 72, 16, 20).build());

            forget.setTooltip(Tooltip.create(Component.literal("Delete Bed")));
        } else {
            bed.setTooltip(Tooltip.create(
                    Component.literal("Dog has not claimed a bed.").withStyle(ChatFormatting.RED)));
        }

        this.addRenderableWidget(Button.builder(Component.literal("Cancel"), button -> this.onClose())
                .bounds(rowX, firstRowY + 98, 46, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("OK"), button -> this.apply())
                .bounds(rowX + 52, firstRowY + 98, 46, 20).build());
    }

    private void addRow(int x, int y, int width, DogBehaviorState state, String label,
                        String sliderLabel, String tooltip, int value, int min, int max, IntConsumer onChange) {
        if (this.selectedState == state) {
            IntSlider slider = new IntSlider(x, y, width, sliderLabel, value, min, max, onChange);
            slider.setTooltip(Tooltip.create(Component.literal(tooltip)));
            this.addRenderableWidget(slider);
        } else {
            this.addRenderableWidget(Button.builder(Component.literal(label), b -> this.select(state))
                    .bounds(x, y, width, 20).build());
        }
    }

    protected int sideLeft() {
        return this.left + SIDE_OFFSET_X;
    }

    protected int sideTop() {
        return this.top + SIDE_OFFSET_Y;
    }

    protected int tabX() {
        return this.sideLeft() + SIDE_WIDTH - TAB_OVERLAP;
    }

    protected int tabY(DogScreenTab tab) {
        return this.sideTop() + FIRST_TAB_Y + tab.ordinal() * TAB_HEIGHT;
    }

    protected boolean isOver(int x, int y, int width, int height) {
        return this.mouseXPos >= x && this.mouseXPos < x + width
                && this.mouseYPos >= y && this.mouseYPos < y + height;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        this.mouseXPos = mouseX;
        this.mouseYPos = mouseY;

        this.hoverTitle = null;
        this.hoverDescription = null;

        int sideLeft = this.sideLeft();
        int sideTop = this.sideTop();

        for (DogScreenTab tab : DogScreenTab.values()) {
            if (tab != this.tab()) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, tab.texture(),
                        this.tabX(), this.tabY(tab), 0.0F, 0.0F,
                        TAB_WIDTH, TAB_HEIGHT, TAB_WIDTH, TAB_HEIGHT);
            }
        }

        graphics.blit(RenderPipelines.GUI_TEXTURED, this.sidePanelTexture(),
                sideLeft, sideTop, 0.0F, (float) SIDE_V,
                SIDE_WIDTH, SIDE_HEIGHT, 256, 256);

        graphics.blit(RenderPipelines.GUI_TEXTURED, this.tab().texture(),
                this.tabX(), this.tabY(this.tab()), 0.0F, 0.0F,
                TAB_WIDTH, TAB_HEIGHT, TAB_WIDTH, TAB_HEIGHT);

        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND,
                this.left, this.top, 0.0F, (float) TEXTURE_V,
                PANEL_WIDTH, PANEL_HEIGHT, 256, 256);

        InventoryScreen.extractEntityInInventoryFollowsMouse(graphics,
                this.left + 10, this.top + 8,
                this.left + 108, this.top + 77,
                60, 0.0625F, this.mouseXPos, this.mouseYPos, this.dog);

        super.extractRenderState(graphics, mouseX, mouseY, a);

        Component name = this.dog.getName();

        graphics.text(this.font, name,
                this.left + 59 - this.font.width(name) / 2, this.top + NAME_Y, 0xFFFFFFFF);

        int healthX = this.left + HEALTH_X;
        int healthY = this.top + HEALTH_Y;

        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND,
                healthX, healthY, (float) HEALTH_U, (float) HEALTH_EMPTY_V,
                HEALTH_WIDTH, HEALTH_HEIGHT, 256, 256);

        int healthFilled = (int) (HEALTH_WIDTH * this.dog.getHealth() / this.dog.getMaxHealth());

        if (healthFilled > 0) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND,
                    healthX, healthY, (float) HEALTH_U, (float) HEALTH_FILL_V,
                    healthFilled, HEALTH_HEIGHT, 256, 256);
        }

        if (this.isOver(healthX, healthY, HEALTH_WIDTH, HEALTH_HEIGHT)) {
            this.hoverTitle = "Health";
            this.hoverDescription = (int) this.dog.getHealth() + "/" + (int) this.dog.getMaxHealth();
        }

        for (DogScreenTab tab : DogScreenTab.values()) {
            if (this.isOver(this.tabX(), this.tabY(tab), TAB_WIDTH, TAB_HEIGHT)) {
                this.hoverTitle = tab.title();
            }
        }

        this.renderSidePanel(graphics, sideLeft, sideTop);
        this.renderTooltip(graphics);
    }

    protected void renderTooltip(GuiGraphicsExtractor graphics) {
        if (this.hoverTitle == null) {
            return;
        }

        graphics.text(this.font, Component.literal(this.hoverTitle),
                (int) this.mouseXPos + 8, (int) this.mouseYPos - 12, TOOLTIP_COLOUR);

        if (this.hoverDescription != null) {
            graphics.text(this.font, Component.literal(this.hoverDescription),
                    (int) this.mouseXPos + 8, (int) this.mouseYPos, TOOLTIP_DIM);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        for (DogScreenTab tab : DogScreenTab.values()) {
            if (tab == this.tab()) {
                continue;
            }

            int x = this.tabX();
            int y = this.tabY(tab);

            if (event.x() >= x && event.x() < x + TAB_WIDTH
                    && event.y() >= y && event.y() < y + TAB_HEIGHT) {
                this.minecraft.gui.setScreen(tab.create(this.dog));
                return true;
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    protected void select(DogBehaviorState state) {
        this.selectedState = state;
        this.rebuildWidgets();
    }

    protected void apply() {
        ClientPlayNetworking.send(new SetDogStatePayload(this.dogEntityId, this.selectedState,
                this.followDistance, this.guardRadius, this.relaxRadius));
        this.minecraft.gui.setScreen(null);
    }

    void forgetBed() {
        ClientPlayNetworking.send(new ForgetDogBedPayload(this.dogEntityId));
        this.minecraft.gui.setScreen(null);
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(null);
    }

    protected static class IntSlider extends AbstractSliderButton {
        private final String label;
        private final int min;
        private final int max;
        private final IntConsumer onChange;

        IntSlider(int x, int y, int width, String label, int initial, int min, int max, IntConsumer onChange) {
            super(x, y, width, 20, Component.empty(), (double) (initial - min) / (max - min));
            this.label = label;
            this.min = min;
            this.max = max;
            this.onChange = onChange;
            this.updateMessage();
        }

        private int intValue() {
            return Mth.floor(Mth.lerp(this.value, this.min, this.max) + 0.5D);
        }

        @Override
        protected void applyValue() {
            this.onChange.accept(this.intValue());
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.literal(this.label + ": " + this.intValue()));
        }
    }
}