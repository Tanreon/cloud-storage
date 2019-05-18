package com.geekbrains.cs.client.Controllers;

import com.geekbrains.cs.client.Client;
import com.geekbrains.cs.client.Requests.AccountSignUpRequest;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;

public class SignUpController {
    public static final String SCENE_TITLE = "Регистрация";

    @FXML
    protected TextField loginTextField;
    @FXML
    protected TextField emailTextField;
    @FXML
    protected TextField passwordTextField;
    @FXML
    protected TextField repeatPasswordTextField;
    @FXML
    protected Hyperlink signInLink;
    @FXML
    protected Button signUpButton;

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

        new Thread(() -> new AccountSignUpRequest(Client.getNetworkChannel(), this.loginTextField.getText(), this.emailTextField.getText(), this.passwordTextField.getText())).start();

        // TODO показать loader пока не придет ответ от сервера
    }
}
