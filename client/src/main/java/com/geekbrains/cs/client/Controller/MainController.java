package com.geekbrains.cs.client.Controller;

import com.geekbrains.cs.client.Contract.SizeUnit;
import com.geekbrains.cs.client.Request.*;
import com.geekbrains.cs.client.Client;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
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
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class MainController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    @FXML
    protected TableView<AbstractFileRow> clientStorageTableView;
    @FXML
    protected TableColumn<AbstractFileRow, String> clientStorageFileNameColumn;
    @FXML
    protected TableColumn<AbstractFileRow, String> clientStorageFileCreatedAtColumn;
    @FXML
    protected TableColumn<AbstractFileRow, String> clientStorageFileSizeColumn;
    @FXML
    protected TableColumn<AbstractFileRow, String> clientStorageFileStatusColumn;
    @FXML
    protected TableView<AbstractFileRow> serverStorageTableView;
    @FXML
    protected TableColumn<AbstractFileRow, String> serverStorageFileNameColumn;
    @FXML
    protected TableColumn<AbstractFileRow, String> serverStorageFileCreatedAtColumn;
    @FXML
    protected TableColumn<AbstractFileRow, String> serverStorageFileSizeColumn;
    @FXML
    protected TableColumn<AbstractFileRow, String> serverStorageFileStatusColumn;

    @FXML
    protected Label connectionStatusLabel;

    protected Instant clientTableViewLastUpdate;
    protected Instant serverTableViewLastUpdate;

    public TableView<AbstractFileRow> getClientStorageTableView() {
        return this.clientStorageTableView;
    }

    public TableView<AbstractFileRow> getServerStorageTableView() {
        return this.serverStorageTableView;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.initTableView();

        Client.getGui().getMainStage().setOnShown(event -> {
            this.updateConnectionStatusLabel();
            this.updateClientStorageTableView(true);
            this.updateServerStorageTableView(true);
        });
    }

    public void updateClientStorageTableView(boolean force) {
        if (force || this.canUpdate(this.clientTableViewLastUpdate)) { // фиксим ненужные обновления 23838 раз в секунду
            this.clientTableViewLastUpdate = Instant.now();
        } else {
            return;
        }

        { // clear
            this.clientStorageTableView.getItems().clear();
        }

        { // update
            Path storage = Paths.get(Client.STORAGE_PATH);

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

            files.forEach(path -> {
                ClientFileRow clientFileRow = new ClientFileRow(path);
                clientFileRow.setFinishPercentage(100); // FIXME сохранять предыдущее значение в конфиг либо в filename.conf.tmp

                this.clientStorageTableView.getItems().add(clientFileRow);
            });
        }
    }

    public void updateServerStorageTableView(boolean force) {
        if (force || this.canUpdate(this.serverTableViewLastUpdate)) { // фиксим ненужные обновления 23838 раз в секунду
            this.serverTableViewLastUpdate = Instant.now();
        } else {
            return;
        }

        if (! Client.getNetworkChannel().isOpen() || ! Client.getAuth().isSignedIn()) { // TODO переделать определение состояния сокета
            return;
        }

        { // update
            new CommandFileListRequest();
        }
    }

    /*
    * FIXME глюки при скачивании большого файла
    * */
    public void onUpdateFileListButtonAction(ActionEvent actionEvent) {
        this.updateClientStorageTableView(true);
        this.updateServerStorageTableView(true);
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

                for (SizeUnit sizeUnit : SizeUnit.values()) {
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
            Callback<TableColumn.CellDataFeatures<AbstractFileRow, String>, ObservableValue<String>> callback = param -> {
                String status;

                if (param.getValue().getFinishPercentage() < 100) {
                    status = String.format("%d%%", param.getValue().getFinishPercentage());
                } else {
                    status = "OK";
                }

                return new SimpleStringProperty(status);
            };

            this.clientStorageFileStatusColumn.setCellValueFactory(callback);
            this.serverStorageFileStatusColumn.setCellValueFactory(callback);
        }

        {
            this.clientStorageTableView.setRowFactory(tableView -> {
                final TableRow<AbstractFileRow> tableRow = new TableRow<>();
                final ContextMenu contextMenu = new ContextMenu();

                MenuItem downloadMenuItem = new MenuItem("Upload");

                downloadMenuItem.setOnAction(event -> this.onClientTableRowUploadAction(tableRow));

                MenuItem renameMenuItem = new MenuItem("Rename");
                renameMenuItem.setOnAction(event -> this.onClientTableRowRenameAction(tableRow));

                MenuItem deleteMenuItem = new MenuItem("Delete");
                deleteMenuItem.setOnAction(event -> this.onClientTableRowDeleteAction(tableRow));

                contextMenu.getItems().addAll(downloadMenuItem, renameMenuItem, deleteMenuItem);

                // Set context menu on row, but use a binding to make it only show for non-empty rows:
                tableRow.contextMenuProperty().bind(Bindings.when(tableRow.emptyProperty()).then((ContextMenu) null).otherwise(contextMenu)); // FIXME все равно вылазит меню на пустом месте при ПКМ

                tableView.setContextMenu(contextMenu);

                return tableRow;
            });

            this.serverStorageTableView.setRowFactory(tableView -> {
                final TableRow<AbstractFileRow> tableRow = new TableRow<>();
                final ContextMenu contextMenu = new ContextMenu();

                MenuItem downloadMenuItem = new MenuItem("Download");
                downloadMenuItem.setOnAction(event -> this.onServerTableRowDownloadAction(tableRow));

                MenuItem renameMenuItem = new MenuItem("Rename");
                renameMenuItem.setOnAction(event -> this.onServerTableRowRenameAction(tableRow));

                MenuItem deleteMenuItem = new MenuItem("Delete");
                deleteMenuItem.setOnAction(event -> this.onServerTableRowDeleteAction(tableRow));

                contextMenu.getItems().addAll(downloadMenuItem, renameMenuItem, deleteMenuItem);

                // Set context menu on row, but use a binding to make it only show for non-empty rows:
                tableRow.contextMenuProperty().bind(Bindings.when(tableRow.emptyProperty()).then((ContextMenu) null).otherwise(contextMenu)); // FIXME все равно вылазит меню на пустом месте при ПКМ

                tableView.setContextMenu(contextMenu);

                return tableRow;
            });
        }
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

    private void onClientTableRowUploadAction(TableRow<AbstractFileRow> tableRow) {
        LOGGER.log(Level.INFO, "UPLOAD_CLIENT File command {0}", tableRow.getItem().getName());
        if (! Client.getNetworkChannel().isOpen() || ! Client.getAuth().isSignedIn()) { // TODO переделать определение состояния сокета
            return;
        }

        new UploadFileRequest(tableRow.getItem().getName());
    }

    private void onClientTableRowRenameAction(TableRow<AbstractFileRow> tableRow) {
        LOGGER.log(Level.INFO, "RENAME_CLIENT File command {0}", tableRow.getItem().getName());
        if (! Client.getNetworkChannel().isOpen() || ! Client.getAuth().isSignedIn()) { // TODO переделать определение состояния сокета
            return;
        }

        {
            String oldFileName = tableRow.getItem().getName();

            Path oldFilePath = Paths.get(Client.STORAGE_PATH, oldFileName);
            Path newFilePath = Paths.get(Client.STORAGE_PATH, "new-file-name");

            if (newFilePath.toFile().exists()) {
                LOGGER.log(Level.INFO, "New file already exists: {0}", oldFileName);
                Client.getGui().runInThread(gui -> gui.showErrorAlert("Ошибка", "Переименование", "Файл с таким именем уже существует."));
                return;
            }

            oldFilePath.toFile().renameTo(newFilePath.toFile());
        }

        this.updateClientStorageTableView(true);
    }

    private void onClientTableRowDeleteAction(TableRow<AbstractFileRow> tableRow) {
        LOGGER.log(Level.INFO, "DELETE_CLIENT File command {0}", tableRow.getItem().getName());
        if (! Client.getNetworkChannel().isOpen() || ! Client.getAuth().isSignedIn()) { // TODO переделать определение состояния сокета
            return;
        }

        {
            String fileName = tableRow.getItem().getName();

            Path filePath = Paths.get(Client.STORAGE_PATH, fileName);

            if (! filePath.toFile().exists()) {
                LOGGER.log(Level.INFO, "File not found: {0}", fileName);

                return;
            }

            filePath.toFile().delete();
        }

        this.updateClientStorageTableView(true);
    }

    private void onServerTableRowDownloadAction(TableRow<AbstractFileRow> tableRow) {
        LOGGER.log(Level.INFO, "DOWNLOAD_SERVER File command {0}", tableRow.getItem().getName());
        if (! Client.getNetworkChannel().isOpen() || ! Client.getAuth().isSignedIn()) { // TODO переделать определение состояния сокета
            return;
        }

        new DownloadFileRequest(tableRow.getItem().getName());
    }

    private void onServerTableRowRenameAction(TableRow<AbstractFileRow> tableRow) {
        LOGGER.log(Level.INFO, "RENAME_SERVER File command {0}", tableRow.getItem().getName());
        if (! Client.getNetworkChannel().isOpen() || ! Client.getAuth().isSignedIn()) { // TODO переделать определение состояния сокета
            return;
        }

        new CommandRenameFileRequest(tableRow.getItem().getName(), "new-file-name"); // TODO 2 arg
    }

    private void onServerTableRowDeleteAction(TableRow<AbstractFileRow> tableRow) {
        LOGGER.log(Level.INFO, "DELETE_SERVER File command {0}", tableRow.getItem().getName());
        if (! Client.getNetworkChannel().isOpen() || ! Client.getAuth().isSignedIn()) { // TODO переделать определение состояния сокета
            return;
        }

        new CommandDeleteFileRequest(tableRow.getItem().getName());
    }

    private boolean canUpdate(Instant lastUpdate) {
        if (lastUpdate != null && Instant.now().minus(5, ChronoUnit.SECONDS).compareTo(lastUpdate) < 0) {
            return false;
        }

        return true;
    }

    public abstract static class AbstractFileRow {
        protected String name;
        protected long size;
        protected Instant modifiedAt;
        protected Instant createdAt;
        protected int finishPercentage;

        public int getFinishPercentage() {
            return finishPercentage;
        }

        public void setFinishPercentage(int percentage) {
            this.finishPercentage = percentage;
        }

        public String getName() {
            return name;
        }

        public double getSize(SizeUnit unit) {
            return (double) size / unit.getValue();
        }

        public Instant getCreatedAt() {
            return createdAt;
        }

        public Instant getModifiedAt() {
            return modifiedAt;
        }
    }

    public static class ClientFileRow extends AbstractFileRow {
        public ClientFileRow(Path path) {
            this.name = path.toFile().getName();
            this.size = path.toFile().length();

            try {
                this.createdAt = Files.readAttributes(path, BasicFileAttributes.class).creationTime().toInstant();
                this.modifiedAt = Files.readAttributes(path, BasicFileAttributes.class).lastModifiedTime().toInstant();
            } catch (IOException e) {
                this.createdAt = Instant.EPOCH;
                this.modifiedAt = Instant.EPOCH;
            }
        }
    }

    public static class ServerFileRow extends AbstractFileRow {
        public void setName(String name) {
            this.name = name;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public void setCreatedAt(long createdAtMillis) {
            this.createdAt = Instant.ofEpochMilli(createdAtMillis);
        }

        public void setModifiedAt(long createdAtMillis) {
            this.modifiedAt = Instant.ofEpochMilli(createdAtMillis);
        }
    }
}