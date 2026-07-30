package cn.ussshenzhou.hotbaaaar.mixin;

import cn.ussshenzhou.hotbaaaar.client.HotbaaaarClient;
import cn.ussshenzhou.hotbaaaar.util.Util;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Renders the extended hotbar on the 1.21.x sprite API: up to four 9-slot rows as one wide strip,
 * items drawn at fixed logical positions (see {@link HotbaaaarClient}). Faithfully reproduces the
 * vanilla hotbar (background, selection frame, offhand slot, attack indicator) generalised to N rows.
 *
 * @author USS_Shenzhou
 */
@Mixin(Gui.class)
public abstract class GuiMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    protected abstract Player getCameraPlayer();

    @Shadow
    protected abstract void renderSlot(GuiGraphics guiGraphics, int x, int y, DeltaTracker deltaTracker, Player player, ItemStack stack, int seed);

    @Inject(method = "renderItemHotbar", at = @At("HEAD"), cancellable = true)
    private void hotbaaaar$renderHotbar(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        Player player = this.getCameraPlayer();
        if (player == null) {
            return;
        }
        HotbaaaarClient.tickSanity();
        Inventory inv = player.getInventory();
        int rows = HotbaaaarClient.getRows();

        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();
        ItemStack offhand = player.getOffhandItem();
        HumanoidArm offhandArm = player.getMainArm().getOpposite();

        final int oneHotbar = Util.HOTBAR_UNIT_LENGTH;
        final int half = 91;
        final int height = Util.HOTBAR_UNIT_HEIGHT;
        int center = screenWidth / 2;
        int x0 = center - rows * half;
        int x1 = x0 + rows * oneHotbar;

        // backgrounds
        for (int i = 0; i < rows; i++) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, Util.HOTBAR_SPRITE, x0 + i * oneHotbar, screenHeight - height, oneHotbar, height);
        }

        // selection frame at the logical selected slot
        int logicalSelected = Mth.clamp(HotbaaaarClient.getActiveLogicalRow(), 0, rows - 1) * 9 + inv.getSelectedSlot();
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, Util.HOTBAR_SELECTION_SPRITE, x0 - 1 + logicalSelected * 20 + (logicalSelected / 9 * 2), screenHeight - height - 1, 24, 23);

        // offhand frame
        if (!offhand.isEmpty()) {
            if (offhandArm == HumanoidArm.LEFT) {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, Util.HOTBAR_OFFHAND_LEFT_SPRITE, x0 - 29, screenHeight - 23, 29, 24);
            } else {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, Util.HOTBAR_OFFHAND_RIGHT_SPRITE, x1, screenHeight - 23, 29, 24);
            }
        }

        // items, read from the physical slot that currently holds each logical position
        int seed = 1;
        for (int i = 0; i < rows * 9; i++) {
            int logicalRow = i / 9;
            int col = i % 9;
            int physicalSlot = HotbaaaarClient.physicalRowOfLogical(logicalRow) * 9 + col;
            int x = x0 + i * 20 + 3 + (i / 9 * 2);
            int y = screenHeight - 16 - 3;
            this.renderSlot(guiGraphics, x, y, deltaTracker, player, inv.getItem(physicalSlot), seed++);
        }

        // offhand item
        if (!offhand.isEmpty()) {
            int y = screenHeight - 16 - 3;
            if (offhandArm == HumanoidArm.LEFT) {
                this.renderSlot(guiGraphics, x0 - 26, y, deltaTracker, player, offhand, seed++);
            } else {
                this.renderSlot(guiGraphics, x1 + 10, y, deltaTracker, player, offhand, seed++);
            }
        }

        // attack indicator
        if (this.minecraft.options.attackIndicator().get() == AttackIndicatorStatus.HOTBAR) {
            float scale = this.minecraft.player.getAttackStrengthScale(0.0F);
            if (scale < 1.0F) {
                int y = screenHeight - 20;
                int x = (offhandArm == HumanoidArm.RIGHT) ? x0 - 22 : x1 + 6;
                int progress = (int) (scale * 19.0F);
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, Util.HOTBAR_ATTACK_INDICATOR_BACKGROUND_SPRITE, x, y, 18, 18);
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, Util.HOTBAR_ATTACK_INDICATOR_PROGRESS_SPRITE, 18, 18, 0, 18 - progress, x, y + 18 - progress, 18, progress);
            }
        }

        ci.cancel();
    }
}
