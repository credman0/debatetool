package gui;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.TextFlow;

public class CardCutter extends CardViewer {
    @FXML protected BorderPane mainPane;
    @FXML protected TextField authorField;
    @FXML protected TextField dateField;
    @FXML protected TextField additionalField;
    @FXML protected TextFlow cardTextArea;
    protected String text;

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
