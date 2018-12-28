package core;

import io.componentio.ComponentIOManager;

import java.io.IOException;

public abstract class SpeechComponent {
    public abstract void load(ComponentIOManager manager) throws IOException;
    public abstract boolean isLoaded();
}
