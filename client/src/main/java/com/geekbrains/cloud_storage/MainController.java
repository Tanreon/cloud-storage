package com.geekbrains.cloud_storage;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class MainController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    @FXML
    public TableView<FileListCellView> clientStorageTableView;
    @FXML
    public TableColumn<FileListCellView, String> clientStorageFileNameColumn;
    @FXML
    public TableColumn<FileListCellView, String> clientStorageFileCreatedAtColumn;
    @FXML
    public TableColumn<FileListCellView, String> clientStorageFileSizeColumn;
    @FXML
    public TableView<FileListCellView> serverStorageTableView;
    @FXML
    public TableColumn<FileListCellView, String> serverStorageFileNameColumn;
    @FXML
    public TableColumn<FileListCellView, String> serverStorageFileCreatedAtColumn;
    @FXML
    public TableColumn<FileListCellView, String> serverStorageFileSizeColumn;
    @FXML
    public Label connectionStatusLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.initTableView();

        this.updateConnectionStatusLabel();
        this.updateClientStorageTableView();
        this.updateServerStorageTableView();
    }

    private void initTableView() {
        this.clientStorageFileNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getName()));

        this.clientStorageFileCreatedAtColumn.setCellValueFactory(param -> {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

            return new SimpleStringProperty(simpleDateFormat.format(param.getValue().getCreatedAt().toMillis()));
        });

        this.clientStorageFileSizeColumn.setCellValueFactory(param -> {
            String format = "%.2f %s";
            double size = 0;
            String unit = "B";

            for (FileListCellView.SizeUnit sizeUnit : FileListCellView.SizeUnit.values()) {
                size = param.getValue().getSize(sizeUnit);

                switch (sizeUnit) {
                    case YOTTABYTES:
                        unit = "YB";
                        break;
                    case ZETTABYTES:
                        unit = "ZB";
                        break;
                    case EXABYTES:
                        unit = "EB";
                        break;
                    case PETABYTES:
                        unit = "PB";
                        break;
                    case TERABYTES:
                        unit = "TB";
                        break;
                    case GIGABYTES:
                        unit = "GB";
                        break;
                    case MEGABYTES:
                        unit = "MB";
                        break;
                    case KILOBYTES:
                        unit = "KB";
                        break;
                    case BYTES:
                        unit = "B";
                        format = "%.0f %s";
                        break;
                }

                if (size < 999) {
                    break;
                }
            }

            return new SimpleStringProperty(String.format(format, size, unit));
        });
    }

    private void updateServerStorageTableView() {
        //
    }

    private void updateClientStorageTableView() {
        Path storage = Paths.get("client_storage");

        Stream<Path> files;

        try {
            files = Files.list(storage);
        } catch (NoSuchFileException e) {
            LOGGER.log(Level.WARNING, "ERROR: Local path not found");
            return;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "ERROR: IOException when reading client dir");
            e.printStackTrace();
            return;
        }

        files.forEach(path -> this.clientStorageTableView.getItems().add(new FileListCellView(path)));
    }

    private void updateConnectionStatusLabel() {
        new Thread(() -> {
            while (true) {
                Platform.runLater(() -> {
                    if (Client.getNetwork().getSocketChannel().isConnected()) {
                        this.connectionStatusLabel.setText("подключен");
                    } else {
                        this.connectionStatusLabel.setText("не подключен");
                    }
                });

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void sendAuth(ActionEvent actionEvent) throws IOException {
        Client.getNetwork().getSocketChannel().write(ByteBuffer.wrap(new byte[]{(byte) 90, (byte) 1}));
        Client.getNetwork().getSocketChannel().write(ByteBuffer.wrap(new byte[]{(byte) 0, (byte) -1, (byte) 0, (byte) -1})); // send end
        LOGGER.log(Level.INFO, "sending: new byte[] { (byte) 90, (byte) 1 }");
    }
}