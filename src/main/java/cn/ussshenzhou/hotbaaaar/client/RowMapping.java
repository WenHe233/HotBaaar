package cn.ussshenzhou.hotbaaaar.client;

import java.util.Arrays;

/**
 * Pure logical-to-physical row state. Keeping this independent from Minecraft makes the permutation
 * rules testable and lets callers preflight every required menu slot before issuing any click.
 */
final class RowMapping {

    static final int MAX_ROWS = 4;

    private final int[] physicalOfLogical = {0, 1, 2, 3};
    private int activeLogicalRow;

    int activeLogicalRow() {
        return activeLogicalRow;
    }

    int physicalOfLogical(int logicalRow) {
        if (logicalRow < 0 || logicalRow >= MAX_ROWS) {
            return logicalRow;
        }
        return physicalOfLogical[logicalRow];
    }

    void resetIdentity() {
        for (int i = 0; i < MAX_ROWS; i++) {
            physicalOfLogical[i] = i;
        }
        activeLogicalRow = 0;
    }

    boolean isIdentity() {
        for (int i = 0; i < MAX_ROWS; i++) {
            if (physicalOfLogical[i] != i) {
                return false;
            }
        }
        return true;
    }

    /**
     * Return the physical rows that must be swapped with the hotbar to restore identity.
     * This method does not mutate the live mapping.
     */
    int[] planRestore() {
        int[] simulated = Arrays.copyOf(physicalOfLogical, MAX_ROWS);
        int[] plan = new int[MAX_ROWS * 2];
        int size = 0;

        for (int home = 1; home < MAX_ROWS; home++) {
            if (logicalAtPhysical(simulated, home) == home) {
                continue;
            }
            if (logicalAtPhysical(simulated, 0) != home) {
                int source = simulated[home];
                plan[size++] = source;
                applySwap(simulated, source);
            }
            plan[size++] = home;
            applySwap(simulated, home);
        }
        return Arrays.copyOf(plan, size);
    }

    /** Update the mapping after physical row {@code physical} was successfully swapped with row 0. */
    void applyHotbarSwap(int physical) {
        if (physical == 0) {
            return;
        }
        applySwap(physicalOfLogical, physical);
        activeLogicalRow = logicalAtPhysical(physicalOfLogical, 0);
    }

    int[] snapshot() {
        return Arrays.copyOf(physicalOfLogical, MAX_ROWS);
    }

    private static void applySwap(int[] mapping, int physical) {
        int hotbarLogical = logicalAtPhysical(mapping, 0);
        int otherLogical = logicalAtPhysical(mapping, physical);
        mapping[hotbarLogical] = physical;
        mapping[otherLogical] = 0;
    }

    private static int logicalAtPhysical(int[] mapping, int physical) {
        for (int logical = 0; logical < MAX_ROWS; logical++) {
            if (mapping[logical] == physical) {
                return logical;
            }
        }
        throw new IllegalStateException("Invalid row permutation");
    }
}
