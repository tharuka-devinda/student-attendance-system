package lk.ijse.dep10.app.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import lk.ijse.dep10.app.db.DBConnection;
import lk.ijse.dep10.app.model.Student;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.sql.*;
import java.util.ArrayList;

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
    private TableView<Student> tblStudent;

    @FXML
    private TextField txtID;

    @FXML
    private TextField txtName;

    @FXML
    private TextField txtSearch;

    public void initialize() {

        tblStudent.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
        tblStudent.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("name"));
        tblStudent.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("picture"));

        loadAllStudents();

        btnDelete.setDisable(true);
        tblStudent.getSelectionModel().selectedItemProperty().addListener((ov, prev, current) -> {
            btnDelete.setDisable(current == null);
            if (current == null) return;

            txtID.setText(current.getId() + "");
            txtName.setText(current.getName());

            if (current.getPicture() != null) {
                try {
                    InputStream is = current.getPicture().getBinaryStream();
                    Image itemImage = new Image(is);
                    imgPicture.setImage(itemImage);
                    btnClear.setDisable(false);
                } catch (SQLException e) {
                    new Alert(Alert.AlertType.ERROR, "Failed to load the student's picture. Try again!").showAndWait();
                    throw new RuntimeException(e);
                }
            } else {
                btnClear.fire();
            }
        });

        Platform.runLater(btnNewStudent::fire);
    }

    private void loadAllStudents() {
        try {
            Connection connection = DBConnection.getInstance().getConnection();
            Statement stm = connection.createStatement();
            ResultSet rst = stm.executeQuery("SELECT * FROM Student");
            PreparedStatement stm2 = connection.prepareStatement("SELECT * FROM Picture WHERE student_id = ?");

            while (rst.next()) {
                String id = rst.getString("id");
                String name = rst.getString("name");
                Blob picture = null;

                stm2.setString(1, id);
                ResultSet rstPicture = stm2.executeQuery();
                if (rstPicture.next()) {
                    picture = rstPicture.getBlob("picture");
                }
                Student student = new Student(id, name, picture);
                tblStudent.getItems().add(student);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load customer details. Try again!", ButtonType.OK).show();
        }
    }

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
        Image image = new Image("/images/no-profile.png");
        imgPicture.setImage(image);
        btnClear.setDisable(true);
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
