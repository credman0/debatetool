package gui.cardediting;

import com.sun.javafx.webkit.WebConsoleListener;
import core.Card;
import core.CardOverlay;
import io.iocontrollers.IOController;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class CardCutter extends CardViewer {
    @FXML protected ComboBox tagChoice;
    @FXML protected Button addUnderlineButton;
    @FXML protected Button addHighlightButton;
    @FXML protected Label citeLabel;
    @FXML protected ComboBox<CardOverlay> highlightChoice;
    @FXML protected ComboBox<CardOverlay> underlineChoice;
    @FXML protected BorderPane mainPane;
    @FXML protected WebView cardTextArea;
    @FXML protected RadioButton underlineRadio;
    private StringProperty author = new SimpleStringProperty();
    private StringProperty date = new SimpleStringProperty();
    private StringProperty additionalInfo = new SimpleStringProperty();
    private String text;
    private String cutterHTMLUrl = getClass().getClassLoader().getResource("CardCutter.html").toExternalForm();
    private ObservableList<CardOverlay> highlightingOverlayList = FXCollections.checkedObservableList(FXCollections.observableArrayList(), CardOverlay.class);
    private ObservableList<CardOverlay> underliningOverlayList = FXCollections.checkedObservableList(FXCollections.observableArrayList(), CardOverlay.class);
    private ObservableList<String> tagsList = FXCollections.checkedObservableList(FXCollections.observableArrayList(), String.class);
    private Card card;

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
        initOverlayChoiceListeners();
        initTagChoiceListeners();
        citeLabel.textProperty().bind(Bindings.concat(author, " ", date, " (", additionalInfo,")"));
    }

    private void initTagChoiceListeners(){
        // listener to set on losing focus
        tagChoice.getEditor().focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
                if (t1.equals(false)){
                    tagChoice.getEditor().commitValue();
                }
            }
        });
        // listener to change list
        tagChoice.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue observableValue, String o, String t1) {
                if (t1!=null && !t1.equals(o)) {
                    ((ObjectProperty) observableValue).setValue(t1);
                }
            }

        });
    }

    private void initOverlayChoiceListeners(){
        // listeners to change the name when the field is edited
        underlineChoice.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observableValue, Object o, Object t1) {
                if (o != null && t1!=null && t1.getClass().equals(String.class)){
                    CardOverlay overlay = (CardOverlay) o;
                    overlay.setName(t1.toString());
                    ((SimpleObjectProperty) observableValue).setValue(o);
                }
            }
        });
        underlineChoice.getEditor().focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
                if (t1.equals(false)){
                    underlineChoice.getEditor().commitValue();
                }
            }
        });
        highlightChoice.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observableValue, Object o, Object t1) {
                if (o != null && t1!=null && t1.getClass().equals(String.class)){
                    CardOverlay overlay = (CardOverlay) o;
                    overlay.setName(t1.toString());
                    ((SimpleObjectProperty) observableValue).setValue(o);
                }
            }
        });
        highlightChoice.getEditor().focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
                if (t1.equals(false)) {
                    underlineChoice.getEditor().commitValue();
                }
            }
        });

        // listeners to update the overlay when the selection is changed
        underlineChoice.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                if (t1.intValue()<0){
                    return;
                }
                applyOverlay();
            }
        });
        highlightChoice.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                if (t1.intValue()<0){
                    return;
                }
                applyOverlay();
            }
        });
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


    public void newUnderline(ActionEvent actionEvent) {
        underliningOverlayList.add(new CardOverlay("New Underline"));
        underlineChoice.getSelectionModel().select(underliningOverlayList.size()-1);
    }

    public void newHighlight(ActionEvent actionEvent) {
        highlightingOverlayList.add(new CardOverlay("New Highlight"));
        highlightChoice.getSelectionModel().select(highlightingOverlayList.size()-1);
    }

    public class JavaBridge {
        public void updateSelection(int start, int end) {
            System.out.println(start+"e"+end);
            getActiveOverlay().updateOverlay(start,end,getActiveOverlayType());
            applyOverlay();
        }
    }

    private void applyOverlay(){

        if  (highlightChoice.getValue()==null||underlineChoice.getValue()==null){
            return;
        }
        CardOverlay combinedOverlay = CardOverlay.combineOverlays(getActiveUnderlineOverlay(),getActiveHighlightOverlay());
        cardTextArea.getEngine().executeScript("document.getElementById('textarea').innerHTML = \""+combinedOverlay.generateHTML(text)+"\";");
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
            return getActiveUnderlineOverlay();
        }else {
            return getActiveHighlightOverlay();
        }
    }

    private CardOverlay getActiveHighlightOverlay(){
        return highlightChoice.getValue();
    }

    private CardOverlay getActiveUnderlineOverlay(){
        return underlineChoice.getValue();
    }

    @Override
    public void open(Card card){
        if (getCurrentHash()!=null && Arrays.equals(getCurrentHash(), card.getHash())){
            // should already be loaded, don't overwrite stuff
            applyOverlay();
            return;
        }

        this.card = card;
        super.open(card);
        highlightingOverlayList.clear();
        highlightingOverlayList.addAll(IOController.getIoController().getOverlayIOManager().getOverlays(getCurrentHash(), "Highlight"));
        if (highlightingOverlayList.isEmpty()){
            highlightingOverlayList.add(new CardOverlay("Highlighting"));
        }
        highlightChoice.getSelectionModel().select(0);

        underliningOverlayList.clear();
        underliningOverlayList.addAll(IOController.getIoController().getOverlayIOManager().getOverlays(getCurrentHash(), "Underline"));
        if (underliningOverlayList.isEmpty()){
            underliningOverlayList.add(new CardOverlay("Underlining"));
        }
        underlineChoice.getSelectionModel().select(0);

        applyOverlay();

        tagsList = FXCollections.observableList(card.getTags());
        tagChoice.setItems(tagsList);
        if (tagsList.isEmpty()){
            tagsList.add("Empty tag");
            tagChoice.getSelectionModel().select(0);
        }
    }

    @Override
    public void save(List<String> path){
        IOController.getIoController().getOverlayIOManager().saveOverlays(card.getHash(), highlightingOverlayList, "Highlight");
        IOController.getIoController().getOverlayIOManager().saveOverlays(card.getHash(), underliningOverlayList, "Underline");
        try {
            IOController.getIoController().getComponentIOManager().storeSpeechComponent(card);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
