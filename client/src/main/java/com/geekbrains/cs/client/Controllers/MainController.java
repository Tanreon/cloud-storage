package com.geekbrains.cs.client.Controllers;

import com.geekbrains.cs.client.Contracts.SizeUnit;
import com.geekbrains.cs.client.Requests.*;
import com.geekbrains.cs.client.Client;
import com.geekbrains.cs.common.Common;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

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
    private static final int CLIENT_UPDATE_INTERVAL = 1;
    private static final int SERVER_UPDATE_INTERVAL = 3;

    @FXML
    protected TableView<ClientFileRow> clientStorageTableView;
    @FXML
    protected TableColumn<ClientFileRow, String> clientStorageFileNameColumn;
    @FXML
    protected TableColumn<ClientFileRow, String> clientStorageFileModifiedAtColumn;
    @FXML
    protected TableColumn<ClientFileRow, String> clientStorageFileSizeColumn;
    @FXML
    protected TableColumn<ClientFileRow, String> clientStorageFileStatusColumn;
    @FXML
    protected TableView<ServerFileRow> serverStorageTableView;
    @FXML
    protected TableColumn<ServerFileRow, String> serverStorageFileNameColumn;
    @FXML
    protected TableColumn<ServerFileRow, String> serverStorageFileModifiedAtColumn;
    @FXML
    protected TableColumn<ServerFileRow, String> serverStorageFileSizeColumn;
    @FXML
    protected TableColumn<ServerFileRow, String> serverStorageFileStatusColumn;

    @FXML
    protected Label connectionStatusLabel;

    protected Instant clientTableViewLastUpdate;
    protected Instant serverTableViewLastUpdate;

    protected ObservableList<ClientFileRow> clientStorageFileRowList = FXCollections.observableArrayList();
    protected ObservableList<ServerFileRow> serverStorageFileRowList = FXCollections.observableArrayList();

    public ObservableList<ClientFileRow> getClientStorageFileRowList() {
        return this.clientStorageFileRowList;
    }

    public ObservableList<ServerFileRow> getServerStorageFileRowList() {
        return this.serverStorageFileRowList;
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
        if (force || this.canUpdate(this.clientTableViewLastUpdate, CLIENT_UPDATE_INTERVAL)) { // фиксим ненужные обновления 23838 раз в секунду
            this.clientTableViewLastUpdate = Instant.now();
        } else {
            return;
        }

        int selectedRowIndex = this.clientStorageTableView.getSelectionModel().getSelectedIndex();

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
                clientFileRow.setAvailability(100); // FIXME сохранять предыдущее значение в конфиг либо в filename.conf.tmp
                this.clientStorageFileRowList.add(clientFileRow);
            });
        }

        this.clientStorageTableView.getSelectionModel().select(selectedRowIndex);
    }

    public void updateServerStorageTableView(boolean force) {
        if (force || this.canUpdate(this.serverTableViewLastUpdate, SERVER_UPDATE_INTERVAL)) { // фиксим ненужные обновления 23838 раз в секунду
            this.serverTableViewLastUpdate = Instant.now();
        } else {
            return;
        }

        if (! Client.getNetworkChannel().isOpen() || ! Client.getAuth().isSignedIn()) { // TODO переделать определение состояния сокета
            return;
        }

        int selectedRowIndex = this.serverStorageTableView.getSelectionModel().getSelectedIndex();

        { // update
            new Thread(() -> new CommandFileListRequest(Client.getNetworkChannel())).start();
        }

        this.serverStorageTableView.getSelectionModel().select(selectedRowIndex);
    }

    public void onUpdateFileListButtonAction(ActionEvent actionEvent) {
        this.updateClientStorageTableView(true);
        this.updateServerStorageTableView(true);
    }

    private void initTableView() {
        { // init list for table
            this.clientStorageTableView.setItems(this.clientStorageFileRowList);
            this.serverStorageTableView.setItems(this.serverStorageFileRowList);
        }

        { // init file name column
            this.clientStorageFileNameColumn.setCellValueFactory(param -> param.getValue().getNameProperty());
            this.serverStorageFileNameColumn.setCellValueFactory(param -> param.getValue().getNameProperty());
        }

        { // init created date column
            this.clientStorageFileModifiedAtColumn.setCellValueFactory(param -> param.getValue().getModifiedAtProperty());
            this.serverStorageFileModifiedAtColumn.setCellValueFactory(param -> param.getValue().getModifiedAtProperty());
        }

        { // init file size column
            this.clientStorageFileSizeColumn.setCellValueFactory(param -> param.getValue().getSizeProperty());
            this.serverStorageFileSizeColumn.setCellValueFactory(param -> param.getValue().getSizeProperty());
        }

        { // init file status column
            this.clientStorageFileStatusColumn.setCellValueFactory(param -> param.getValue().getAvailabilityProperty());
            this.serverStorageFileStatusColumn.setCellValueFactory(param -> param.getValue().getAvailabilityProperty());
        }

        {
            this.clientStorageTableView.setRowFactory(tableView -> {
                final TableRow<ClientFileRow> tableRow = new TableRow<>();
                final ContextMenu contextMenu = new ContextMenu();

                MenuItem downloadMenuItem = new MenuItem("Upload");
                downloadMenuItem.setOnAction(event -> new Thread(() -> this.onClientTableRowUploadAction(tableRow)).start());

                MenuItem renameMenuItem = new MenuItem("Rename");
                renameMenuItem.setOnAction(event -> new Thread(() -> this.onClientTableRowRenameAction(tableRow)).start());

                MenuItem deleteMenuItem = new MenuItem("Delete");
                deleteMenuItem.setOnAction(event -> new Thread(() -> this.onClientTableRowDeleteAction(tableRow)).start());

                contextMenu.getItems().addAll(downloadMenuItem, renameMenuItem, deleteMenuItem);

                // Set context menu on row, but use a binding to make it only show for non-empty rows:
                tableRow.contextMenuProperty().bind(Bindings.when(tableRow.emptyProperty()).then((ContextMenu) null).otherwise(contextMenu)); // FIXME все равно вылазит меню на пустом месте при ПКМ

                tableView.setContextMenu(contextMenu);

                return tableRow;
            });

            this.serverStorageTableView.setRowFactory(tableView -> {
                final TableRow<ServerFileRow> tableRow = new TableRow<>();
                final ContextMenu contextMenu = new ContextMenu();

                MenuItem downloadMenuItem = new MenuItem("Download");
                downloadMenuItem.setOnAction(event -> new Thread(() -> this.onServerTableRowDownloadAction(tableRow)).start());

                MenuItem renameMenuItem = new MenuItem("Rename");
                renameMenuItem.setOnAction(event -> new Thread(() -> this.onServerTableRowRenameAction(tableRow)).start());

                MenuItem deleteMenuItem = new MenuItem("Delete");
                deleteMenuItem.setOnAction(event -> new Thread(() -> this.onServerTableRowDeleteAction(tableRow)).start());

                contextMenu.getItems().addAll(downloadMenuItem, renameMenuItem, deleteMenuItem);

                // Set context menu on row, but use a binding to make it only show for non-empty rows:
                tableRow.contextMenuProperty().bind(Bindings.when(tableRow.emptyProperty()).then((ContextMenu) null).otherwise(contextMenu)); // FIXME все равно вылазит меню на пустом месте при ПКМ

                tableView.setContextMenu(contextMenu);

                return tableRow;
            });
        }
    }

    private void updateConnectionStatusLabel() { // FIXME вынести в триггеры onConnected onDisconnected
//        new Thread(() -> {
//            while (true) {
//                Client.getGui().runInThread(gui -> {
//                    if (Client.getNetworkChannel().isOpen()) { // TODO переделать определение состояния сокета
//                        this.connectionStatusLabel.setText("подключен");
//                    } else {
//                        this.connectionStatusLabel.setText("не подключен");
//                    }
//                });
//
//                try {
//                    Thread.sleep(500);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
    }

    private void onClientTableRowUploadAction(TableRow<ClientFileRow> tableRow) {
        LOGGER.log(Level.INFO, "UPLOAD_CLIENT File command {0}", tableRow.getItem().getName());
        if (! Client.getNetworkChannel().isOpen() || ! Client.getAuth().isSignedIn()) { // TODO переделать определение состояния сокета
            return;
        }

        new UploadFileRequest(Client.getNetworkChannel(), tableRow.getItem().getName());
    }

    private void onClientTableRowRenameAction(TableRow<ClientFileRow> tableRow) {
        LOGGER.log(Level.INFO, "RENAME_CLIENT File command {0}", tableRow.getItem().getName());

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

    private void onClientTableRowDeleteAction(TableRow<ClientFileRow> tableRow) {
        LOGGER.log(Level.INFO, "DELETE_CLIENT File command {0}", tableRow.getItem().getName());

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

    private void onServerTableRowDownloadAction(TableRow<ServerFileRow> tableRow) {
        LOGGER.log(Level.INFO, "DOWNLOAD_SERVER File command {0}", tableRow.getItem().getName());
        if (! Client.getNetworkChannel().isOpen() || ! Client.getAuth().isSignedIn()) { // TODO переделать определение состояния сокета
            return;
        }

        new DownloadFileRequest(Client.getNetworkChannel(), tableRow.getItem().getName());
    }

    private void onServerTableRowRenameAction(TableRow<ServerFileRow> tableRow) {
        LOGGER.log(Level.INFO, "RENAME_SERVER File command {0}", tableRow.getItem().getName());
        if (! Client.getNetworkChannel().isOpen() || ! Client.getAuth().isSignedIn()) { // TODO переделать определение состояния сокета
            return;
        }

        new CommandRenameFileRequest(Client.getNetworkChannel(), tableRow.getItem().getName(), "new-file-name"); // TODO 2 arg
    }

    private void onServerTableRowDeleteAction(TableRow<ServerFileRow> tableRow) {
        LOGGER.log(Level.INFO, "DELETE_SERVER File command {0}", tableRow.getItem().getName());
        if (! Client.getNetworkChannel().isOpen() || ! Client.getAuth().isSignedIn()) { // TODO переделать определение состояния сокета
            return;
        }

        new CommandDeleteFileRequest(Client.getNetworkChannel(), tableRow.getItem().getName());
    }

    private boolean canUpdate(Instant lastUpdate, int interval) {
        if (lastUpdate != null && Instant.now().minus(interval, ChronoUnit.SECONDS).compareTo(lastUpdate) < 0) {
            return false;
        }

        return true;
    }

    public abstract static class AbstractFileRow {
        protected String name;
        protected long size;
        protected Instant modifiedAt;
        protected int availability;

        protected StringProperty nameProperty = new SimpleStringProperty();
        protected StringProperty sizeProperty = new SimpleStringProperty();
        protected StringProperty modifiedAtProperty = new SimpleStringProperty();
        protected StringProperty availabilityProperty = new SimpleStringProperty();

        public String getName() {
            return name;
        }

        public long getSize() {
            return size;
        }

        public Instant getModifiedAt() {
            return modifiedAt;
        }

        public int getAvailability() {
            return availability;
        }

        public void setSize(long size) {
            this.size = size;
            this.notifySizeProperty();
        }

        public void setAvailability(int availability) {
            this.availability = availability;
            this.notifyAvailabilityProperty();
        }


        public StringProperty getNameProperty() {
            return this.nameProperty;
        }

        public StringProperty getSizeProperty() {
            return this.sizeProperty;
        }

        public StringProperty getModifiedAtProperty() {
            return this.modifiedAtProperty;
        }

        public StringProperty getAvailabilityProperty() {
            return this.availabilityProperty;
        }

        protected void notifyNameProperty() {
            this.nameProperty.set(this.name);
        }

        protected void notifySizeProperty() {
            String format = "%.2f %s";
            double size = 0;
            String unit = "B";

            for (SizeUnit sizeUnit : SizeUnit.values()) {
                size = (double) this.size / sizeUnit.getValue();

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

            this.sizeProperty.set(String.format(format, size, unit));
        }

        protected void notifyModifiedAtProperty() {
            this.modifiedAtProperty.set(new SimpleDateFormat(Common.DATE_FORMAT).format(this.modifiedAt.toEpochMilli()));
        }

        protected void notifyAvailabilityProperty() {
            String status;

            if (this.availability < 100) {
                status = String.format("%d%%", this.availability);
            } else {
                status = "OK";
            }

            this.availabilityProperty.set(status);
        }
    }

    public static class ClientFileRow extends AbstractFileRow {
        public ClientFileRow(Path path) {
            this.name = path.toFile().getName();
            this.size = path.toFile().length();

            try {
                BasicFileAttributes basicFileAttributes = Files.readAttributes(path, BasicFileAttributes.class);
                this.modifiedAt = basicFileAttributes.lastModifiedTime().toInstant();
            } catch (IOException ex) {
                this.modifiedAt = Instant.EPOCH;
            }

            this.notifyNameProperty();
            this.notifySizeProperty();
            this.notifyModifiedAtProperty();
        }
    }

    public static class ServerFileRow extends AbstractFileRow {
        public void setName(String name) {
            this.name = name;
            this.notifyNameProperty();
        }

        public void setModifiedAt(long modifiedAtMillis) {
            this.modifiedAt = Instant.ofEpochMilli(modifiedAtMillis);
            this.notifyModifiedAtProperty();
        }
    }
}