package lk.ijse.dep10.app.controller;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import lk.ijse.dep10.app.db.DBConnection;
import lk.ijse.dep10.app.model.Student;

import javax.imageio.ImageIO;
import javax.sql.rowset.serial.SerialBlob;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Optional;

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

        tblStudent.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("id"));
        tblStudent.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("name"));
        tblStudent.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("picture"));

        loadAllStudents();

        btnDelete.setDisable(true);
        tblStudent.getSelectionModel().selectedItemProperty().addListener((ov, prev, current) -> {
            btnDelete.setDisable(current == null);
            if (current == null) return;

            txtID.setText(current.getId() + "");
            txtName.setText(current.getName());

            if (current.getPicture() != null) {
                Image studentImage = current.getPicture().getImage();
                imgPicture.setImage(studentImage);
                btnClear.setDisable(false);
            } else {
                btnClear.fire();
            }
        });

        txtSearch.textProperty().addListener((ov, prev, current) -> {
            Connection connection = DBConnection.getInstance().getConnection();
            try {
                Statement stm = connection.createStatement();
                String sql = "SELECT  * FROM Student WHERE Student.name LIKE '%1$s' OR Student.id LIKE '%1$s'";
                PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM Picture WHERE Picture.student_id = ?");
                sql = String.format(sql, "%" + current + "%");
                ResultSet rst = stm.executeQuery(sql);

                ObservableList<Student> studentList = tblStudent.getItems();
                studentList.clear();
                while (rst.next()) {
                    String id = rst.getString("id");
                    String name = rst.getString("name");
                    Image image = new Image("/images/no-profile.png");
//                    BufferedImage bi = SwingFXUtils.fromFXImage(image, null);
//                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
//                    ImageIO.write(bi, "png", bos);
//                    byte[] bytes = bos.toByteArray();
                    ImageView picture = new ImageView(image);

                    preparedStatement.setString(1, id);
                    ResultSet rstPicture = preparedStatement.executeQuery();

                    if (rstPicture.next()) {
                        picture = new ImageView(new Image(rstPicture.getBlob("picture").getBinaryStream()));
                        studentList.add(new Student(id, name, picture));
                    }
                }
            } catch (SQLException e) {
                new Alert(Alert.AlertType.ERROR, "Failed to load student data from database. Try again!").showAndWait();
                throw new RuntimeException(e);
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
                ImageView picture = null;

                stm2.setString(1, id);
                ResultSet rstPicture = stm2.executeQuery();
                if (rstPicture.next()) {
                    picture = new ImageView(new Image((rstPicture.getBlob("picture").getBinaryStream())));
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
        Student selectedStudent = tblStudent.getSelectionModel().getSelectedItem();
        Optional<ButtonType> buttonType = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure to delete student with student id " + selectedStudent.getId() + " ?", ButtonType.YES, ButtonType.NO).showAndWait();
        if (buttonType.isPresent() && buttonType.get() == ButtonType.YES) {
            Connection connection = DBConnection.getInstance().getConnection();
            try {
                connection.setAutoCommit(false);
                PreparedStatement stmPicture = connection.prepareStatement("DELETE FROM Picture WHERE student_id = ?");
                stmPicture.setString(1, selectedStudent.getId());
                PreparedStatement stmStudent = connection.prepareStatement("DELETE FROM Student WHERE id = ?");
                stmStudent.setString(1, selectedStudent.getId());
                stmPicture.executeUpdate();
                stmStudent.executeUpdate();
                connection.commit();

                tblStudent.getItems().remove(selectedStudent);
                if (!tblStudent.getItems().isEmpty()) btnNewStudent.fire();
                connection.commit();
            } catch (Throwable e) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Failed to delete the item. Try again!").show();
            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }

    @FXML
    void btnNewStudentOnAction(ActionEvent event) {
        txtID.setDisable(false);
        txtName.setDisable(false);
        btnBrowse.setDisable(false);
        btnSave.setDisable(false);
        txtID.clear();
        txtName.clear();
        txtName.getStyleClass().remove("invalid");
        imgPicture.setImage(new Image("/images/no-profile.png"));
        txtName.requestFocus();
        generateID();
    }

    @FXML
    void btnSaveOnAction(ActionEvent event) {
        if (!isDataValid()) return;

        Connection connection = DBConnection.getInstance().getConnection();
        try {
            PreparedStatement stmStudent = connection.prepareStatement("INSERT INTO Student (id, name) VALUES (?, ?)");
            PreparedStatement stmStudentPicture = connection.prepareStatement("INSERT INTO Picture (student_id, picture) VALUES (?, ?)");

            connection.setAutoCommit(false);

            stmStudent.setString(1, txtID.getText());
            stmStudent.setString(2, txtName.getText());
            stmStudent.executeUpdate();

            Student newStudent = new Student(txtID.getText(), txtName.getText(), null);

            Image image = imgPicture.getImage();
            if (image != null) {
                /*nJavaFX Image -> Blob */
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "png", baos);
                byte[] bytes = baos.toByteArray();
                Blob studentPicture = new SerialBlob(bytes);

                newStudent.setPicture(new ImageView(new Image(studentPicture.getBinaryStream())));
                stmStudentPicture.setString(1, txtID.getText());
                stmStudentPicture.setBlob(2, studentPicture);
                stmStudentPicture.executeUpdate();
            }
            connection.commit();
            tblStudent.getItems().add(newStudent);
            btnNewStudent.fire();
        } catch (Throwable e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            new Alert(Alert.AlertType.ERROR, "Failed to saved the Student").show();
            e.printStackTrace();
        }
    }

    private void generateID() {
        if (tblStudent.getItems().size() == 0) txtID.setText("DEP-10/S001");
        else {
            int newID = Integer.parseInt(tblStudent.getItems().get(tblStudent.getItems().size() - 1).getId().substring(8));
            newID++;
            txtID.setText(String.format("DEP-10/S%03d", newID));
        }
    }

    private boolean isDataValid() {
        String name = txtName.getText();
        boolean dataValid = true;
        txtName.getStyleClass().remove("invalid");

        if (!name.matches("[A-z ]+")) {
            txtName.requestFocus();
            txtName.selectAll();
            txtName.getStyleClass().add("invalid");
            dataValid = false;
        }
        return dataValid;
    }

    @FXML
    void tblStudentOnKeyReleased(KeyEvent event) {
        if (event.getCode() == KeyCode.DELETE) btnDelete.fire();
    }

}
