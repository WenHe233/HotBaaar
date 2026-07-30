package cn.ussshenzhou.hotbaaaar.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MenuSlotLookupTest {

    @Test
    void resolvesReorderedMenuSlotsByInventoryIndex() {
        MenuSlotLookup lookup = new MenuSlotLookup(new boolean[]{false, false, true, false});
        for (int column = 8; column >= 0; column--) {
            lookup.record(18 + column, 100 - column);
        }
        int[][] result = lookup.finish();
        assertNotNull(result);
        for (int column = 0; column < 9; column++) {
            assertEquals(100 - column, result[2][column]);
        }
    }

    @Test
    void missingSlotRejectsEntirePlan() {
        MenuSlotLookup lookup = new MenuSlotLookup(new boolean[]{false, true, false, false});
        for (int column = 0; column < 8; column++) {
            lookup.record(9 + column, 40 + column);
        }
        assertNull(lookup.finish());
    }

    @Test
    void duplicateInventoryIndexRejectsEntirePlan() {
        MenuSlotLookup lookup = new MenuSlotLookup(new boolean[]{false, true, false, false});
        for (int column = 0; column < 9; column++) {
            lookup.record(9 + column, 40 + column);
        }
        lookup.record(9, 99);
        assertNull(lookup.finish());
    }

    @Test
    void unrelatedAndProxySlotsAreIgnoredByCaller() {
        MenuSlotLookup lookup = new MenuSlotLookup(new boolean[]{false, false, false, true});
        for (int column = 0; column < 9; column++) {
            lookup.record(27 + column, 60 + column);
        }
        // Indices outside the required row do not affect the resolved plan.
        lookup.record(0, 0);
        lookup.record(17, 17);
        assertNotNull(lookup.finish());
    }
}
