package bg.uni.isn.localmarketplace.config;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Profile("prod")
public class FileAppLogger implements AppLogger {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final LogLevel configuredLevel;
    private final String logFilePath;

    public FileAppLogger(MarketplaceProperties properties) {
        this.configuredLevel = properties.getLogLevel();
        this.logFilePath = properties.getLogFile();
        new File(logFilePath).getParentFile().mkdirs();
    }

    @Override
    public void trace(String message) {
        if (LogLevel.TRACE.isEnabled(configuredLevel)) {
            write(LogLevel.TRACE.toString(), message);
        }
    }

    @Override
    public void debug(String message) {
        if (LogLevel.DEBUG.isEnabled(configuredLevel)) {
            write(LogLevel.DEBUG.toString(), message);
        }
    }

    @Override
    public void info(String message) {
        if (LogLevel.INFO.isEnabled(configuredLevel)) {
            write(LogLevel.INFO.toString(), message);
        }
    }

    @Override
    public void error(String message) {
        write(LogLevel.ERROR.toString(), message);
    }

    @Override
    public void error(String message, Throwable t) {
        write(LogLevel.ERROR.toString(), message + " - " + t.getMessage());
    }

    private void write(String level, String message) {
        String line = String.format("[%s] [%s] %s%n", LocalDateTime.now().format(FMT), level, message);

        try (PrintWriter pw = new PrintWriter(new FileWriter(logFilePath, true))) {
            pw.print(line);
        } catch (IOException e) {
            System.err.println("Log write failed: " + e.getMessage());
        }
    }
}
