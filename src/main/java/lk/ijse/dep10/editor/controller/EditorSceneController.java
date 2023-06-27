package lk.ijse.dep10.editor.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lk.ijse.dep10.editor.AppInitializer;
import lk.ijse.dep10.editor.util.SearchResult;

import java.io.*;
import java.util.ArrayList;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditorSceneController {
    public TextArea txtEditor;
    public TextField txtReplace;
    public TextField txtFind;
    public Button btnDown;
    public Button btnUp;
    public CheckBox shkMatchCase;
    public Button btnReplace;
    public Button btnReplaceAll;
    public Label lblResults;
    private int num = 0;
    private File file;
    FileChooser fileChooser;
    String name;
    String nameOpen;
    boolean typing = false;
    boolean typingOpen = false;
    boolean dragOpen = false;
    private ArrayList<SearchResult> searchResultList = new ArrayList<>();
    private int pos = 0;
    SearchResult searchResult;
    String selectedText;
    boolean isIgnoreCase = true;
    public void initialize() {
        txtFind.textProperty().addListener((observable, oldValue, current) -> {
            findResultCount();
        });
        txtEditor.textProperty().addListener((observable, oldValue, current) -> {
            findResultCount();

            typing = true;
            if (current.length() != 0 && num == 0)
                ((Stage) txtEditor.getScene().getWindow()).setTitle("*untitled document");
            if (num > 0) {
                ((Stage) txtEditor.getScene().getWindow()).setTitle("*" + name);
            }
        });
    }

    private void findResultCount() {
        String query = txtFind.getText();
        if (query.isEmpty()) return;
        searchResultList.clear();
        pos = 0;
        txtEditor.deselect();

        Pattern pattern;
        try {
            if (isIgnoreCase) pattern = Pattern.compile("(?i)" + query);
            else pattern = Pattern.compile(query);
        } catch (RuntimeException e) {
            return;
        }
        Matcher matcher = pattern.matcher(txtEditor.getText());

        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            SearchResult searchResult = new SearchResult(start, end);
            searchResultList.add(searchResult);
        }
        lblResults.setText(String.format("%d Result", searchResultList.size()));
        select();
    }

    private void select() {
        if (searchResultList.isEmpty()) return;
        searchResult = searchResultList.get(pos);
        txtEditor.selectRange(searchResult.getStart(), searchResult.getEnd());
        lblResults.setText(String.format("%d/%d Result", pos + 1, searchResultList.size()));
    }

    @FXML
    void mnAboutOnAction(ActionEvent event) throws IOException {
        Stage stage = new Stage();
        stage.setScene(new Scene(new FXMLLoader(getClass().getResource("/view/AboutScene.fxml")).load()));
        stage.setTitle("About");
        stage.show();
        stage.centerOnScreen();
    }

    @FXML
    void mnCloseOnAction(ActionEvent event) throws IOException {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION,
                "Do you need to save this file ?",
                ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> optButton = confirmAlert.showAndWait();
        if (optButton.isEmpty() || optButton.get() == ButtonType.NO) {
            System.exit(0);
            return;
        }
        if (txtEditor.getText().length() > 0) mnSaveAsOnAction(null);
    }

    @FXML
    void mnNewOnAction(ActionEvent event) throws IOException {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION,
                "Do you need to save this file ?",
                ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> optButton = confirmAlert.showAndWait();
        if (optButton.isEmpty() || optButton.get() == ButtonType.NO) {
            txtEditor.clear();
            ((Stage) txtEditor.getScene().getWindow()).setTitle("untitled document");
            return;
        }
        mnSaveOnAction(null);
    }

    @FXML
    void mnOpenOnAction(ActionEvent event) throws IOException {
        num++;
        fileChooser = new FileChooser();
        fileChooser.setTitle("Open a text file");
        file = fileChooser.showOpenDialog(txtEditor.getScene().getWindow());
        if (file == null) return;
        ((Stage) txtEditor.getScene().getWindow()).setTitle(file.getName());
        name = file.getName();
        FileInputStream fis = new FileInputStream(file);
        byte[] bytes = fis.readAllBytes();
        fis.close();

        typingOpen = false;
        txtEditor.setText(new String(bytes));
    }

    @FXML
    void mnSaveOnAction(ActionEvent event) throws IOException {
        if (num == 0) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save a file");
            file = fileChooser.showSaveDialog(txtEditor.getScene().getWindow());
            String[] address = file.toString().split("/");
            name = address[address.length - 1];
            if (file == null) return;

            FileOutputStream fos = new FileOutputStream(file);
            String text = txtEditor.getText();
            byte[] bytes = text.getBytes();
            fos.write(bytes);
            fos.close();
            num++;
            ((Stage) txtEditor.getScene().getWindow()).setTitle(address[address.length - 1]);
            typing = false;
        } else {
            FileOutputStream fos = new FileOutputStream(file);
            String text = txtEditor.getText();
            byte[] bytes = text.getBytes();
            fos.write(bytes);
            fos.close();
            ((Stage) txtEditor.getScene().getWindow()).setTitle(name);
            typing = false;
        }
    }

    public void rootOnDragOver(DragEvent dragEvent) {
        dragEvent.acceptTransferModes(TransferMode.ANY);
    }

    public void rootOnDragDrop(DragEvent dragEvent) throws IOException {
        num++;
        file = dragEvent.getDragboard().getFiles().get(0);
        name = file.getName();
        ((Stage) txtEditor.getScene().getWindow()).setTitle(name);
        FileInputStream fis = new FileInputStream(file);
        byte[] bytes = fis.readAllBytes();
        fis.close();
        txtEditor.setText(new String(bytes));
        typing = false;
        dragOpen = false;

    }

    public void mnSaveAsOnAction(ActionEvent actionEvent) throws IOException {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save a file");
        File file = fileChooser.showSaveDialog(txtEditor.getScene().getWindow());
        if (file == null) return;
        String[] address = file.toString().split("/");
        ((Stage) txtEditor.getScene().getWindow()).setTitle(address[address.length - 1]);

        FileOutputStream fos = new FileOutputStream(file);
        String text = txtEditor.getText();
        byte[] bytes = text.getBytes();
        fos.write(bytes);
        fos.close();
        num++;
    }

    public void closeOnKeyRelease(KeyEvent keyEvent) {
        ((Stage) txtEditor.getScene().getWindow()).setOnCloseRequest(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Do you need to save file before close",
                    ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> button = alert.showAndWait();
            if (button.isPresent() && button.get() == ButtonType.YES) {
                try {
                    mnSaveAsOnAction(null);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public void btnDownOnAction(ActionEvent actionEvent) {
        pos++;
        if (pos == searchResultList.size()) {
            pos = -1;
            return;
        }
        select();
    }

    public void btnUpOnAction(ActionEvent actionEvent) {
        pos--;
        if (pos < 0) {
            pos = searchResultList.size();
            return;
        }
        select();
    }

    public void shkMatchCaseOnAction(ActionEvent actionEvent) {
        if (shkMatchCase.isSelected()) {
            isIgnoreCase = true;
            findResultCount();
        }
        if (!shkMatchCase.isSelected()) {
            isIgnoreCase = false;
            findResultCount();
        }
    }

    public void btnReplaceOnAction(ActionEvent actionEvent) {
        txtEditor.replaceText(searchResult.getStart(), searchResult.getEnd(), txtReplace.getText());
        findResultCount();
    }

    public void btnReplaceAllOnAction(ActionEvent actionEvent) {
        selectedText = txtEditor.getSelectedText();
        txtEditor.setText(txtEditor.getText().replaceAll(selectedText, txtReplace.getText()));
        txtReplace.clear();
        txtFind.clear();
    }

}
