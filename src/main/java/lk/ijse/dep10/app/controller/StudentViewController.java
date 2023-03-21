package lk.ijse.dep10.app.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.MalformedURLException;

public class StudentViewController {

    @FXML
    private Button btnBrowse;

    @FXML
    private Button btnClear;

    @FXML
    private Button btnDelete;

    @FXML
    private Button btnNewStudent;

    @FXML
    private Button btnSave;

    @FXML
    private ImageView imgPicture;

    @FXML
    private TableView<?> tblStudent;

    @FXML
    private TextField txtID;

    @FXML
    private TextField txtName;

    @FXML
    private TextField txtSearch;

    @FXML
    void btnBrowseOnAction(ActionEvent event) throws MalformedURLException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a Picture for the Student");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp"));
        File file = fileChooser.showOpenDialog(btnBrowse.getScene().getWindow());
        if (file != null){
            Image image = new Image(file.toURI().toURL().toString(), 177.0, 161.0, true, true);
            imgPicture.setImage(image);
            btnClear.setDisable(false);
        }
    }

    @FXML
    void btnClearOnAction(ActionEvent event) {

    }

    @FXML
    void btnDeleteOnAction(ActionEvent event) {

    }

    @FXML
    void btnNewStudentOnAction(ActionEvent event) {
        txtID.setDisable(false);
        txtName.setDisable(false);
        btnBrowse.setDisable(false);
        btnSave.setDisable(false);
        txtID.clear();
        txtName.clear();
        txtID.getStyleClass().remove("invalid");
        txtName.getStyleClass().remove("invalid");
        txtName.requestFocus();
    }

    @FXML
    void btnSaveOnAction(ActionEvent event) {

    }

    @FXML
    void tblStudentOnKeyReleased(KeyEvent event) {

    }

}
