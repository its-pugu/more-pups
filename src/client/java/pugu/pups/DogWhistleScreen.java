package pugu.pups;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.wolf.Wolf;

import java.util.List;

public class DogWhistleScreen extends Screen {
    private static final Identifier BACKGROUND =
            Identifier.fromNamespaceAndPath(MorePups.MOD_ID, "textures/gui/whistle_screen.png");
    private static final Identifier SCROLLER_SPRITE =
            Identifier.withDefaultNamespace("container/villager/scroller");
    private static final Identifier SCROLLER_DISABLED_SPRITE =
            Identifier.withDefaultNamespace("container/villager/scroller_disabled");

    private static final int PANEL_WIDTH = 216;
    private static final int PANEL_HEIGHT = 142;
    private static final int TEXTURE_V = 14;

    private static final int LIST_X = 115;
    private static final int LIST_Y = 8;
    private static final int LIST_WIDTH = 83;
    private static final int ROW_HEIGHT = 20;
    private static final int VISIBLE_ROWS = 6;

    private static final int SCROLL_X = 199;
    private static final int SCROLL_Y = 8;
    private static final int SCROLL_HEIGHT = 124;
    private static final int SCROLLER_HEIGHT = 27;

    private static final int PUP_SCALE = 60;
    private static final int WOLF_SCALE = 45;

    private final List<DogRecord> dogs;
    private final Button[] rowButtons = new Button[VISIBLE_ROWS];

    private DogRecord selected;
    private PupEntity previewPup;
    private Wolf previewWolf;
    private Button summonButton;

    private int scrollOff;
    private boolean isDragging;

    private int left;
    private int top;
    private float mouseXPos;
    private float mouseYPos;

    public DogWhistleScreen(List<DogRecord> dogs) {
        super(Component.literal("Summon your pup."));
        this.dogs = dogs;
    }

    @Override
    protected void init() {
        this.left = (this.width - PANEL_WIDTH) / 2;
        this.top = (this.height - PANEL_HEIGHT) / 2;

        for (int i = 0; i < VISIBLE_ROWS; i++) {
            int index = i;

            this.rowButtons[i] = this.addRenderableWidget(Button.builder(CommonComponents.EMPTY,
                            button -> this.selectRow(index))
                    .bounds(this.left + LIST_X, this.top + LIST_Y + i * ROW_HEIGHT, LIST_WIDTH, ROW_HEIGHT)
                    .build());
        }

        this.summonButton = this.addRenderableWidget(Button.builder(Component.literal("\u2714"),
                        button -> this.summon())
                .bounds(this.left + LIST_X + LIST_WIDTH - 18, this.top + LIST_Y, 18, ROW_HEIGHT).build());

        this.summonButton.setTooltip(Tooltip.create(Component.literal("Summon Dog?")));
        this.summonButton.visible = false;

        this.createPreviews();
        this.refreshRows();
    }

    private void createPreviews() {
        if (this.minecraft.level == null) {
            return;
        }

        if (this.previewPup == null) {
            this.previewPup = ModEntityTypes.PUP.create(this.minecraft.level, EntitySpawnReason.LOAD);

            if (this.previewPup != null) {
                this.previewPup.setId(-1);
                this.previewPup.setTame(true, false);
            }
        }

        if (this.previewWolf == null) {
            this.previewWolf = EntityTypes.WOLF.create(this.minecraft.level, EntitySpawnReason.LOAD);

            if (this.previewWolf != null) {
                this.previewWolf.setId(-2);
                this.previewWolf.setTame(true, false);
            }
        }
    }

    private Wolf previewFor(DogRecord record) {
        return record.breed().isPresent() ? this.previewPup : this.previewWolf;
    }

    private int scaleFor(DogRecord record) {
        return record.breed().isPresent() ? PUP_SCALE : WOLF_SCALE;
    }

    private void selectRow(int row) {
        int index = row + this.scrollOff;

        if (index < this.dogs.size()) {
            this.selected = this.dogs.get(index);

            Wolf preview = this.previewFor(this.selected);

            if (preview != null) {
                if (preview instanceof PupEntity pup && this.selected.breed().isPresent()) {
                    pup.setBreed(this.selected.breed().get());
                }

                preview.setComponent(DataComponents.WOLF_COLLAR, this.selected.collar());

                this.selected.variant()
                        .flatMap(key -> this.minecraft.level.registryAccess()
                                .lookupOrThrow(Registries.WOLF_VARIANT).get(key))
                        .ifPresent(holder -> preview.setComponent(DataComponents.WOLF_VARIANT, holder));
            }

            this.refreshRows();
        }
    }

