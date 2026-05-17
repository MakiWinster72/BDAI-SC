package com.gcsc.studentcenter.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Service
public class BackupService {

  @Value("${spring.datasource.url}")
  private String jdbcUrl;

  @Value("${spring.datasource.username}")
  private String username;

  @Value("${spring.datasource.password}")
  private String password;

  @Value("${app.upload-dir:./uploads}")
  private String uploadDir;

  private static final DateTimeFormatter FILE_DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

  /**
   * Execute mysqldump and return the SQL file content as a byte array.
   * Redirects stderr to /dev/null to prevent deprecation warnings from polluting
   * stdout.
   */
  public byte[] dumpDatabase() throws IOException, InterruptedException {
    String[] dbInfo = parseJdbcUrl(jdbcUrl);
    String host = dbInfo[0];
    int port = Integer.parseInt(dbInfo[1]);
    String database = dbInfo[2];

    String dumpCmd = String.format(
        "mysqldump -h %s -P %d -u %s --password=%s --single-transaction --quick --lock-tables=false --add-drop-table --add-drop-trigger --databases %s 2>/dev/null",
        host, port, username, password, database);

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

    return baos.toByteArray();
  }

  /**
   * Write SQL content to a temp file, then restore via mysql CLI using input
   * redirect.
   * Uses mariadb client and redirects stderr to suppress deprecation warnings.
   */
  public void restoreDatabase(byte[] sqlContent) throws IOException, InterruptedException {
    String[] dbInfo = parseJdbcUrl(jdbcUrl);
    String host = dbInfo[0];
    int port = Integer.parseInt(dbInfo[1]);
    String database = dbInfo[2];

    Path tempSqlFile = Files.createTempFile("bdai_sc_restore_", ".sql");
    try {
      Files.write(tempSqlFile, sqlContent);

      String mysqlCmd = String.format(
          "mariadb -h %s -P %d -u %s --password=%s %s < %s 2>/dev/null",
          host, port, username, password, database, tempSqlFile);

      ProcessBuilder pb = new ProcessBuilder("sh", "-c", mysqlCmd);
      pb.redirectErrorStream(true);

      Process p = pb.start();

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
    return "bdai_sc_backup_" + LocalDateTime.now().format(FILE_DATE_FMT) + ".sql";
  }

  public String generateAttachmentsFilename() {
    return "bdai_sc_attachments_" + LocalDateTime.now().format(FILE_DATE_FMT) + ".zip";
  }

  /**
   * Zip the uploads/ directory and return as byte array.
   */
  public byte[] dumpAttachments() throws IOException {
    Path uploadsPath = Paths.get(uploadDir).toAbsolutePath().normalize();
    if (!Files.exists(uploadsPath)) {
      return new byte[0];
    }

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ZipOutputStream zos = new ZipOutputStream(baos)) {
      Files.walk(uploadsPath)
          .filter(path -> !Files.isDirectory(path))
          .forEach(filePath -> {
            try {
              String entryName = uploadsPath.relativize(filePath).toString().replace("\\", "/");
              ZipEntry entry = new ZipEntry(entryName);
              zos.putNextEntry(entry);
              Files.copy(filePath, zos);
              zos.closeEntry();
            } catch (IOException e) {
              throw new UncheckedIOException(e);
            }
          });
    }
    return baos.toByteArray();
  }

  /**
   * Extract ZIP content to the uploads/ directory.
   * Existing files are overwritten.
   */
  public void restoreAttachments(byte[] zipContent) throws IOException {
    if (zipContent == null || zipContent.length == 0) {
      throw new RuntimeException("ZIP 文件内容为空");
    }
    Path uploadsPath = Paths.get(uploadDir).toAbsolutePath().normalize();
    Files.createDirectories(uploadsPath);

    try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipContent))) {
      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {
        Path targetPath = uploadsPath.resolve(entry.getName()).normalize();
        // Prevent zip slip vulnerability
        if (!targetPath.startsWith(uploadsPath)) {
          throw new RuntimeException("非法文件路径: " + entry.getName());
        }
        if (entry.isDirectory()) {
          Files.createDirectories(targetPath);
        } else {
          Files.createDirectories(targetPath.getParent());
          Files.copy(zis, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
        zis.closeEntry();
      }
    }
  }

  private String[] parseJdbcUrl(String jdbcUrl) {
    Pattern p = Pattern.compile("jdbc:mysql://([^:]+):(\\d+)/(\\w+)");
    Matcher m = p.matcher(jdbcUrl);
    if (!m.find()) {
      throw new RuntimeException("Unable to parse JDBC URL: " + jdbcUrl);
    }
    return new String[] { m.group(1), m.group(2), m.group(3) };
  }
}
