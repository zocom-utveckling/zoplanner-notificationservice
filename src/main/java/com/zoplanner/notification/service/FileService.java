package com.zoplanner.notification.service;

import com.zoplanner.notification.dto.FileResponse;
import com.zoplanner.notification.exception.FileNotFoundException;
import com.zoplanner.notification.exception.FileStorageException;
import com.zoplanner.notification.model.FileEntity;
import com.zoplanner.notification.repository.FileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class FileService {

    private static final Logger log = LoggerFactory.getLogger(FileService.class);
    private final FileRepository fileRepository;

    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    @Transactional
    public FileResponse uploadFile(MultipartFile file) throws IOException {
        log.info("Uploading file: {} (size: {} bytes, type: {})",
                file.getOriginalFilename(), file.getSize(), file.getContentType());

        FileEntity fileEntity = new FileEntity();
        fileEntity.setFileName(file.getOriginalFilename());
        fileEntity.setContentType(file.getContentType());
        fileEntity.setSize(file.getSize());
        fileEntity.setData(file.getBytes());

        FileEntity saved = fileRepository.save(fileEntity);
        log.info("File saved successfully with ID: {}", saved.getId());

        return mapToResponse(saved);
    }

    @Transactional
    public FileResponse replaceFile(Integer id, MultipartFile file) throws IOException {
        log.info("Replacing file with ID: {}", id);

        FileEntity fileEntity = fileRepository.findById(id)
                .orElseThrow(() -> new FileNotFoundException("File not found with id: " + id));

        fileEntity.setFileName(file.getOriginalFilename());
        fileEntity.setContentType(file.getContentType());
        fileEntity.setSize(file.getSize());
        fileEntity.setData(file.getBytes());

        FileEntity updated = fileRepository.save(fileEntity);
        log.info("File replaced successfully with ID: {}", updated.getId());

        return mapToResponse(updated);
    }

    @Transactional
    public void deleteFile(Integer id) {
        log.info("Deleting file with ID: {}", id);

        if (!fileRepository.existsById(id)) {
            throw new FileNotFoundException("File not found with id: " + id);
        }

        fileRepository.deleteById(id);
        log.info("File deleted successfully with ID: {}", id);
    }

    public FileEntity getFile(Integer id) {
        log.info("Retrieving file with ID: {}", id);
        return fileRepository.findById(id)
                .orElseThrow(() -> new FileNotFoundException("File not found with id: " + id));
    }

    private FileResponse mapToResponse(FileEntity fileEntity) {
        FileResponse response = new FileResponse();
        response.setId(fileEntity.getId());
        response.setFileName(fileEntity.getFileName());
        response.setSize(fileEntity.getSize());
        response.setUrl("/files/" + fileEntity.getId());
        return response;
    }
}





