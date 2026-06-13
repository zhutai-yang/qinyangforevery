package com.tt.infrastructure.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

  private final Path baseDir;
  private final ObjectMapper objectMapper;

  public FileStorageService(
      @Value("${tt.files.base-dir:./uploads}") String baseDir, ObjectMapper objectMapper) {
    this.baseDir = Paths.get(baseDir).toAbsolutePath().normalize();
    this.objectMapper = objectMapper;
  }

  public FileMeta store(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("file is empty");
    }

    String fileId = UUID.randomUUID().toString();
    String originalName = file.getOriginalFilename();
    if (originalName == null) originalName = fileId;

    String ext = toExt(originalName);
    String contentType = file.getContentType();
    if (contentType == null || contentType.isBlank()) {
      contentType = "application/octet-stream";
    }

    Path dir = baseDir.resolve(fileId);
    try {
      Files.createDirectories(dir);
      Path dataPath = dir.resolve("data");
      try {
        // 直接落盘，避免把大文件全量读到内存
        file.transferTo(dataPath);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }

      long size = Files.size(dataPath);
      long uploadedAt = Instant.now().toEpochMilli();

      FileMeta meta =
          new FileMeta(fileId, originalName, ext, contentType, size, uploadedAt);
      objectMapper.writeValue(dir.resolve("meta.json").toFile(), meta);
      return meta;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public FileMeta loadMeta(String fileId) {
    UUID ignored = parseUuid(fileId);
    Path dir = baseDir.resolve(fileId);
    Path metaPath = dir.resolve("meta.json");
    if (!Files.exists(metaPath)) {
      throw new IllegalArgumentException("file not found: " + fileId);
    }
    try {
      return objectMapper.readValue(metaPath.toFile(), FileMeta.class);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public Path resolveDataPath(String fileId) {
    UUID ignored = parseUuid(fileId);
    return baseDir.resolve(fileId).resolve("data");
  }

  public List<FileMeta> listLatest(int limit) {
    if (limit <= 0) return List.of();
    if (!Files.exists(baseDir)) return List.of();

    try (Stream<Path> entries = Files.list(baseDir)) {
      return entries
          .filter(Files::isDirectory)
          .map(dir -> dir.resolve("meta.json"))
          .filter(Files::exists)
          .map(
              metaPath -> {
                try {
                  return objectMapper.readValue(metaPath.toFile(), FileMeta.class);
                } catch (IOException e) {
                  throw new UncheckedIOException(e);
                }
              })
          .sorted(Comparator.comparingLong(FileMeta::uploadedAtEpochMillis).reversed())
          .limit(limit)
          .collect(Collectors.toList());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public void delete(String fileId) {
    UUID ignored = parseUuid(fileId);
    Path dir = baseDir.resolve(fileId);
    if (!Files.exists(dir)) return;
    // 删除目录下内容
    Path data = dir.resolve("data");
    Path meta = dir.resolve("meta.json");
    try {
      Files.deleteIfExists(meta);
      Files.deleteIfExists(data);
      Files.deleteIfExists(dir);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private UUID parseUuid(String fileId) {
    if (fileId == null || fileId.isBlank()) {
      throw new IllegalArgumentException("invalid fileId");
    }
    try {
      return UUID.fromString(fileId.trim());
    } catch (Exception e) {
      throw new IllegalArgumentException("invalid fileId");
    }
  }

  private String toExt(String originalName) {
    if (originalName == null) return "";
    int idx = originalName.lastIndexOf('.');
    if (idx < 0 || idx == originalName.length() - 1) return "";
    String ext = originalName.substring(idx + 1).trim().toLowerCase();
    // 只保留字母/数字，避免奇怪扩展名影响 Content-Disposition
    ext = ext.replaceAll("[^a-z0-9]", "");
    return ext;
  }
}

