package com.geekbrains.cs.client.Controller;

import com.geekbrains.cs.client.Client;
import com.geekbrains.cs.client.Request.AccountSignInRequest;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;

import java.io.IOException;

public class SignInController {
    public static final String SCENE_TITLE = "Вход";

    @FXML
    protected TextField loginTextField;
    @FXML
    protected TextField passwordTextField;
    @FXML
    protected Button signInButton;
    @FXML
    protected Hyperlink signUpLink;

    @FXML
    public void handleSignUpLinkOnAction(ActionEvent actionEvent) throws IOException {
        Client.getGui().initSignUpScene();

        Client.getGui().getSignInStage().hide();
        Client.getGui().getSignUpStage().show();
    }

    public void handleSignInButtonOnAction(ActionEvent actionEvent) throws IOException {
        if (!Client.getNetworkChannel().isOpen()) {
            Client.getGui().runInThread(gui -> gui.showErrorAlert("Ошибка", "Ошибка сети", "Запрошенная операция не может быть выполнена. Выполняется переподключение, попробуйте через несколько минут."));

            return;
        }

        new AccountSignInRequest(this.loginTextField.getText(), this.passwordTextField.getText());

        // TODO показать loader пока не придет ответ от сервера
    }
}
