package com.geekbrains.cs.common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

public class Common {
    public static final byte[] END_BYTES = { 0, -1, 1, -1, 0 };
    public static final int MAX_BUFFER_LENGTH = 64 * 1024;
    public static final int BUFFER_LENGTH = 60 * 1024;

    public static void initLogger(Logger logger, Level level) {
        Handler handler = new ConsoleHandler();
        handler.setLevel(level);
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
