package core;

import java.io.IOException;

public abstract class SpeechComponent {
    public abstract void load() throws IOException;
    public abstract boolean isLoaded();
    public abstract String getDisplayContent();
    public abstract SpeechComponent clone();
}
