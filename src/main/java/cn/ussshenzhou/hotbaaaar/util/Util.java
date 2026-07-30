package cn.ussshenzhou.hotbaaaar.util;

import net.minecraft.resources.Identifier;

/**
 * 1.21.x GUI hotbar sprites and geometry constants. 1.20.5+ renders the HUD from the sprite atlas,
 * so these are sprite ids (under {@code minecraft:gui/sprites/...}), not raw texture sheets.
 *
 * @author USS_Shenzhou
 */
public class Util {

    public static final Identifier HOTBAR_SPRITE = Identifier.withDefaultNamespace("hud/hotbar");
    public static final Identifier HOTBAR_SELECTION_SPRITE = Identifier.withDefaultNamespace("hud/hotbar_selection");
    public static final Identifier HOTBAR_OFFHAND_LEFT_SPRITE = Identifier.withDefaultNamespace("hud/hotbar_offhand_left");
    public static final Identifier HOTBAR_OFFHAND_RIGHT_SPRITE = Identifier.withDefaultNamespace("hud/hotbar_offhand_right");
    public static final Identifier HOTBAR_ATTACK_INDICATOR_BACKGROUND_SPRITE = Identifier.withDefaultNamespace("hud/hotbar_attack_indicator_background");
    public static final Identifier HOTBAR_ATTACK_INDICATOR_PROGRESS_SPRITE = Identifier.withDefaultNamespace("hud/hotbar_attack_indicator_progress");

    /** Width of one vanilla hotbar in GUI pixels. */
    public static final int HOTBAR_UNIT_LENGTH = 182;
    /** Height of one vanilla hotbar in GUI pixels. */
    public static final int HOTBAR_UNIT_HEIGHT = 22;
}
