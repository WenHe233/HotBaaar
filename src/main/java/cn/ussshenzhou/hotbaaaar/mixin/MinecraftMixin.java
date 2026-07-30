package cn.ussshenzhou.hotbaaaar.mixin;

import cn.ussshenzhou.hotbaaaar.client.HotbaaaarClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Uses the presence of any GUI as the canonical-inventory boundary. No key binding, screen class or
 * container type is enumerated; the client state resolves capabilities from the active menu.
 */
@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Shadow
    public Screen screen;

    @Inject(method = "setScreen", at = @At("TAIL"))
    private void hotbaaaar$afterSetScreen(Screen newScreen, CallbackInfo ci) {
        HotbaaaarClient.reconcileScreenState(this.screen != null);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void hotbaaaar$afterTick(CallbackInfo ci) {
        HotbaaaarClient.reconcileScreenState(this.screen != null);
    }
}
