package gui;

import com.sun.javafx.webkit.WebConsoleListener;
import core.Card;
import core.CardOverlay;
import core.Main;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker.State;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import java.util.Arrays;
import java.util.List;

public class CardCutter extends CardViewer {
    @FXML protected Label citeLabel;
    @FXML protected ComboBox highlightChoice;
    @FXML protected ComboBox underlineChoice;
    @FXML protected BorderPane mainPane;
    @FXML protected WebView cardTextArea;
    @FXML protected RadioButton underlineRadio;
    private StringProperty author = new SimpleStringProperty();
    private StringProperty date = new SimpleStringProperty();
    private StringProperty additionalInfo = new SimpleStringProperty();
    private String text;
    private String cutterHTMLUrl = getClass().getClassLoader().getResource("CardCutter.html").toExternalForm();
    private ObservableList<CardOverlay> highlightingOverlayList = FXCollections.observableArrayList();
    private ObservableList<CardOverlay> underliningOverlayList = FXCollections.observableArrayList();
    private byte[] currentHash = null;

    public void init(){

        WebConsoleListener.setDefaultListener(new WebConsoleListener(){
            @Override
            public void messageAdded(WebView webView, String message, int lineNumber, String sourceId) {
                System.out.println("Console: [" + sourceId + ":" + lineNumber + "] " + message);
            }
        });
        initHTML();
        highlightChoice.setItems(highlightingOverlayList);
        underlineChoice.setItems(underliningOverlayList);
        citeLabel.textProperty().bind(Bindings.concat(author, " ", date, " (", additionalInfo,")"));
    }

    public void initHTML(){

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
            getActiveOverlay().updateOverlay(start,end,getActiveOverlayType());
            applyOverlay();
        }
    }

    private void applyOverlay(){
        cardTextArea.getEngine().executeScript("document.getElementById('textarea').innerHTML = \""+getActiveOverlay().generateHTML(text)+"\";");
    }

    private byte getActiveOverlayType(){
        if (underlineRadio.isSelected()){
            return CardOverlay.UNDERLINE;
        }else{
            return CardOverlay.HIGHLIGHT;
        }
    }

    private CardOverlay getActiveOverlay(){
        if (underlineRadio.isSelected()){
            return underliningOverlayList.get(underlineChoice.getSelectionModel().getSelectedIndex());
        }else {
            return highlightingOverlayList.get(highlightChoice.getSelectionModel().getSelectedIndex());
        }
    }

    @Override
    public void open(Card card){
        super.open(card);
        if (currentHash!=null && Arrays.equals(currentHash, card.getHash())){
            // should already be loaded, don't overwrite stuff
            applyOverlay();
            return;
        }
        currentHash = card.getHash();
        highlightingOverlayList.clear();
        highlightingOverlayList.addAll(Main.getIoController().getOverlayIOManager().getOverlays(currentHash, "Highlight"));
        if (highlightingOverlayList.isEmpty()){
            highlightingOverlayList.add(new CardOverlay("Highlighting"));
        }
        highlightChoice.getSelectionModel().select(0);

        underliningOverlayList.clear();
        underliningOverlayList.addAll(Main.getIoController().getOverlayIOManager().getOverlays(currentHash, "Underline"));
        if (underliningOverlayList.isEmpty()){
            underliningOverlayList.add(new CardOverlay("Underlining"));
        }
        underlineChoice.getSelectionModel().select(0);

        applyOverlay();
    }

    @Override
    public void save(List<String> path){
        Card card = createCard();
        Main.getIoController().getOverlayIOManager().saveOverlays(card.getHash(), highlightingOverlayList, "Highlight");
        Main.getIoController().getOverlayIOManager().saveOverlays(card.getHash(), underliningOverlayList, "Underline");
    }

    @Override
    public void setAuthor(String author) {
        this.author.setValue(author);
    }

    @Override
    public void setDate(String date) {
        this.date.setValue(date);
    }

    @Override
    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo.setValue(additionalInfo);
    }

    @Override
    public void setText(String text) {
        cardTextArea.getEngine().getDocument().getElementById("textarea").setTextContent(text);
        this.text = text;
    }

    @Override
    public String getAuthor() {
        return author.get();
    }

    @Override
    public String getDate() {
        return date.get();
    }

    @Override
    public String getAdditionalInfo() {
        return additionalInfo.get();
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
