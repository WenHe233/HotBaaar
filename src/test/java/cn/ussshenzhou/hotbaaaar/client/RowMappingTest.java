package cn.ussshenzhou.hotbaaaar.client;

import java.util.Arrays;

final class RowMappingTest {

    static void run() {
        everyActiveRowRestoresToIdentity();
        representativePermutationRestoresAndCanResumeSavedRow();
        planningDoesNotMutateLiveState();
    }

    private static void everyActiveRowRestoresToIdentity() {
        for (int target = 0; target < RowMapping.MAX_ROWS; target++) {
            RowMapping mapping = new RowMapping();
            if (target > 0) {
                mapping.applyHotbarSwap(target);
            }
            for (int physical : mapping.planRestore()) {
                mapping.applyHotbarSwap(physical);
            }
            require(mapping.isIdentity(), "row " + target + " did not restore to identity");
            require(mapping.activeLogicalRow() == 0, "restored active row was not zero");
        }
    }

    private static void representativePermutationRestoresAndCanResumeSavedRow() {
        RowMapping mapping = new RowMapping();
        mapping.applyHotbarSwap(1);
        mapping.applyHotbarSwap(2);
        mapping.applyHotbarSwap(3);
        int saved = mapping.activeLogicalRow();

        for (int physical : mapping.planRestore()) {
            mapping.applyHotbarSwap(physical);
        }
        require(Arrays.equals(new int[]{0, 1, 2, 3}, mapping.snapshot()), "restore was not identity");

        mapping.applyHotbarSwap(mapping.physicalOfLogical(saved));
        require(mapping.activeLogicalRow() == saved, "saved row was not resumed");
        require(Arrays.equals(new int[]{3, 1, 2, 0}, mapping.snapshot()), "resumed mapping was wrong");
    }

    private static void planningDoesNotMutateLiveState() {
        RowMapping mapping = new RowMapping();
        mapping.applyHotbarSwap(2);
        int[] before = mapping.snapshot();
        mapping.planRestore();
        require(Arrays.equals(before, mapping.snapshot()), "planning mutated live state");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
