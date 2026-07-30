package cn.ussshenzhou.hotbaaaar;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/**
 * Client-only NeoForge entry point. Runtime behaviour is implemented by client mixins.
 */
@Mod(HotBaaaar.MOD_ID)
public final class HotBaaaar {
    public static final String MOD_ID = "hotbaaaar";

    public HotBaaaar(IEventBus modEventBus) {
    }
}
