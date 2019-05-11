package com.geekbrains.cloud_storage;

import com.geekbrains.cloud_storage.Action.AccountSignUpRequest;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import javafx.stage.StageStyle;

public class SignUpController {
    public static final String SCENE_TITLE = "Регистрация";

    @FXML
    public TextField loginTextField;
    @FXML
    public TextField emailTextField;
    @FXML
    public TextField passwordTextField;
    @FXML
    public TextField repeatPasswordTextField;
    @FXML
    public Hyperlink signInLink;
    @FXML
    public Button signUpButton;

    @FXML
    public void handleSignInLinkOnAction(ActionEvent actionEvent) {
        Client.getGui().getSignUpStage().close();
        Client.getGui().getSignInStage().show();
    }

    @FXML
    public void handleSignUpButtonOnAction(ActionEvent actionEvent) {
        if (! Client.getNetwork().isSocketWritable()) {
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

        new AccountSignUpRequest(this.loginTextField.getText(), this.emailTextField.getText(), this.passwordTextField.getText());

        // TODO показать loader пока не придет ответ от сервера

//        Client.signInStage.close();
//        Client.mainStage.show();
    }
}
