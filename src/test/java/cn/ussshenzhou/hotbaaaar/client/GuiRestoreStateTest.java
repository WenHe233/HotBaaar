package cn.ussshenzhou.hotbaaaar.client;

final class GuiRestoreStateTest {

    static void run() {
        guiToGuiTransitionRestoresAndResumesOnce();
        failedRestoreRetriesWithoutChangingState();
        windowShrinkRestoresBeforeReturningToRowZero();
        playerReplacementOnSameConnectionPreservesState();
        newConnectionInitializesNewBaseline();
    }

    private static void guiToGuiTransitionRestoresAndResumesOnce() {
        GuiRestoreState state = new GuiRestoreState();
        FakeActions actions = new FakeActions(0);
        Object connection = new Object();

        state.reconcile(connection, true, false, 4, actions);
        actions.activeRow = 2;
        actions.resetCalls = 0;
        state.reconcile(connection, true, true, 4, actions);
        state.reconcile(connection, true, true, 4, actions);
        state.reconcile(connection, true, false, 4, actions);

        require(actions.restoreCalls == 1, "GUI-to-GUI transition restored more than once");
        require(actions.activateCalls == 1, "return to HUD did not resume exactly once");
        require(actions.activeRow == 2, "return to HUD resumed the wrong row");
    }

    private static void failedRestoreRetriesWithoutChangingState() {
        GuiRestoreState state = new GuiRestoreState();
        FakeActions actions = new FakeActions(0);
        Object connection = new Object();
        state.reconcile(connection, true, false, 4, actions);
        actions.activeRow = 3;
        actions.resetCalls = 0;
        actions.restoreSucceeds = false;

        state.reconcile(connection, true, true, 4, actions);
        state.reconcile(connection, true, true, 4, actions);

        require(actions.restoreCalls == 2, "unsafe restore was not retried");
        require(actions.activeRow == 3, "failed restore changed the active row");
        require(!state.isCanonicalForScreen(), "failed restore was marked canonical");
    }

    private static void windowShrinkRestoresBeforeReturningToRowZero() {
        GuiRestoreState state = new GuiRestoreState();
        FakeActions actions = new FakeActions(0);
        Object connection = new Object();
        state.reconcile(connection, true, false, 4, actions);
        actions.activeRow = 3;
        actions.resetCalls = 0;

        state.reconcile(connection, true, false, 2, actions);

        require(actions.restoreCalls == 1, "window shrink did not physically restore");
        require(actions.activeRow == 0, "window shrink did not end on row zero");
        require(actions.resetCalls == 0, "window shrink discarded mapping by reset");
    }

    private static void playerReplacementOnSameConnectionPreservesState() {
        GuiRestoreState state = new GuiRestoreState();
        FakeActions actions = new FakeActions(0);
        Object connection = new Object();
        state.reconcile(connection, true, false, 4, actions);
        actions.activeRow = 2;
        actions.resetCalls = 0;

        // Player identity is intentionally not an input to the coordinator.
        state.reconcile(connection, true, false, 4, actions);

        require(actions.resetCalls == 0, "same-connection player replacement reset the baseline");
        require(actions.activeRow == 2, "same-connection player replacement lost the active row");
    }

    private static void newConnectionInitializesNewBaseline() {
        GuiRestoreState state = new GuiRestoreState();
        FakeActions actions = new FakeActions(2);
        state.reconcile(new Object(), true, false, 4, actions);
        actions.activeRow = 3;
        actions.resetCalls = 0;

        state.reconcile(new Object(), true, false, 4, actions);

        require(actions.resetCalls == 1, "new connection did not initialize a baseline");
        require(actions.activeRow == 0, "new connection baseline was not identity");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static final class FakeActions implements GuiRestoreState.InventoryActions {
        private int activeRow;
        private int resetCalls;
        private int restoreCalls;
        private int activateCalls;
        private boolean restoreSucceeds = true;

        private FakeActions(int activeRow) {
            this.activeRow = activeRow;
        }

        @Override
        public int activeRow() {
            return activeRow;
        }

        @Override
        public void resetIdentity() {
            resetCalls++;
            activeRow = 0;
        }

        @Override
        public boolean restoreCanonical() {
            restoreCalls++;
            if (restoreSucceeds) {
                activeRow = 0;
            }
            return restoreSucceeds;
        }

        @Override
        public boolean activateLogicalRow(int row) {
            activateCalls++;
            activeRow = row;
            return true;
        }
    }
}
