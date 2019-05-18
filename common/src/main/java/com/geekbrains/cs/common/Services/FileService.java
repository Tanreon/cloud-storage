package com.geekbrains.cs.common.Services;

import com.geekbrains.cs.common.Common;

import java.io.File;

public class FileService {
    public static long availableBytes(int filePart, File file) {
        long availableBytes;

        long fileLength = file.length();

        if (Common.BUFFER_LENGTH > fileLength) {
            availableBytes = fileLength;
        } else {
            if (((filePart + 1) * Common.BUFFER_LENGTH) > fileLength) {
                availableBytes = fileLength - (filePart * Common.BUFFER_LENGTH);
            } else {
                availableBytes = Common.BUFFER_LENGTH;
            }
        }

        return availableBytes;
    }
}
