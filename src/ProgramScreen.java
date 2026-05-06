public interface ProgramScreen {
    String getProgramName();

    default void onShow() {
        // Called when the hub shows this program.
    }

    default void onHide() {
        // Called when the hub hides this program.
    }
}