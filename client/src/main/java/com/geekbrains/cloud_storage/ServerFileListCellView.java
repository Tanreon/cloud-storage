package com.geekbrains.cloud_storage;

import com.geekbrains.cloud_storage.Contract.AbstractFileListCellView;

import java.time.Instant;

public class ServerFileListCellView extends AbstractFileListCellView {
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
