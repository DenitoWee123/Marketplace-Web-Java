package bg.uni.isn.localmarketplace.config;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ConsoleAppLogger implements AppLogger {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final LogLevel configuredLevel;

    public ConsoleAppLogger(MarketplaceProperties properties) {
        this.configuredLevel = properties.getLogLevel();
    }

    @Override
    public void trace(String message) {
        if (LogLevel.TRACE.isEnabled(configuredLevel)) {
            print(LogLevel.TRACE.toString(), message);
        }
    }

    @Override
    public void debug(String message) {
        if (LogLevel.DEBUG.isEnabled(configuredLevel)) {
            print(LogLevel.DEBUG.toString(), message);
        }
    }

    @Override
    public void info(String message) {
        if (LogLevel.INFO.isEnabled(configuredLevel)) {
            print(LogLevel.INFO.toString(), message);
        }
    }

    @Override
    public void error(String message) {
        print(LogLevel.ERROR.toString(), message);
    }

    @Override
    public void error(String message, Throwable t) {
        print(LogLevel.ERROR.toString(), message + " - " + t.getMessage());
    }

    private void print(String level, String msg) {
        System.out.printf("[%s] [%s] %s%n", LocalDateTime.now().format(FMT), level, msg);
    }
}
