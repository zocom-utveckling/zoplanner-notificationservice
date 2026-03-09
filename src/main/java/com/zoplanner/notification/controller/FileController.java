package com.zoplanner.notification.controller;

import com.zoplanner.notification.dto.FileResponse;
import com.zoplanner.notification.exception.FileStorageException;
import com.zoplanner.notification.model.FileEntity;
import com.zoplanner.notification.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/files")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);
    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("Received file upload request: {}", file.getOriginalFilename());

        try {
            FileResponse response = fileService.uploadFile(file);
            log.info("File uploaded successfully: {}", response);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("Error uploading file: {}", e.getMessage(), e);
            throw new FileStorageException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileResponse> replaceFile(
            @PathVariable Integer id,
            @RequestParam("file") MultipartFile file) {
        log.info("Received file replace request for ID: {}", id);

        try {
            FileResponse response = fileService.replaceFile(id, file);
            log.info("File replaced successfully: {}", response);
            return ResponseEntity.ok(response);
        } catch (com.zoplanner.notification.exception.FileNotFoundException e) {
            log.error("File not found with ID {}: {}", id, e.getMessage());
            throw e;  // Re-throw to let GlobalExceptionHandler return 404
        } catch (IOException e) {
            log.error("Error replacing file: {}", e.getMessage(), e);
            throw new FileStorageException("Failed to replace file: " + e.getMessage(), e);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteFile(@PathVariable Integer id) {
        log.info("Received file delete request for ID: {}", id);
        fileService.deleteFile(id);
        log.info("File deleted successfully with ID: {}", id);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "File with ID " + id + " has been successfully deleted");
        response.put("deleteFieldId", String.valueOf(id));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getFile(@PathVariable Integer id) {
        log.info("Received file download request for ID: {}", id);
        FileEntity file = fileService.getFile(id);
        log.info("File retrieved successfully: {}", file.getFileName());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFileName() + "\"")
                .body(file.getData());
    }
}






