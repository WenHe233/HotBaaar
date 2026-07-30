package cn.ussshenzhou.hotbaaaar.mixin;

import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 1.16.5 official mappings expose the inventory-local slot index only as a private field.
 */
@Mixin(Slot.class)
public interface SlotAccessor {

    @Accessor("slot")
    int hotbaaaar$getContainerSlot();
}
