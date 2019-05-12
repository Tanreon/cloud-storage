package com.geekbrains.cloud_storage.Controller;

import com.geekbrains.cloud_storage.Action.AccountSignUpRequest;
import com.geekbrains.cloud_storage.Client;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;

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
        if (! Client.getNetworkChannel().isOpen()) {
            Client.getGui().runInThread(gui -> gui.showErrorAlert("Ошибка", "Ошибка сети", "Запрошенная операция не может быть выполнена. Выполняется переподключение, попробуйте через несколько минут."));

            return;
        }

        new AccountSignUpRequest(this.loginTextField.getText(), this.emailTextField.getText(), this.passwordTextField.getText());

        // TODO показать loader пока не придет ответ от сервера
    }
}
