package cn.ussshenzhou.hotbaaaar.client;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.util.math.MathHelper;

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

    private static final GuiRestoreState GUI_STATE = new GuiRestoreState();
    private static final GuiRestoreState.InventoryActions INVENTORY_ACTIONS =
            new GuiRestoreState.InventoryActions() {
                @Override
                public int activeRow() {
                    return ROW_MAPPING.activeLogicalRow();
                }

                @Override
                public void resetIdentity() {
                    ROW_MAPPING.resetIdentity();
                }

                @Override
                public boolean restoreCanonical() {
                    return HotbaaaarClient.restoreCanonical();
                }

                @Override
                public boolean activateLogicalRow(int row) {
                    return HotbaaaarClient.activateLogicalRow(row);
                }
            };

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
        return MathHelper.clamp(mc.getWindow().getGuiScaledWidth() / 182, 1, RowMapping.MAX_ROWS);
    }

    /**
     * Reconcile the logical state with the current connection, GUI state and available row count.
     * Called immediately after screen changes and once per client tick; failed safe restores are
     * therefore retried after modded menus finish initializing their slots.
     */
    public static void reconcileScreenState(boolean screenOpen) {
        Minecraft mc = Minecraft.getInstance();
        GUI_STATE.reconcile(
                mc.getConnection(),
                mc.player != null && mc.gameMode != null,
                screenOpen,
                getRows(),
                INVENTORY_ACTIONS
        );
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
        if (mc.screen != null || GUI_STATE.isCanonicalForScreen() || mc.player == null) {
            return;
        }
        int dir = (int) Math.signum(direction);
        if (dir == 0) {
            return;
        }
        tickSanity();
        if (GUI_STATE.isCanonicalForScreen() || mc.screen != null) {
            return;
        }

        PlayerInventory inventory = mc.player.inventory;
        int newSelected = inventory.selected - dir;
        if (newSelected < 0) {
            setSelected(inventory, flipRow(-1) ? ROW - 1 : 0);
        } else if (newSelected >= ROW) {
            setSelected(inventory, flipRow(1) ? 0 : ROW - 1);
        } else {
            setSelected(inventory, newSelected);
        }
    }

    private static void setSelected(PlayerInventory inventory, int slot) {
        inventory.selected = slot;
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null) {
            mc.getConnection().send(new CHeldItemChangePacket(slot));
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
        PlayerEntity player = mc.player;
        if (player == null || mc.gameMode == null) {
            return null;
        }
        Container menu = player.containerMenu;
        PlayerInventory inventory = player.inventory;
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
            lookup.record(slot.getSlotIndex(), slot.index);
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
        private final Container menu;
        private final PlayerEntity player;
        private final int[] physicalRows;
        private final int[][] menuSlots;

        private ResolvedSwapPlan(Container menu, PlayerEntity player, int[] physicalRows, int[][] menuSlots) {
            this.menu = menu;
            this.player = player;
            this.physicalRows = physicalRows;
            this.menuSlots = menuSlots;
        }
    }
}
