package com.geekbrains.cloud_storage;

import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.ConnectException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client extends Application {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    private static Network network;
    private static GUI gui;
    private static Auth auth;

    private String host = "localhost";
    private int port = 8189;

    public static void main(String[] args) throws Exception {
        launch(args);

        // TODO daemon close after fin main thread
    }

    public static Network getNetwork() {
        return network;
    }

    public static GUI getGui() {
        return gui;
    }

    public static Auth getAuth() {
        return auth;
    }

    @Override
    public void init() {
        // init logger
        Common.initLogger(LOGGER);

        // init networking
        this.initNetwork();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        gui = new GUI(primaryStage);
        auth = new Auth();
    }

    /*
    * TODO KeepAlive packet
    * TODO Append auth key in restricted area
    * TODO Save and restore Auth key using conf file
    * */
    private void initNetwork() {
        new Thread(() -> {
            while (true) {
                try {
                    Client.network = new Network(this.host, this.port);
                    Client.network.run();
                } catch (ConnectException ignored) {
                    LOGGER.log(Level.WARNING, "Disconnected. Re-Connecting...");
                } catch (IOException e) {
                    e.printStackTrace();
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