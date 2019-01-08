package core;

import java.util.ArrayList;

public class Speech {
    ArrayList<SpeechComponent> contents;

    public String getDisplayContent(){
        StringBuilder contentsBuilder = new StringBuilder();
        for (int i = 0; i < contents.size(); i++) {
            contentsBuilder.append("<p>"+(i)+"</p>");
            SpeechComponent component = contents.get(i);
            contentsBuilder.append(component.getDisplayContent());
        }
        return contentsBuilder.toString();
    }
}
