package core;

import java.io.IOException;

public class Analytic extends SpeechComponent implements BlockComponent {
    private final String content;

    public Analytic(String content) {
        this.content = content;
    }

    @Override
    public String getDisplayContent() {
        return content;
    }

    @Override
    public String getBlockStorageString() {
        return content;
    }

    @Override
    public void load() throws IOException {
        // nothing to do
    }

    @Override
    public boolean isLoaded() {
        return true;
    }
}
