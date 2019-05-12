package com.geekbrains.cloud_storage;

import com.geekbrains.cloud_storage.Action.CommandFileListRequest;
import com.geekbrains.cloud_storage.Action.DownloadFileRequest;
import com.geekbrains.cloud_storage.Contract.AbstractFileRow;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
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
    public TableView<AbstractFileRow> clientStorageTableView;
    @FXML
    public TableColumn<AbstractFileRow, String> clientStorageFileNameColumn;
    @FXML
    public TableColumn<AbstractFileRow, String> clientStorageFileCreatedAtColumn;
    @FXML
    public TableColumn<AbstractFileRow, String> clientStorageFileSizeColumn;
    @FXML
    public TableView<AbstractFileRow> serverStorageTableView;
    @FXML
    public TableColumn<AbstractFileRow, String> serverStorageFileNameColumn;
    @FXML
    public TableColumn<AbstractFileRow, String> serverStorageFileCreatedAtColumn;
    @FXML
    public TableColumn<AbstractFileRow, String> serverStorageFileSizeColumn;
    @FXML
    public Label connectionStatusLabel;

    public TableView<AbstractFileRow> getServerStorageTableView() {
        return this.serverStorageTableView;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.initTableView();

        Client.getGui().getMainStage().setOnShown(event -> {
            this.updateConnectionStatusLabel();
            this.updateClientStorageTableView();
            this.updateServerStorageTableView();
        });
    }

    private void initTableView() {
        {
            Callback<TableColumn.CellDataFeatures<AbstractFileRow, String>, ObservableValue<String>> callback = param -> new SimpleStringProperty(param.getValue().getName());

            this.clientStorageFileNameColumn.setCellValueFactory(callback);
            this.serverStorageFileNameColumn.setCellValueFactory(callback);
        }

        {
            Callback<TableColumn.CellDataFeatures<AbstractFileRow, String>, ObservableValue<String>> callback = param -> {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss"); // FIXME вынести в отдельную константу

                return new SimpleStringProperty(simpleDateFormat.format(param.getValue().getCreatedAt().toEpochMilli()));
            };

            this.clientStorageFileCreatedAtColumn.setCellValueFactory(callback);
            this.serverStorageFileCreatedAtColumn.setCellValueFactory(callback);
        }

        {
            Callback<TableColumn.CellDataFeatures<AbstractFileRow, String>, ObservableValue<String>> callback = param -> {
                String format = "%.2f %s";
                double size = 0;
                String unit = "B";

                for (AbstractFileRow.SizeUnit sizeUnit : AbstractFileRow.SizeUnit.values()) {
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
            };

            this.clientStorageFileSizeColumn.setCellValueFactory(callback);
            this.serverStorageFileSizeColumn.setCellValueFactory(callback);
        }

        {
            this.serverStorageTableView.setRowFactory(tableView -> {
                final TableRow<AbstractFileRow> tableRow = new TableRow<>();
                final ContextMenu contextMenu = new ContextMenu();

                MenuItem downloadMenuItem = new MenuItem("Download");
                downloadMenuItem.setOnAction(event -> {
                    LOGGER.log(Level.INFO, "DOWNLOAD File command {0}", tableRow.getItem().getName());
                    if (! Client.getNetworkChannel().isOpen() || ! Client.getAuth().isSignedIn()) { // TODO переделать определение состояния сокета
                        return;
                    }

                    new Thread(() -> new DownloadFileRequest(tableRow.getItem().getName())).start();
                });

//                MenuItem deleteMenuItem = new MenuItem("Delete"); // TODO реализовать функционал
//                deleteMenuItem.setOnAction(event -> {
//                    System.out.println("delete item " + tableRow.getItem().getName());
//                });

                contextMenu.getItems().addAll(downloadMenuItem);

                // Set context menu on row, but use a binding to make it only show for non-empty rows:
                tableRow.contextMenuProperty().bind(Bindings.when(tableRow.emptyProperty()).then((ContextMenu) null).otherwise(contextMenu));

                tableView.setContextMenu(contextMenu);

                return tableRow;
            });
        }
    }

    private void updateServerStorageTableView() {
        if (! Client.getNetworkChannel().isOpen() || ! Client.getAuth().isSignedIn()) { // TODO переделать определение состояния сокета
            return;
        }

        new Thread(CommandFileListRequest::new).start();
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

        files.forEach(path -> this.clientStorageTableView.getItems().add(new ClientFileRow(path)));
    }

    private void updateConnectionStatusLabel() {
        new Thread(() -> {
            while (true) {
                Client.getGui().runInThread(gui -> {
                    if (Client.getNetworkChannel().isOpen()) { // TODO переделать определение состояния сокета
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
}