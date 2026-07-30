package cn.ussshenzhou.hotbaaaar.mixin;

import cn.ussshenzhou.hotbaaaar.client.HotbaaaarClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Redirects the vanilla wheel-selection helper into the extended row state machine.
 */
@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @Redirect(
            method = "onScroll",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/ScrollWheelHandler;getNextScrollWheelSelection(DII)I")
    )
    private int hotbaaaar$scroll(double wheel, int currentSelected, int limit) {
        HotbaaaarClient.onScroll(wheel);
        return Minecraft.getInstance().player.getInventory().getSelectedSlot();
    }
}
