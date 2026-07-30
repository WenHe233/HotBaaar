package cn.ussshenzhou.hotbaaaar.client;

public final class ClientStateTestSuite {

    private ClientStateTestSuite() {
    }

    public static void main(String[] args) {
        RowMappingTest.run();
        MenuSlotLookupTest.run();
        GuiRestoreStateTest.run();
    }
}
