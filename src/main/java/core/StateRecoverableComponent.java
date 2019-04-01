package core;

public interface StateRecoverableComponent {
    String getStateString();
    void restoreState(String stateString);
}
