package cn.ussshenzhou.hotbaaaar;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Client-only "super long hotbar" mod for Forge.
 * <p>
 * All behaviour lives in client-side mixins and
 * {@code cn.ussshenzhou.hotbaaaar.client.HotbaaaarClient}.
 * The mod does nothing on a dedicated server and is safe to connect to vanilla servers.
 *
 * @author USS_Shenzhou
 */
@Mod(HotBaaaar.MOD_ID)
public class HotBaaaar {
    public static final String MOD_ID = "hotbaaaar";

    public HotBaaaar(FMLJavaModLoadingContext context) {
    }
}
