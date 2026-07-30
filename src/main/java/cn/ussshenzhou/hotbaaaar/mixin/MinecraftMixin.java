package cn.ussshenzhou.hotbaaaar.mixin;

import cn.ussshenzhou.hotbaaaar.client.HotbaaaarClient;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Retries GUI/inventory reconciliation once per client tick after modded menus have finished
 * initializing their slots.
 */
@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void hotbaaaar$afterTick(CallbackInfo ci) {
        HotbaaaarClient.tickSanity();
    }
}
