package com.geekbrains.cloud_storage;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.ConnectException;
import java.util.logging.Logger;

public class Client extends Application {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());
    private static Network network;
    private String host = "localhost";
    private int port = 8189;

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    public static Network getNetwork() {
        return network;
    }

    @Override
    public void init() throws Exception {
        // init logger
        Common.initLogger(LOGGER);

        // init networking
        this.initNetwork();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/main.fxml"));
        primaryStage.setTitle("Cloud Storage Client");
        primaryStage.setScene(new Scene(root, 400, 400));
        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> {
            if (network.getSocketChannel().isConnected()) {
                try {
                    network.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            Platform.exit();
            System.exit(0);
        });
    }

    public void showAlert() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.initStyle(StageStyle.UTILITY);
            alert.setTitle("Information");
            alert.setHeaderText("test");
            alert.setContentText("test");

            alert.showAndWait();
        });
    }

    private void initNetwork() {
        showAlert();

        new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                try {
                    network = new Network(this.host, this.port);
                    network.run();
                } catch (ConnectException ignored) {
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


        }).start();
    }
}