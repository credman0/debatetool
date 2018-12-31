package gui;

import com.sun.javafx.webkit.WebConsoleListener;
import core.CardOverlay;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

public class CardCutter extends CardViewer {
    @FXML protected BorderPane mainPane;
    @FXML protected TextField authorField;
    @FXML protected TextField dateField;
    @FXML protected TextField additionalField;
    @FXML protected WebView cardTextArea;
    private String text;
    private String cutterHTMLUrl = getClass().getClassLoader().getResource("CardCutter.html").toExternalForm();
    private CardOverlay overlay = new CardOverlay(new byte[0], new byte[0]);

    public void init(){

        WebConsoleListener.setDefaultListener(new WebConsoleListener(){
            @Override
            public void messageAdded(WebView webView, String message, int lineNumber, String sourceId) {
                System.out.println("Console: [" + sourceId + ":" + lineNumber + "] " + message);
            }
        });
        initCardText();
    }

    public void initCardText(){

        cardTextArea.getEngine().getLoadWorker().stateProperty().addListener(
                new ChangeListener<>() {
                    // somewhere needs to hold a hard reference to this object or it gets garbage collected
                    private JavaBridge bridge = new JavaBridge();
                    @Override
                    public void changed(ObservableValue<? extends State> ov, State oldState, State newState) {
                        if (newState == State.SUCCEEDED) {
                            JSObject jsobj = (JSObject) cardTextArea.getEngine().executeScript("window");
                            jsobj.setMember("java", bridge);
                        }
                    }
                });
        cardTextArea.getEngine().load(cutterHTMLUrl);
    }

    public class JavaBridge {
        public void updateSelection(int start, int end) {
            System.out.println(start+"e"+end);
            overlay.updateOverlay(start,end,CardOverlay.HIGHLIGHT);
            cardTextArea.getEngine().executeScript("document.getElementById('textarea').innerHTML = \""+overlay.generateHTML(text)+"\";");
        }
    }

    @Override
    public void setAuthor(String author) {
        authorField.setText(author);
    }

    @Override
    public void setDate(String date) {
        dateField.setText(date);
    }

    @Override
    public void setAdditionalInfo(String additionalInfo) {
        additionalField.setText(additionalInfo);
    }

    @Override
    public void setText(String text) {
        cardTextArea.getEngine().getDocument().getElementById("textarea").setTextContent(text);
        this.text = text;
    }

    @Override
    public String getAuthor() {
        return authorField.getText();
    }

    @Override
    public String getDate() {
        return dateField.getText();
    }

    @Override
    public String getAdditionalInfo() {
        return additionalField.getText();
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public Pane getPane() {
        return mainPane;
    }

}
