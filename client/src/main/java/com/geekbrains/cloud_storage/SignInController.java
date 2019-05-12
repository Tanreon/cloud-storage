package com.geekbrains.cloud_storage;

import com.geekbrains.cloud_storage.Action.AccountSignInRequest;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class SignInController {
    public static final String SCENE_TITLE = "Вход";

    @FXML
    public TextField loginTextField;
    @FXML
    public TextField passwordTextField;
    @FXML
    public Button signInButton;
    @FXML
    public Hyperlink signUpLink;

    @FXML
    public void handleSignUpLinkOnAction(ActionEvent actionEvent) throws IOException {
        Stage stage = (Stage) this.signUpLink.getScene().getWindow();
        stage.hide();

        Client.getGui().initSignUpScene();
        Client.getGui().getSignUpStage().show();
    }

    public void handleSignInButtonOnAction(ActionEvent actionEvent) throws IOException {
        if (! Client.getNetwork().isOpen()) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initStyle(StageStyle.UTILITY);
                alert.setTitle("Ошибка");
                alert.setHeaderText("Ошибка сети");
                alert.setContentText("Запрошенная операция не может быть выполнена. Выполняется переподключение, попробуйте через несколько минут.");
                alert.showAndWait();
            });

            return;
        }

        new AccountSignInRequest(this.loginTextField.getText(), this.passwordTextField.getText());

        // TODO показать loader пока не придет ответ от сервера

//         todo каким то образом происходит понимание того ответ от сервера ОШИБКА или ОК
//        if (Client.getAuth().isSignedIn()) {
//            Client.getGui().getSignInStage().close();
//            Client.getGui().getMainStage().show();
//        }
    }

//    private void showSignUpScene() throws IOException {
//        FXMLLoader fxmlLoader = new FXMLLoader();
//        fxmlLoader.setLocation(getClass().getResource("/signUp.fxml"));
//
//        Stage stage = new Stage();
//        stage.setTitle(String.format("%s: %s", GUI.WINDOW_TITLE, SignUpController.SCENE_TITLE));
//        stage.setScene(new Scene(fxmlLoader.load()));
//        stage.show();
//
//        Client.getGui().setSignUpStage(stage);
//    }
}
