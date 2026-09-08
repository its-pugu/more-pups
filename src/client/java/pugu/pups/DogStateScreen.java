package pugu.pups;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.wolf.Wolf;

import java.util.function.IntConsumer;

public class DogStateScreen extends Screen {
    private static final int FOLLOW_MIN = 2;
    private static final int FOLLOW_MAX = 12;
    private static final int RADIUS_MIN = 2;
    private static final int RADIUS_MAX = 32;

    private static final int PANEL_WIDTH = 118;
    private static final int PANEL_HEIGHT = 206;
    private static final int TEXTURE_V = 14;

    private static final Identifier BACKGROUND =
            Identifier.fromNamespaceAndPath(MorePups.MOD_ID, "textures/gui/dog_state_screen.png");

    private final Wolf dog;
    private final int dogEntityId;
    private final boolean hasBed;

    private DogBehaviorState selectedState;
    private int followDistance;
    private int guardRadius;
    private int relaxRadius;

    private int left;
    private int top;
    private float mouseXPos;
    private float mouseYPos;

    public DogStateScreen(Wolf wolf) {
        super(Component.literal("Dog Behavior"));

        this.dog = wolf;
        this.dogEntityId = wolf.getId();
        this.hasBed = wolf.getAttached(ModAttachments.DOG_BED_POS) != null;
        this.selectedState = wolf.getAttachedOrElse(ModAttachments.DOG_STATE, DogBehaviorState.FOLLOW);
        this.followDistance = wolf.getAttachedOrElse(ModAttachments.FOLLOW_DISTANCE, 3);
        this.guardRadius = wolf.getAttachedOrElse(ModAttachments.GUARD_RADIUS, 8);
        this.relaxRadius = wolf.getAttachedOrElse(ModAttachments.RELAX_RADIUS, 16);
    }

    @Override
    protected void init() {
        this.left = (this.width - PANEL_WIDTH) / 2;
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

        bed.active = this.hasBed && this.selectedState != DogBehaviorState.RETURN_TO_BED;

        if (!this.hasBed) {
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

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        this.mouseXPos = mouseX;
        this.mouseYPos = mouseY;

        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND,
                this.left, this.top, 0.0F, (float) TEXTURE_V,
                PANEL_WIDTH, PANEL_HEIGHT, 256, 256);

        InventoryScreen.extractEntityInInventoryFollowsMouse(graphics,
                this.left + 10, this.top + 8,
                this.left + 108, this.top + 77,
                60, 0.0625F, this.mouseXPos, this.mouseYPos, this.dog);

        super.extractRenderState(graphics, mouseX, mouseY, a);

        graphics.text(this.font, this.title,
                this.width / 2 - this.font.width(this.title) / 2, this.top - 15, 0xFFFFFFFF);
    }

    private void select(DogBehaviorState state) {
        this.selectedState = state;
        this.rebuildWidgets();
    }

    private void apply() {
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

    private static class IntSlider extends AbstractSliderButton {
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