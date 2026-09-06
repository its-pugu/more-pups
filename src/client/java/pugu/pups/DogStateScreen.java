package pugu.pups;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.wolf.Wolf;

public class DogStateScreen extends Screen {
    private static final int FOLLOW_MIN = 2;
    private static final int FOLLOW_MAX = 12;
    private static final int RADIUS_MIN = 2;
    private static final int RADIUS_MAX = 32;

    private final int dogEntityId;
    private final boolean hasBed;

    private DogBehaviorState selectedState;
    private int followDistance;
    private int guardRadius;
    private int relaxRadius;

    private Button followButton;
    private Button guardButton;
    private Button relaxButton;
    private Button bedButton;

    public DogStateScreen(Wolf wolf) {
        super(Component.literal("Dog Behavior"));

        this.dogEntityId = wolf.getId();
        this.hasBed = wolf.getAttached(ModAttachments.DOG_BED_POS) != null;
        this.selectedState = wolf.getAttachedOrElse(ModAttachments.DOG_STATE, DogBehaviorState.FOLLOW);
        this.followDistance = wolf.getAttachedOrElse(ModAttachments.FOLLOW_DISTANCE, 3);
        this.guardRadius = wolf.getAttachedOrElse(ModAttachments.GUARD_RADIUS, 8);
        this.relaxRadius = wolf.getAttachedOrElse(ModAttachments.RELAX_RADIUS, 16);
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = this.height / 2 - 85;

        this.followButton = this.addRenderableWidget(Button.builder(Component.literal("Follow"), button -> this.select(DogBehaviorState.FOLLOW))
                .bounds(centerX - 50, startY, 100, 20).build());

        this.guardButton = this.addRenderableWidget(Button.builder(Component.literal("Guard"), button -> this.select(DogBehaviorState.GUARD))
                .bounds(centerX - 50, startY + 25, 100, 20).build());

        this.relaxButton = this.addRenderableWidget(Button.builder(Component.literal("Relax"), button -> this.select(DogBehaviorState.RELAX))
                .bounds(centerX - 50, startY + 50, 100, 20).build());

        this.bedButton = this.addRenderableWidget(Button.builder(Component.literal("Return to Bed"), button -> this.select(DogBehaviorState.RETURN_TO_BED))
                .bounds(centerX - 50, startY + 75, 100, 20).build());

        if (!this.hasBed) {
            this.bedButton.setTooltip(Tooltip.create(
                    Component.literal("Dog has not claimed a bed.").withStyle(ChatFormatting.RED)));
        }

        this.addRenderableWidget(new IntSlider(centerX - 75, startY + 110, "Follow distance",
                this.followDistance, FOLLOW_MIN, FOLLOW_MAX, value -> this.followDistance = value));

        this.addRenderableWidget(new IntSlider(centerX - 75, startY + 135, "Guard radius",
                this.guardRadius, RADIUS_MIN, RADIUS_MAX, value -> this.guardRadius = value));

        this.addRenderableWidget(new IntSlider(centerX - 75, startY + 160, "Relax radius",
                this.relaxRadius, RADIUS_MIN, RADIUS_MAX, value -> this.relaxRadius = value));

        this.addRenderableWidget(Button.builder(Component.literal("Done"), button -> this.apply())
                .bounds(centerX - 50, startY + 190, 100, 20).build());

        this.refreshButtons();
    }

    private void select(DogBehaviorState state) {
        this.selectedState = state;
        this.refreshButtons();
    }

    private void refreshButtons() {
        this.followButton.active = this.selectedState != DogBehaviorState.FOLLOW;
        this.guardButton.active = this.selectedState != DogBehaviorState.GUARD;
        this.relaxButton.active = this.selectedState != DogBehaviorState.RELAX;
        this.bedButton.active = this.hasBed && this.selectedState != DogBehaviorState.RETURN_TO_BED;
    }

    private void apply() {
        ClientPlayNetworking.send(new SetDogStatePayload(this.dogEntityId, this.selectedState,
                this.followDistance, this.guardRadius, this.relaxRadius));
        this.minecraft.gui.setScreen(null);
    }

    private static class IntSlider extends AbstractSliderButton {
        private final String label;
        private final int min;
        private final int max;
        private final java.util.function.IntConsumer onChange;

        IntSlider(int x, int y, String label, int initial, int min, int max, java.util.function.IntConsumer onChange) {
            super(x, y, 150, 20, Component.empty(), (double) (initial - min) / (max - min));
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
        protected void updateMessage() {
            this.setMessage(Component.literal(this.label + ": " + this.intValue()));
        }

        @Override
        protected void applyValue() {
            this.onChange.accept(this.intValue());
        }
    }
}