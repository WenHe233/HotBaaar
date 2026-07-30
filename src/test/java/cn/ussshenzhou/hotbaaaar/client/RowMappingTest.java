package cn.ussshenzhou.hotbaaaar.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RowMappingTest {

    @Test
    void everyActiveRowRestoresToIdentity() {
        for (int target = 0; target < RowMapping.MAX_ROWS; target++) {
            RowMapping mapping = new RowMapping();
            if (target > 0) {
                mapping.applyHotbarSwap(target);
            }
            int[] plan = mapping.planRestore();
            for (int physical : plan) {
                mapping.applyHotbarSwap(physical);
            }
            assertTrue(mapping.isIdentity());
            assertEquals(0, mapping.activeLogicalRow());
        }
    }

    @Test
    void representativePermutationRestoresAndCanResumeSavedRow() {
        RowMapping mapping = new RowMapping();
        mapping.applyHotbarSwap(1);
        mapping.applyHotbarSwap(2);
        mapping.applyHotbarSwap(3);
        int saved = mapping.activeLogicalRow();

        for (int physical : mapping.planRestore()) {
            mapping.applyHotbarSwap(physical);
        }
        assertArrayEquals(new int[]{0, 1, 2, 3}, mapping.snapshot());

        mapping.applyHotbarSwap(mapping.physicalOfLogical(saved));
        assertEquals(saved, mapping.activeLogicalRow());
        assertArrayEquals(new int[]{3, 1, 2, 0}, mapping.snapshot());
    }

    @Test
    void planningDoesNotMutateLiveState() {
        RowMapping mapping = new RowMapping();
        mapping.applyHotbarSwap(2);
        int[] before = mapping.snapshot();
        mapping.planRestore();
        assertArrayEquals(before, mapping.snapshot());
    }
}
