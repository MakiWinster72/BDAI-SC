package com.gcsc.studentcenter.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class BackupService {

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    private static final DateTimeFormatter FILE_DATE_FMT =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /**
     * Execute mysqldump and return the SQL file content as a byte array.
     * Saves a copy to the backups/ directory under the project root.
     * Redirects stderr to /dev/null to prevent deprecation warnings from polluting stdout.
     */
    public DumpResult dumpDatabase() throws IOException, InterruptedException {
        String[] dbInfo = parseJdbcUrl(jdbcUrl);
        String host = dbInfo[0];
        int port = Integer.parseInt(dbInfo[1]);
        String database = dbInfo[2];

        // Use sh to redirect stderr to /dev/null, avoiding MariaDB deprecation warnings in stdout
        String dumpCmd = String.format(
                "mysqldump -h %s -P %d -u %s --password=%s --single-transaction --quick --lock-tables=false --add-drop-table --add-drop-trigger --databases %s 2>/dev/null",
                host, port, username, password, database
        );

        ProcessBuilder pb = new ProcessBuilder("sh", "-c", dumpCmd);
        pb.redirectErrorStream(true);

        Process p = pb.start();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (InputStream is = p.getInputStream()) {
            byte[] buf = new byte[8192];
            int len;
            while ((len = is.read(buf)) != -1) {
                baos.write(buf, 0, len);
            }
        }

        int exitCode = p.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("mysqldump failed with exit code " + exitCode);
        }

        byte[] sqlContent = baos.toByteArray();

        // Save to backups/ directory under project root
        String filename = generateBackupFilename();
        Path backupsDir = Paths.get(System.getProperty("user.dir"), "backups");
        Files.createDirectories(backupsDir);
        Path filePath = backupsDir.resolve(filename);
        Files.write(filePath, sqlContent);

        return new DumpResult(sqlContent, filePath.toAbsolutePath().toString());
    }

    /**
     * Write SQL content to a temp file, then restore via mysql CLI using input redirect.
     * Uses mariadb client and redirects stderr to suppress deprecation warnings.
     */
    public void restoreDatabase(byte[] sqlContent) throws IOException, InterruptedException {
        String[] dbInfo = parseJdbcUrl(jdbcUrl);
        String host = dbInfo[0];
        int port = Integer.parseInt(dbInfo[1]);
        String database = dbInfo[2];

        // Write SQL to a temp file to avoid stdin buffering issues with large files
        Path tempSqlFile = Files.createTempFile("gcsc_restore_", ".sql");
        try {
            Files.write(tempSqlFile, sqlContent);

            // Use mariadb client, redirect stderr to suppress warnings
            String mysqlCmd = String.format(
                    "mariadb -h %s -P %d -u %s --password=%s %s < %s 2>/dev/null",
                    host, port, username, password, database, tempSqlFile
            );

            ProcessBuilder pb = new ProcessBuilder("sh", "-c", mysqlCmd);
            pb.redirectErrorStream(true);

            Process p = pb.start();

            // Drain stdout to prevent blocking
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try (InputStream is = p.getInputStream()) {
                byte[] buf = new byte[4096];
                int len;
                while ((len = is.read(buf)) != -1) {
                    output.write(buf, 0, len);
                }
            }

            int exitCode = p.waitFor();
            if (exitCode != 0) {
                String errMsg = output.toString();
                throw new RuntimeException("mysql restore failed with exit code " + exitCode + ": " + errMsg);
            }
        } finally {
            Files.deleteIfExists(tempSqlFile);
        }
    }

    public String generateBackupFilename() {
        return "gcsc_backup_" + LocalDateTime.now().format(FILE_DATE_FMT) + ".sql";
    }

    public record DumpResult(byte[] content, String absolutePath) {}

    private String[] parseJdbcUrl(String jdbcUrl) {
        // jdbc:mysql://localhost:3306/gcsc?...
        Pattern p = Pattern.compile("jdbc:mysql://([^:]+):(\\d+)/(\\w+)");
        Matcher m = p.matcher(jdbcUrl);
        if (!m.find()) {
            throw new RuntimeException("Unable to parse JDBC URL: " + jdbcUrl);
        }
        return new String[]{m.group(1), m.group(2), m.group(3)};
    }
}
