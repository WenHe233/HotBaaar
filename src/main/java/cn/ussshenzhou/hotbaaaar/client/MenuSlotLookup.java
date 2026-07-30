package cn.ussshenzhou.hotbaaaar.client;

import java.util.Arrays;

/**
 * Pure accumulator for resolving inventory indices to active-menu slot indices.
 * The caller is responsible for filtering slots by exact inventory identity.
 */
final class MenuSlotLookup {

    private static final int ROW = 9;

    private final boolean[] required;
    private final int[][] menuSlots = new int[RowMapping.MAX_ROWS][];
    private boolean invalid;

    MenuSlotLookup(boolean[] required) {
        this.required = Arrays.copyOf(required, required.length);
        for (int physical = 1; physical < RowMapping.MAX_ROWS; physical++) {
            if (this.required[physical]) {
                menuSlots[physical] = new int[ROW];
                Arrays.fill(menuSlots[physical], -1);
            }
        }
    }

    void record(int inventoryIndex, int menuIndex) {
        int physical = inventoryIndex / ROW;
        int column = inventoryIndex % ROW;
        if (inventoryIndex < ROW || physical >= RowMapping.MAX_ROWS || !required[physical]) {
            return;
        }
        if (menuSlots[physical][column] != -1) {
            invalid = true;
            return;
        }
        menuSlots[physical][column] = menuIndex;
    }

    int[][] finish() {
        if (invalid) {
            return null;
        }
        for (int physical = 1; physical < RowMapping.MAX_ROWS; physical++) {
            if (!required[physical]) {
                continue;
            }
            for (int column = 0; column < ROW; column++) {
                if (menuSlots[physical][column] < 0) {
                    return null;
                }
            }
        }
        return menuSlots;
    }
}
