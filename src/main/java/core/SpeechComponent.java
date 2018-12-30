package core;

import java.io.IOException;

public abstract class SpeechComponent {
    public abstract void load() throws IOException;
    public abstract boolean isLoaded();
}
