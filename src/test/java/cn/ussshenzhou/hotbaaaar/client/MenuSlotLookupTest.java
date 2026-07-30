package cn.ussshenzhou.hotbaaaar.client;

final class MenuSlotLookupTest {

    static void run() {
        resolvesReorderedMenuSlotsByInventoryIndex();
        missingSlotRejectsEntirePlan();
        duplicateInventoryIndexRejectsEntirePlan();
        unrelatedSlotsDoNotAffectRequiredRows();
    }

    private static void resolvesReorderedMenuSlotsByInventoryIndex() {
        MenuSlotLookup lookup = new MenuSlotLookup(new boolean[]{false, false, true, false});
        for (int column = 8; column >= 0; column--) {
            lookup.record(18 + column, 100 - column);
        }
        int[][] result = lookup.finish();
        require(result != null, "reordered complete row was rejected");
        for (int column = 0; column < 9; column++) {
            require(result[2][column] == 100 - column, "wrong menu slot for column " + column);
        }
    }

    private static void missingSlotRejectsEntirePlan() {
        MenuSlotLookup lookup = new MenuSlotLookup(new boolean[]{false, true, false, false});
        for (int column = 0; column < 8; column++) {
            lookup.record(9 + column, 40 + column);
        }
        require(lookup.finish() == null, "incomplete row was accepted");
    }

    private static void duplicateInventoryIndexRejectsEntirePlan() {
        MenuSlotLookup lookup = new MenuSlotLookup(new boolean[]{false, true, false, false});
        for (int column = 0; column < 9; column++) {
            lookup.record(9 + column, 40 + column);
        }
        lookup.record(9, 99);
        require(lookup.finish() == null, "duplicate inventory index was accepted");
    }

    private static void unrelatedSlotsDoNotAffectRequiredRows() {
        MenuSlotLookup lookup = new MenuSlotLookup(new boolean[]{false, false, false, true});
        for (int column = 0; column < 9; column++) {
            lookup.record(27 + column, 60 + column);
        }
        lookup.record(0, 0);
        lookup.record(17, 17);
        require(lookup.finish() != null, "unrelated inventory rows invalidated the plan");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
