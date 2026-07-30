package cn.ussshenzhou.hotbaaaar.client;

/**
 * Pure GUI/connection coordinator. Minecraft-specific clicks stay behind {@link InventoryActions},
 * so transitions and retry behaviour can be tested without launching the game.
 */
final class GuiRestoreState {

    interface InventoryActions {
        int activeRow();

        void resetIdentity();

        boolean restoreCanonical();

        boolean activateLogicalRow(int row);
    }

    private Object lastConnection;
    private boolean canonicalForScreen;
    private int savedActiveRow;

    void reconcile(
            Object connection,
            boolean playerReady,
            boolean screenOpen,
            int availableRows,
            InventoryActions actions
    ) {
        if (connection != lastConnection) {
            lastConnection = connection;
            canonicalForScreen = false;
            savedActiveRow = 0;
            actions.resetIdentity();
        }

        if (!playerReady) {
            return;
        }

        if (screenOpen) {
            if (!canonicalForScreen) {
                int rowToResume = actions.activeRow();
                if (actions.restoreCanonical()) {
                    savedActiveRow = rowToResume;
                    canonicalForScreen = true;
                }
            }
            return;
        }

        if (canonicalForScreen) {
            if (savedActiveRow > 0 && savedActiveRow < availableRows
                    && !actions.activateLogicalRow(savedActiveRow)) {
                return;
            }
            canonicalForScreen = false;
            savedActiveRow = 0;
        }

        if (actions.activeRow() >= availableRows) {
            actions.restoreCanonical();
        }
    }

    boolean isCanonicalForScreen() {
        return canonicalForScreen;
    }
}
