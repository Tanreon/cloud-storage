package com.geekbrains.cs.client.Request;

import com.geekbrains.cs.client.Client;
import com.geekbrains.cs.common.ActionType;
import com.geekbrains.cs.common.Contract.OptionType;
import com.geekbrains.cs.common.OptionType.DownloadOptionType;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DownloadFileRequest {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    private final ActionType ACTION_TYPE = ActionType.DOWNLOAD;
    private final OptionType OPTION_TYPE = DownloadOptionType.FILE;

    private String fileName;

    public DownloadFileRequest(String fileName) {
        this.fileName = fileName;

        // Run protocol processing
        this.sendDataByProtocol();
    }

    private void sendDataByProtocol() {
        try {
            ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
            DataOutputStream outputStream = new DataOutputStream(byteOutputStream);

            {
                outputStream.write(new byte[]{ACTION_TYPE.getValue(), OPTION_TYPE.getValue()});
                LOGGER.log(Level.INFO, "Meta write: {0}", ACTION_TYPE);
            }

            {
                byte[] fileNameBytes = this.fileName.getBytes();
                outputStream.writeInt(fileNameBytes.length);
                outputStream.write(fileNameBytes);

                LOGGER.log(Level.INFO, "Data write: {0}", ACTION_TYPE);
            }

            {
                outputStream.write(Client.getEndBytes());
                LOGGER.log(Level.INFO, "End write: {0}", ACTION_TYPE);
            }

            Client.getNetworkChannel().writeAndFlush(byteOutputStream);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Send exception: {0}", ex.getMessage());
        }
    }
}
