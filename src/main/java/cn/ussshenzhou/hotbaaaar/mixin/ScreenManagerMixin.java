package cn.ussshenzhou.hotbaaaar.mixin;

import cn.ussshenzhou.hotbaaaar.client.HotbaaaarClient;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Uses the presence of any screen as the canonical-inventory boundary. Minecraft 26.2 moved
 * screen ownership from {@code Minecraft} to {@code Gui}, so the transition hook follows it.
 */
@Mixin(Gui.class)
public class ScreenManagerMixin {

    @Inject(method = "setScreen", at = @At("TAIL"))
    private void hotbaaaar$afterSetScreen(Screen newScreen, CallbackInfo ci) {
        HotbaaaarClient.reconcileScreenState(newScreen != null);
    }
}
