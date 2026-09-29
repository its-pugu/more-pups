package pugu.pups;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.wolf.Wolf;

import java.util.List;

public class DogSkillScreen extends AbstractDogScreen {
    private static final int NODE_SIZE = 22;
    private static final int NODE_SPACING = 28;
    private static final int FIRST_NODE_Y = 159;

    private static final int ICONS_WIDTH = 132;
    private static final int ICONS_HEIGHT = 66;

    private static final int ROW_AVAILABLE = 0;
    private static final int ROW_LOCKED = 1;
    private static final int ROW_BOUGHT = 2;

    private static final Identifier SKILL_BACKGROUND =
            Identifier.fromNamespaceAndPath(MorePups.MOD_ID, "textures/gui/skill_tree.png");
    private static final Identifier ICONS =
            Identifier.fromNamespaceAndPath(MorePups.MOD_ID, "textures/gui/skill_icons.png");

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
        List<DogSkill> unlocked = this.dog.getAttachedOrElse(ModAttachments.UNLOCKED_SKILLS, List.of());

        for (DogSkill skill : DogSkill.values()) {
            DogSkill parent = skill.parent();

            if (parent == null || !DogSkill.isVisible(skill, unlocked)) {
                continue;
            }

            this.drawLink(graphics, sideLeft, sideTop, parent, skill);
        }

        for (DogSkill skill : DogSkill.values()) {
            if (!DogSkill.isVisible(skill, unlocked)) {
                continue;
            }

            int x = sideLeft + skill.x();
            int y = this.nodeY(sideTop, skill);

            int row = unlocked.contains(skill)
                    ? ROW_BOUGHT
                    : (this.canBuy(skill, unlocked) ? ROW_AVAILABLE : ROW_LOCKED);

            graphics.blit(RenderPipelines.GUI_TEXTURED, ICONS,
                    x, y, (float) (skill.column() * NODE_SIZE), (float) (row * NODE_SIZE),
                    NODE_SIZE, NODE_SIZE, ICONS_WIDTH, ICONS_HEIGHT);

            if (this.isOver(x, y, NODE_SIZE, NODE_SIZE)) {
                this.drawBorder(graphics, x, y);
                this.hoverTitle = skill.title();
                this.hoverDescription = skill.description();
                this.hoverEffect = skill.effect();
            }
        }

        int points = this.dog.getAttachedOrElse(ModAttachments.SKILL_POINTS, 0);

        graphics.text(this.font, Component.literal("Points: " + points),
                sideLeft + 3, sideTop + 8, 0xFFFFFFFF);

        boolean atBranch = unlocked.contains(DogSkill.RESILIENCE)
                && unlocked.stream().noneMatch(DogSkill::isBranch);

        if (atBranch) {
            Component prompt = Component.literal("Choose your path");
            int promptY = this.nodeY(sideTop, DogSkill.FIGHTER) - 14;

            graphics.text(this.font, prompt,
                    sideLeft + SIDE_WIDTH / 2 - this.font.width(prompt) / 2, promptY, 0xFFFFFFFF);
        }
    }

    private void drawBorder(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.fill(x - 1, y - 1, x + NODE_SIZE + 1, y, 0xFFFFFFFF);
        graphics.fill(x - 1, y + NODE_SIZE, x + NODE_SIZE + 1, y + NODE_SIZE + 1, 0xFFFFFFFF);
        graphics.fill(x - 1, y, x, y + NODE_SIZE, 0xFFFFFFFF);
        graphics.fill(x + NODE_SIZE, y, x + NODE_SIZE + 1, y + NODE_SIZE, 0xFFFFFFFF);
    }

    private boolean canBuy(DogSkill skill, List<DogSkill> unlocked) {
        if (unlocked.contains(skill)) {
            return false;
        }

        DogSkill parent = skill.parent();

        if (parent != null && !unlocked.contains(parent)) {
            return false;
        }

        if (skill.isBranch() && unlocked.stream().anyMatch(DogSkill::isBranch)) {
            return false;
        }

        return this.dog.getAttachedOrElse(ModAttachments.SKILL_POINTS, 0) > 0;
    }

    private int nodeY(int sideTop, DogSkill skill) {
        return sideTop + FIRST_NODE_Y - skill.tier() * NODE_SPACING;
    }

    private void drawLink(GuiGraphicsExtractor graphics, int sideLeft, int sideTop,
                          DogSkill parent, DogSkill child) {
        int parentX = sideLeft + parent.x() + NODE_SIZE / 2;
        int childX = sideLeft + child.x() + NODE_SIZE / 2;
        int parentY = this.nodeY(sideTop, parent);
        int childY = this.nodeY(sideTop, child) + NODE_SIZE;
        int midY = (parentY + childY) / 2;

        this.drawLine(graphics, parentX, midY, parentX, parentY);
        this.drawLine(graphics, Math.min(parentX, childX), midY, Math.max(parentX, childX), midY);
        this.drawLine(graphics, childX, childY, childX, midY);
    }

    private void drawLine(GuiGraphicsExtractor graphics, int x1, int y1, int x2, int y2) {
        int left = Math.min(x1, x2);
        int top = Math.min(y1, y2);
        int right = Math.max(x1, x2);
        int bottom = Math.max(y1, y2);

        graphics.fill(left, top, right + 1, bottom + 1, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        List<DogSkill> unlocked = this.dog.getAttachedOrElse(ModAttachments.UNLOCKED_SKILLS, List.of());
        int sideLeft = this.sideLeft();
        int sideTop = this.sideTop();

        for (DogSkill skill : DogSkill.values()) {
            if (!this.canBuy(skill, unlocked)) {
                continue;
            }

            int x = sideLeft + skill.x();
            int y = this.nodeY(sideTop, skill);

            if (event.x() >= x && event.x() < x + NODE_SIZE
                    && event.y() >= y && event.y() < y + NODE_SIZE) {
                ClientPlayNetworking.send(new BuySkillPayload(this.dog.getId(), skill));
                return true;
            }
        }

        return super.mouseClicked(event, doubleClick);
    }
}