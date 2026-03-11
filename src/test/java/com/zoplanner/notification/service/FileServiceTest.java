package com.zoplanner.notification.service;

import com.zoplanner.notification.dto.FileResponse;
import com.zoplanner.notification.exception.FileNotFoundException;
import com.zoplanner.notification.model.FileEntity;
import com.zoplanner.notification.repository.FileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FileServiceTest {

    @Mock
    private FileRepository fileRepository;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private FileService fileService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testUploadFile_Success() throws IOException {
        // Arrange
        String fileName = "test-image.jpg";
        String contentType = "image/jpeg";
        long fileSize = 1024L;
        byte[] fileData = "test data".getBytes();

        when(multipartFile.getOriginalFilename()).thenReturn(fileName);
        when(multipartFile.getContentType()).thenReturn(contentType);
        when(multipartFile.getSize()).thenReturn(fileSize);
        when(multipartFile.getBytes()).thenReturn(fileData);

        FileEntity savedEntity = new FileEntity();
        savedEntity.setId(1);
        savedEntity.setFileName(fileName);
        savedEntity.setContentType(contentType);
        savedEntity.setSize(fileSize);
        savedEntity.setData(fileData);

        when(fileRepository.save(any(FileEntity.class))).thenReturn(savedEntity);

        // Act
        FileResponse response = fileService.uploadFile(multipartFile);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getId());
        assertEquals(fileName, response.getFileName());
        assertEquals(fileSize, response.getSize());
        assertEquals("/files/1", response.getUrl());

        verify(fileRepository, times(1)).save(any(FileEntity.class));
    }

    @Test
    void testReplaceFile_Success() throws IOException {
        // Arrange
        Integer fileId = 1;
        String newFileName = "new-image.jpg";
        String newContentType = "image/jpeg";
        long newFileSize = 2048L;
        byte[] newFileData = "new test data".getBytes();

        FileEntity existingEntity = new FileEntity();
        existingEntity.setId(fileId);
        existingEntity.setFileName("old-image.jpg");
        existingEntity.setContentType("image/jpeg");
        existingEntity.setSize(1024L);
        existingEntity.setData("old data".getBytes());

        when(multipartFile.getOriginalFilename()).thenReturn(newFileName);
        when(multipartFile.getContentType()).thenReturn(newContentType);
        when(multipartFile.getSize()).thenReturn(newFileSize);
        when(multipartFile.getBytes()).thenReturn(newFileData);
        when(fileRepository.findById(fileId)).thenReturn(Optional.of(existingEntity));
        when(fileRepository.save(any(FileEntity.class))).thenReturn(existingEntity);

        // Act
        FileResponse response = fileService.replaceFile(fileId, multipartFile);

        // Assert
        assertNotNull(response);
        assertEquals(fileId, response.getId());
        assertEquals(newFileName, response.getFileName());
        assertEquals(newFileSize, response.getSize());

        verify(fileRepository, times(1)).findById(fileId);
        verify(fileRepository, times(1)).save(any(FileEntity.class));
    }

    @Test
    void testReplaceFile_FileNotFound() {
        // Arrange
        Integer fileId = 999;
        when(fileRepository.findById(fileId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(FileNotFoundException.class, () -> fileService.replaceFile(fileId, multipartFile));
        verify(fileRepository, times(1)).findById(fileId);
        verify(fileRepository, never()).save(any(FileEntity.class));
    }

    @Test
    void testDeleteFile_Success() {
        // Arrange
        Integer fileId = 1;
        when(fileRepository.existsById(fileId)).thenReturn(true);

        // Act
        fileService.deleteFile(fileId);

        // Assert
        verify(fileRepository, times(1)).existsById(fileId);
        verify(fileRepository, times(1)).deleteById(fileId);
    }

    @Test
    void testDeleteFile_FileNotFound() {
        // Arrange
        Integer fileId = 999;
        when(fileRepository.existsById(fileId)).thenReturn(false);

        // Act & Assert
        assertThrows(FileNotFoundException.class, () -> fileService.deleteFile(fileId));
        verify(fileRepository, times(1)).existsById(fileId);
        verify(fileRepository, never()).deleteById(fileId);
    }

    @Test
    void testGetFile_Success() {
        // Arrange
        Integer fileId = 1;
        FileEntity fileEntity = new FileEntity();
        fileEntity.setId(fileId);
        fileEntity.setFileName("test.jpg");
        fileEntity.setContentType("image/jpeg");
        fileEntity.setSize(1024L);
        fileEntity.setData("data".getBytes());

        when(fileRepository.findById(fileId)).thenReturn(Optional.of(fileEntity));

        // Act
        FileEntity result = fileService.getFile(fileId);

        // Assert
        assertNotNull(result);
        assertEquals(fileId, result.getId());
        assertEquals("test.jpg", result.getFileName());
        verify(fileRepository, times(1)).findById(fileId);
    }

    @Test
    void testGetFile_FileNotFound() {
        // Arrange
        Integer fileId = 999;
        when(fileRepository.findById(fileId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(FileNotFoundException.class, () -> fileService.getFile(fileId));
        verify(fileRepository, times(1)).findById(fileId);
    }
}