    private void refreshRows() {
        for (int i = 0; i < VISIBLE_ROWS; i++) {
            this.rowButtons[i].setWidth(LIST_WIDTH);

            int index = i + this.scrollOff;
            boolean present = index < this.dogs.size();

            this.rowButtons[i].visible = present;

            if (present) {
                DogRecord dog = this.dogs.get(index);
                this.rowButtons[i].setMessage(Component.literal(displayName(dog)));
                this.rowButtons[i].active = !dog.equals(this.selected);
            }
        }

        int selectedRow = this.selected == null ? -1 : this.dogs.indexOf(this.selected) - this.scrollOff;

        if (selectedRow >= 0 && selectedRow < VISIBLE_ROWS) {
            this.summonButton.visible = true;
            this.summonButton.setY(this.top + LIST_Y + selectedRow * ROW_HEIGHT);
            this.rowButtons[selectedRow].setWidth(LIST_WIDTH - 20);
        } else {
            this.summonButton.visible = false;
        }
    }

    private void summon() {
        if (this.selected != null) {
            ClientPlayNetworking.send(new SummonDogPayload(this.selected.dogId()));
        }

        this.onClose();
    }

    private boolean canScroll() {
        return this.dogs.size() > VISIBLE_ROWS;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        if (this.canScroll()) {
            this.scrollOff = Mth.clamp((int) (this.scrollOff - scrollY), 0, this.dogs.size() - VISIBLE_ROWS);
            this.refreshRows();
        }

        return true;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (this.canScroll()
                && event.x() > this.left + SCROLL_X && event.x() < this.left + SCROLL_X + 6
                && event.y() > this.top + SCROLL_Y && event.y() <= this.top + SCROLL_Y + SCROLL_HEIGHT) {
            this.isDragging = true;
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (this.isDragging) {
            int maxScroll = this.dogs.size() - VISIBLE_ROWS;
            float fraction = ((float) event.y() - (this.top + SCROLL_Y) - SCROLLER_HEIGHT / 2.0F)
                    / (SCROLL_HEIGHT - SCROLLER_HEIGHT);

            this.scrollOff = Mth.clamp((int) (fraction * maxScroll + 0.5F), 0, maxScroll);
            this.refreshRows();
            return true;
        }

        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        this.isDragging = false;
        return super.mouseReleased(event);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        this.mouseXPos = mouseX;
        this.mouseYPos = mouseY;

        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND,
                this.left, this.top, 0.0F, (float) TEXTURE_V,
                PANEL_WIDTH, PANEL_HEIGHT, 256, 256);

        if (this.selected != null) {
            Wolf preview = this.previewFor(this.selected);

            if (preview != null) {
                InventoryScreen.extractEntityInInventoryFollowsMouse(graphics,
                        this.left + 11, this.top + 8,
                        this.left + 107, this.top + 75,
                        this.scaleFor(this.selected), 0.0625F,
                        this.mouseXPos, this.mouseYPos, preview);
            }
        }

        this.extractScroller(graphics);

        super.extractRenderState(graphics, mouseX, mouseY, a);

        graphics.text(this.font, Component.literal("Stats:"),
                this.left + 11, this.top + 81, 0xFFFFFFFF);
        graphics.text(this.font, Component.literal("Coming soon..."),
                this.left + 11, this.top + 93, 0xFFAAAAAA);

        graphics.text(this.font, this.title,
                this.width / 2 - this.font.width(this.title) / 2, this.top - 15, 0xFFFFFFFF);
    }

    private void extractScroller(GuiGraphicsExtractor graphics) {
        if (!this.canScroll()) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER_DISABLED_SPRITE,
                    this.left + SCROLL_X, this.top + SCROLL_Y, 6, SCROLLER_HEIGHT);
            return;
        }

        int maxScroll = this.dogs.size() - VISIBLE_ROWS;
        int offset = (SCROLL_HEIGHT - SCROLLER_HEIGHT) * this.scrollOff / maxScroll;

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER_SPRITE,
                this.left + SCROLL_X, this.top + SCROLL_Y + offset, 6, SCROLLER_HEIGHT);
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

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(null);
    }



}