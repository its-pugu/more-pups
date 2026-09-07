package pugu.pups;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class DogWhistleScreen extends Screen {
    private static final int PER_PAGE = 6;

    private final List<DogRecord> dogs;

    private DogRecord selected;
    private int page;
    private Button okButton;

    public DogWhistleScreen(List<DogRecord> dogs) {
        super(Component.literal("Summon your pup."));
        this.dogs = dogs;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = 40;

        int first = this.page * PER_PAGE;
        int last = Math.min(first + PER_PAGE, this.dogs.size());

        for (int i = first; i < last; i++) {
            DogRecord dog = this.dogs.get(i);
            boolean sameDimension = this.minecraft.level != null
                    && this.minecraft.level.dimension().equals(dog.dimension());

            Button entry = Button.builder(Component.literal(displayName(dog)), button -> this.select(dog))
                    .bounds(centerX - 100, startY + (i - first) * 24, 200, 20).build();

            entry.active = sameDimension && dog != this.selected;

            if (!sameDimension) {
                entry.setTooltip(Tooltip.create(
                        Component.literal("In another dimension").withStyle(ChatFormatting.RED)));
            }

            this.addRenderableWidget(entry);
        }

        int pagerY = startY + PER_PAGE * 24 + 8;

        if (this.dogs.size() > PER_PAGE) {
            Button previous = this.addRenderableWidget(Button.builder(Component.literal("< Prev"), button -> this.turnPage(-1))
                    .bounds(centerX - 100, pagerY, 60, 20).build());

            Button next = this.addRenderableWidget(Button.builder(Component.literal("Next >"), button -> this.turnPage(1))
                    .bounds(centerX + 40, pagerY, 60, 20).build());

            previous.active = this.page > 0;
            next.active = last < this.dogs.size();
        }

        this.okButton = this.addRenderableWidget(Button.builder(Component.literal("OK"), button -> this.summon())
                .bounds(centerX - 105, this.height - 40, 100, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Cancel"), button -> this.onClose())
                .bounds(centerX + 5, this.height - 40, 100, 20).build());

        this.okButton.active = this.selected != null;
    }

    private void turnPage(int delta) {
        this.page += delta;
        this.rebuildWidgets();
    }

    private static String displayName(DogRecord dog) {
        if (!dog.name().isEmpty()) {
            return dog.name();
        }

        if (dog.breed().isEmpty()) {
            return "Wolf";
        }

        String breed = dog.breed().get().getSerializedName();
        return Character.toUpperCase(breed.charAt(0)) + breed.substring(1);
    }

    private void select(DogRecord dog) {
        this.selected = dog;
        this.rebuildWidgets();
    }

    private void summon() {
        if (this.selected != null) {
            ClientPlayNetworking.send(new SummonDogPayload(this.selected.dogId()));
        }

        this.onClose();
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(null);
    }
}