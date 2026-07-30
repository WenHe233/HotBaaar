package cn.ussshenzhou.hotbaaaar.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

/**
 * Client-only state and row-swap logic for the extended hotbar.
 *
 * <p>Whenever any GUI is open, the physical inventory is restored to canonical order. This is
 * driven by screen state rather than keys or screen classes. For foreign/modded menus, player
 * inventory slots are resolved from the active menu by inventory identity and inventory index.
 */
public final class HotbaaaarClient {

    private static final int ROW = 9;
    private static final RowMapping ROW_MAPPING = new RowMapping();

    private static Object lastConnection;
    private static boolean canonicalForScreen;
    private static int savedActiveRow;

    private HotbaaaarClient() {
    }

    public static int getActiveLogicalRow() {
        return ROW_MAPPING.activeLogicalRow();
    }

    public static int physicalRowOfLogical(int logicalRow) {
        return ROW_MAPPING.physicalOfLogical(logicalRow);
    }

    public static int getRows() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getWindow() == null) {
            return 1;
        }
        return Mth.clamp(mc.getWindow().getGuiScaledWidth() / 182, 1, RowMapping.MAX_ROWS);
    }

    /**
     * Reconcile the logical state with the current connection, GUI state and available row count.
     * Called immediately after screen changes and once per client tick; failed safe restores are
     * therefore retried after modded menus finish initializing their slots.
     */
    public static void reconcileScreenState(boolean screenOpen) {
        Minecraft mc = Minecraft.getInstance();
        Object connection = mc.getConnection();
        if (connection != lastConnection) {
            lastConnection = connection;
            ROW_MAPPING.resetIdentity();
            canonicalForScreen = false;
            savedActiveRow = 0;
        }

        if (mc.player == null || mc.gameMode == null) {
            return;
        }

        if (screenOpen) {
            if (!canonicalForScreen) {
                int rowToResume = ROW_MAPPING.activeLogicalRow();
                if (restoreCanonical()) {
                    savedActiveRow = rowToResume;
                    canonicalForScreen = true;
                }
            }
            return;
        }

        if (canonicalForScreen) {
            if (savedActiveRow > 0 && savedActiveRow < getRows()) {
                if (!activateLogicalRow(savedActiveRow)) {
                    return;
                }
            }
            canonicalForScreen = false;
            savedActiveRow = 0;
        }

        // A narrower window can make the active logical row unreachable. Restore physically before
        // accepting identity; never discard a non-identity mapping by assignment alone.
        if (ROW_MAPPING.activeLogicalRow() >= getRows()) {
            restoreCanonical();
        }
    }

    /** Kept as the render/scroll entry point used by all version variants. */
    public static void tickSanity() {
        Minecraft mc = Minecraft.getInstance();
        reconcileScreenState(mc.screen != null);
    }

    /** Restore the physical inventory to identity, returning false without side effects if unsafe. */
    public static boolean restoreCanonical() {
        int[] plan = ROW_MAPPING.planRestore();
        if (plan.length == 0) {
            return true;
        }
        ResolvedSwapPlan resolved = resolveSwapPlan(plan);
        if (resolved == null) {
            return false;
        }
        executeSwapPlan(resolved);
        return ROW_MAPPING.isIdentity();
    }

    public static void onScroll(double direction) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null || canonicalForScreen || mc.player == null) {
            return;
        }
        int dir = (int) Math.signum(direction);
        if (dir == 0) {
            return;
        }
        tickSanity();
        if (canonicalForScreen || mc.screen != null) {
            return;
        }

        Inventory inventory = mc.player.getInventory();
        int newSelected = inventory.selected - dir;
        if (newSelected < 0) {
            setSelected(inventory, flipRow(-1) ? ROW - 1 : 0);
        } else if (newSelected >= ROW) {
            setSelected(inventory, flipRow(1) ? 0 : ROW - 1);
        } else {
            setSelected(inventory, newSelected);
        }
    }

    private static void setSelected(Inventory inventory, int slot) {
        inventory.selected = slot;
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null) {
            mc.getConnection().send(new ServerboundSetCarriedItemPacket(slot));
        }
    }

    private static boolean flipRow(int delta) {
        int target = ROW_MAPPING.activeLogicalRow() + delta;
        return target >= 0 && target < getRows() && activateLogicalRow(target);
    }

    private static boolean activateLogicalRow(int target) {
        if (target == ROW_MAPPING.activeLogicalRow()) {
            return true;
        }
        int physical = ROW_MAPPING.physicalOfLogical(target);
        ResolvedSwapPlan resolved = resolveSwapPlan(new int[]{physical});
        if (resolved == null) {
            return false;
        }
        executeSwapPlan(resolved);
        return ROW_MAPPING.activeLogicalRow() == target;
    }

    /**
     * Resolve every required physical inventory cell before performing a single click. Exact
     * inventory identity and a unique inventory index are required; no positional heuristics.
     */
    private static ResolvedSwapPlan resolveSwapPlan(int[] physicalRows) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.gameMode == null) {
            return null;
        }
        AbstractContainerMenu menu = player.containerMenu;
        Inventory inventory = player.getInventory();
        boolean[] required = new boolean[RowMapping.MAX_ROWS];

        for (int physical : physicalRows) {
            if (physical <= 0 || physical >= RowMapping.MAX_ROWS) {
                return null;
            }
            required[physical] = true;
        }

        MenuSlotLookup lookup = new MenuSlotLookup(required);
        for (Slot slot : menu.slots) {
            if (slot.container != inventory) {
                continue;
            }
            lookup.record(slot.getContainerSlot(), slot.index);
        }

        int[][] menuSlots = lookup.finish();
        if (menuSlots == null) {
            return null;
        }
        return new ResolvedSwapPlan(menu, player, physicalRows, menuSlots);
    }

    private static void executeSwapPlan(ResolvedSwapPlan plan) {
        Minecraft mc = Minecraft.getInstance();
        for (int physical : plan.physicalRows) {
            for (int column = 0; column < ROW; column++) {
                mc.gameMode.handleInventoryMouseClick(
                        plan.menu.containerId,
                        plan.menuSlots[physical][column],
                        column,
                        ClickType.SWAP,
                        plan.player
                );
            }
            ROW_MAPPING.applyHotbarSwap(physical);
        }
    }

    private static final class ResolvedSwapPlan {
        private final AbstractContainerMenu menu;
        private final Player player;
        private final int[] physicalRows;
        private final int[][] menuSlots;

        private ResolvedSwapPlan(AbstractContainerMenu menu, Player player, int[] physicalRows, int[][] menuSlots) {
            this.menu = menu;
            this.player = player;
            this.physicalRows = physicalRows;
            this.menuSlots = menuSlots;
        }
    }
}
