package com.tt.api.controller;

import com.tt.infrastructure.storage.FileMeta;
import com.tt.infrastructure.storage.FileStorageService;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/admin/files")
public class FileController {

  private final FileStorageService fileStorageService;

  public FileController(FileStorageService fileStorageService) {
    this.fileStorageService = fileStorageService;
  }

  @PostMapping("/upload")
  public ResponseEntity<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
    FileMeta meta = fileStorageService.store(file);
    String downloadUrl =
        ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/api/public/files/")
            .path(meta.fileId())
            .toUriString();
    return ResponseEntity.ok(
        Map.of(
            "ok",
            true,
            "fileId",
            meta.fileId(),
            "originalName",
            meta.originalName(),
            "size",
            meta.size(),
            "downloadUrl",
            downloadUrl));
  }

  @GetMapping("/{fileId}")
  public ResponseEntity<?> download(@PathVariable String fileId) {
    try {
      FileMeta meta = fileStorageService.loadMeta(fileId);
      Path dataPath = fileStorageService.resolveDataPath(fileId);
      Resource resource = new FileSystemResource(dataPath.toFile());

      MediaType contentType;
      try {
        contentType = MediaType.parseMediaType(meta.contentType());
      } catch (Exception e) {
        contentType = MediaType.APPLICATION_OCTET_STREAM;
      }

      String filename = meta.originalName() != null ? meta.originalName() : meta.fileId();
      String encoded =
          URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
      String contentDisposition = "attachment; filename*=UTF-8''" + encoded;

      return ResponseEntity.ok()
          .contentType(contentType)
          .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
          .contentLength(meta.size())
          .body(resource);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(404)
          .body(Map.of("ok", false, "code", "FILE_NOT_FOUND", "message", e.getMessage()));
    }
  }

  @GetMapping("/list")
  public Map<String, Object> list(@RequestParam(defaultValue = "20") int limit) {
    List<FileMeta> items = fileStorageService.listLatest(limit);
    return Map.of("ok", true, "items", items, "limit", limit);
  }

  @DeleteMapping("/{fileId}")
  public Map<String, Object> delete(@PathVariable String fileId) {
    fileStorageService.delete(fileId);
    return Map.of("ok", true);
  }
}

