package com.geekbrains.cloud_storage;

import com.geekbrains.cloud_storage.Contract.RunnableGUI;
import com.geekbrains.cloud_storage.Controller.SignInController;
import com.geekbrains.cloud_storage.Controller.SignUpController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class GUI {
    public static final String WINDOW_TITLE = "Cloud Storage Client";

    private Stage mainStage;
    private Stage signInStage;
    private Stage signUpStage;

    public void initMainWindow(Stage primaryStage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/main.fxml"));

        this.mainStage = primaryStage;
        this.mainStage.setTitle(GUI.WINDOW_TITLE);
        this.mainStage.setScene(new Scene(fxmlLoader.load()));
        this.mainStage.setUserData(fxmlLoader.getController());
    }

    public void initSignInScene() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/signIn.fxml"));

        Stage stage = new Stage();
        stage.setTitle(String.format("%s: %s", GUI.WINDOW_TITLE, SignInController.SCENE_TITLE));
        stage.setScene(new Scene(fxmlLoader.load()));

        this.signInStage = stage;
    }

    public void initSignUpScene() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/signUp.fxml"));

        Stage stage = new Stage();
        stage.setTitle(String.format("%s: %s", GUI.WINDOW_TITLE, SignUpController.SCENE_TITLE));
        stage.setScene(new Scene(fxmlLoader.load()));

        this.signUpStage = stage;
    }

    public Stage getMainStage() {
        return mainStage;
    }

    public void setMainStage(Stage mainStage) {
        this.mainStage = mainStage;
    }

    public Stage getSignInStage() {
        return signInStage;
    }

    public void setSignInStage(Stage signInStage) {
        this.signInStage = signInStage;
    }

    public Stage getSignUpStage() {
        return signUpStage;
    }

    public void setSignUpStage(Stage signUpStage) {
        this.signUpStage = signUpStage;
    }

    public void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.initStyle(StageStyle.UTILITY);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void showErrorAlert(String title, String header, String content) {
        this.showAlert(Alert.AlertType.ERROR, title, header, content);
    }

    public void showWarningAlert(String title, String header, String content) {
        this.showAlert(Alert.AlertType.WARNING, title, header, content);
    }

    public void showInfoAlert(String title, String header, String content) {
        this.showAlert(Alert.AlertType.INFORMATION, title, header, content);
    }

    public void showConfirmAlert(String title, String header, String content) {
        this.showAlert(Alert.AlertType.CONFIRMATION, title, header, content);
    }

    public void runInThread(RunnableGUI runnable) {
        Platform.runLater(() -> runnable.run(this));
    }
}
