package com.geekbrains.cloud_storage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;


public class Common {
    public static void initLogger(Logger logger) {
        Handler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        handler.setFormatter(new SimpleFormatter() {
            // Create a DateFormat to format the logger timestamp.
            private final DateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");

            @Override
            public String format(LogRecord record) {
                StringBuilder builder = new StringBuilder(1000);
                builder.append(df.format(new Date(record.getMillis()))).append(" - ");
                builder.append("[").append(record.getSourceClassName()).append(".");
                builder.append(record.getSourceMethodName()).append("] - ");
                builder.append("[").append(record.getLevel()).append("] - ");
                builder.append(formatMessage(record));
                builder.append("\n");
                return builder.toString();
            }

            public String getHead(Handler h) {
                return super.getHead(h);
            }

            public String getTail(Handler h) {
                return super.getTail(h);
            }
        });

        logger.addHandler(handler);
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);
    }
}
