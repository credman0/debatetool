package org.debatetool.core;

import java.io.IOException;

public class Analytic extends SpeechComponent {
    private String content;

    public Analytic(String content) {
        this.content = content;
    }

    @Override
    public String getDisplayContent() {
        return "<t>"+content+"</t>";
    }

    @Override
    public SpeechComponent clone() {
        return new Analytic(this.content);
    }

    @Override
    public String getStorageString() {
        return content;
    }

    @Override
    public String getStateString() {
        return null;
    }

    @Override
    public void load() throws IOException {
        // nothing to do
    }

    @Override
    public String getLabel() {
        return content;
    }

    @Override
    public boolean isLoaded() {
        return true;
    }
}
