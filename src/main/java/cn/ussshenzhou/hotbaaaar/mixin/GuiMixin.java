package cn.ussshenzhou.hotbaaaar.mixin;

import cn.ussshenzhou.hotbaaaar.client.HotbaaaarClient;
import cn.ussshenzhou.hotbaaaar.util.Util;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
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
 * Renders the extended hotbar on the 26.2 extract/sprite API ({@code Hud.extractItemHotbar} +
 * {@code GuiGraphicsExtractor.blitSprite(RenderPipelines.GUI_TEXTURED, ...)}): up to four 9-slot rows
 * as one wide strip, items drawn at fixed logical positions (see {@link HotbaaaarClient}).
 *
 * @author USS_Shenzhou
 */
@Mixin(Hud.class)
public abstract class GuiMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    protected abstract Player getCameraPlayer();

    @Shadow
    protected abstract void extractSlot(GuiGraphicsExtractor graphics, int x, int y, DeltaTracker deltaTracker, Player player, ItemStack itemStack, int seed);

    @Inject(method = "extractItemHotbar", at = @At("HEAD"), cancellable = true)
    private void hotbaaaar$extractItemHotbar(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        Player player = this.getCameraPlayer();
        if (player == null) {
            return;
        }
        HotbaaaarClient.tickSanity();
        Inventory inv = player.getInventory();
        int rows = HotbaaaarClient.getRows();

        ItemStack offhand = player.getOffhandItem();
        HumanoidArm offhandArm = player.getMainArm().getOpposite();

        final int oneHotbar = Util.HOTBAR_UNIT_LENGTH;
        final int half = 91;
        final int height = Util.HOTBAR_UNIT_HEIGHT;
        int center = graphics.guiWidth() / 2;
        int x0 = center - rows * half;
        int x1 = x0 + rows * oneHotbar;

        for (int i = 0; i < rows; i++) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, Util.HOTBAR_SPRITE, x0 + i * oneHotbar, graphics.guiHeight() - height, oneHotbar, height);
        }

        int logicalSelected = Mth.clamp(HotbaaaarClient.getActiveLogicalRow(), 0, rows - 1) * 9 + inv.getSelectedSlot();
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, Util.HOTBAR_SELECTION_SPRITE, x0 - 1 + logicalSelected * 20 + (logicalSelected / 9 * 2), graphics.guiHeight() - height - 1, 24, 23);

        if (!offhand.isEmpty()) {
            if (offhandArm == HumanoidArm.LEFT) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, Util.HOTBAR_OFFHAND_LEFT_SPRITE, x0 - 29, graphics.guiHeight() - 23, 29, 24);
            } else {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, Util.HOTBAR_OFFHAND_RIGHT_SPRITE, x1, graphics.guiHeight() - 23, 29, 24);
            }
        }

        int seed = 1;
        for (int i = 0; i < rows * 9; i++) {
            int logicalRow = i / 9;
            int col = i % 9;
            int physicalSlot = HotbaaaarClient.physicalRowOfLogical(logicalRow) * 9 + col;
            int x = x0 + i * 20 + 3 + (i / 9 * 2);
            int y = graphics.guiHeight() - 16 - 3;
            this.extractSlot(graphics, x, y, deltaTracker, player, inv.getItem(physicalSlot), seed++);
        }

        if (!offhand.isEmpty()) {
            int y = graphics.guiHeight() - 16 - 3;
            if (offhandArm == HumanoidArm.LEFT) {
                this.extractSlot(graphics, x0 - 26, y, deltaTracker, player, offhand, seed++);
            } else {
                this.extractSlot(graphics, x1 + 10, y, deltaTracker, player, offhand, seed++);
            }
        }

        if (this.minecraft.options.attackIndicator().get() == AttackIndicatorStatus.HOTBAR) {
            float scale = this.minecraft.player.getAttackStrengthScale(0.0F);
            if (scale < 1.0F) {
                int y = graphics.guiHeight() - 20;
                int x = (offhandArm == HumanoidArm.RIGHT) ? x0 - 22 : x1 + 6;
                int progress = (int) (scale * 19.0F);
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, Util.HOTBAR_ATTACK_INDICATOR_BACKGROUND_SPRITE, x, y, 18, 18);
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, Util.HOTBAR_ATTACK_INDICATOR_PROGRESS_SPRITE, 18, 18, 0, 18 - progress, x, y + 18 - progress, 18, progress);
            }
        }

        ci.cancel();
    }
}
