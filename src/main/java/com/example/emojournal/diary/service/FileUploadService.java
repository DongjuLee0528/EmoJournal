package com.example.emojournal.diary.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class FileUploadService {

    // 업로드 디렉토리 (application.properties에서 설정)
    @Value("${file.upload.path:/home/uploads/diary-images}")
    private String uploadPath;

    // 애플리케이션 기본 URL
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    // 허용된 파일 확장자
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "webp"
    );

    // 최대 파일 크기 (10MB)
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * 파일 업로드 처리
     */
    public String uploadFile(MultipartFile file) throws IOException {
        log.info("[FILE_UPLOAD] 파일 업로드 시작 - 원본명: [{}], 크기: {}bytes, ContentType: {}",
                file.getOriginalFilename(), file.getSize(), file.getContentType());

        try {
            // 파일 유효성 검증
            validateFile(file);
            log.debug("[FILE_UPLOAD] 파일 유효성 검증 통과");

            // 업로드 디렉토리 생성
            createUploadDirectory();

            // 고유한 파일명 생성
            String uniqueFileName = generateUniqueFileName(file.getOriginalFilename());
            log.debug("[FILE_UPLOAD] 고유 파일명 생성: {}", uniqueFileName);

            // 파일 저장
            Path filePath = Paths.get(uploadPath, uniqueFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            log.info("[FILE_UPLOAD] 파일 업로드 완료 - 원본: [{}], 저장: [{}], 크기: {}bytes, 경로: {}",
                    file.getOriginalFilename(), uniqueFileName, file.getSize(), filePath.toString());

            // 웹 경로 반환 (DB 저장용)
            return "/uploads/" + uniqueFileName;

        } catch (IOException e) {
            log.error("[FILE_UPLOAD] 파일 업로드 실패 - 원본명: [{}], 오류: {}",
                    file.getOriginalFilename(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 파일 삭제
     */
    public boolean deleteFile(String webPath) {
        try {
            if (webPath == null || webPath.trim().isEmpty()) {
                return false;
            }

            // 웹 경로에서 파일명 추출 (/uploads/filename -> filename)
            String fileName = webPath.startsWith("/uploads/") ?
                webPath.substring("/uploads/".length()) : webPath;

            Path filePath = Paths.get(uploadPath, fileName);
            boolean deleted = Files.deleteIfExists(filePath);

            if (deleted) {
                log.info("파일 삭제 완료: {}", fileName);
            } else {
                log.warn("삭제할 파일이 존재하지 않음: {}", fileName);
            }

            return deleted;
        } catch (IOException e) {
            log.error("파일 삭제 중 오류 발생: {}", webPath, e);
            return false;
        }
    }

    /**
     * 파일 존재 여부 확인
     */
    public boolean fileExists(String webPath) {
        if (webPath == null || webPath.trim().isEmpty()) {
            return false;
        }

        // 웹 경로에서 파일명 추출 (/uploads/filename -> filename)
        String fileName = webPath.startsWith("/uploads/") ?
            webPath.substring("/uploads/".length()) : webPath;

        Path filePath = Paths.get(uploadPath, fileName);
        return Files.exists(filePath);
    }

    /**
     * 파일의 절대 URL 생성
     */
    public String getFileUrl(String webPath) {
        if (webPath == null || webPath.trim().isEmpty()) {
            return null;
        }

        // 이미 절대 URL인 경우 그대로 반환
        if (webPath.startsWith("http")) {
            return webPath;
        }

        // 웹 경로를 절대 URL로 변환
        return baseUrl + webPath;
    }

    /**
     * 실제 파일 경로 반환 (파일 서빙용)
     */
    public Path getActualFilePath(String webPath) {
        if (webPath == null || webPath.trim().isEmpty()) {
            return null;
        }

        // 웹 경로에서 파일명 추출
        String fileName = webPath.startsWith("/uploads/") ?
            webPath.substring("/uploads/".length()) : webPath;

        return Paths.get(uploadPath, fileName);
    }

    /**
     * 파일 유효성 검증
     */
    private void validateFile(MultipartFile file) throws IOException {
        log.debug("[FILE_UPLOAD] 파일 유효성 검증 시작 - 크기: {}bytes, ContentType: {}",
                file.getSize(), file.getContentType());

        // 파일이 비어있는지 확인
        if (file.isEmpty()) {
            log.error("[FILE_UPLOAD] 유효성 검증 실패 - 빈 파일");
            throw new IOException("업로드할 파일이 비어있습니다.");
        }

        // 파일 크기 검증
        if (file.getSize() > MAX_FILE_SIZE) {
            log.error("[FILE_UPLOAD] 유효성 검증 실패 - 파일 크기 초과: {}bytes > {}bytes",
                    file.getSize(), MAX_FILE_SIZE);
            throw new IOException("파일 크기가 너무 큽니다. 최대 10MB까지 업로드 가능합니다.");
        }

        // 파일 확장자 검증
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            log.error("[FILE_UPLOAD] 유효성 검증 실패 - 유효하지 않은 파일명");
            throw new IOException("파일명이 유효하지 않습니다.");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            log.error("[FILE_UPLOAD] 유효성 검증 실패 - 지원하지 않는 확장자: [{}], 지원 형식: {}",
                    extension, ALLOWED_EXTENSIONS);
            throw new IOException("지원하지 않는 파일 형식입니다. 지원 형식: " + String.join(", ", ALLOWED_EXTENSIONS));
        }

        // 파일 타입 검증 (MIME Type)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            log.error("[FILE_UPLOAD] 유효성 검증 실패 - 지원하지 않는 MIME 타입: [{}]", contentType);
            throw new IOException("이미지 파일만 업로드 가능합니다.");
        }

        log.debug("[FILE_UPLOAD] 파일 유효성 검증 완료 - 확장자: [{}], MIME: [{}]", extension, contentType);
    }

    /**
     * 업로드 디렉토리 생성
     */
    private void createUploadDirectory() throws IOException {
        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            try {
                Files.createDirectories(uploadDir);
                log.info("[FILE_UPLOAD] 업로드 디렉토리 생성 성공: {}", uploadPath);
            } catch (IOException e) {
                log.error("[FILE_UPLOAD] 업로드 디렉토리 생성 실패: {}, 오류: {}", uploadPath, e.getMessage());
                throw new IOException("업로드 디렉토리 생성에 실패했습니다: " + e.getMessage(), e);
            }
        } else {
            log.debug("[FILE_UPLOAD] 업로드 디렉토리 이미 존재: {}", uploadPath);
        }
    }

    /**
     * 고유한 파일명 생성
     */
    private String generateUniqueFileName(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);

        return String.format("diary_%s_%s.%s", timestamp, uuid, extension);
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * 업로드 디렉토리 정보 반환
     */
    public String getUploadPath() {
        return uploadPath;
    }

    /**
     * 허용된 파일 확장자 목록 반환
     */
    public List<String> getAllowedExtensions() {
        return ALLOWED_EXTENSIONS;
    }

    /**
     * 최대 파일 크기 반환 (바이트)
     */
    public long getMaxFileSize() {
        return MAX_FILE_SIZE;
    }

    /**
     * 최대 파일 크기 반환 (읽기 쉬운 형태)
     */
    public String getMaxFileSizeReadable() {
        return (MAX_FILE_SIZE / (1024 * 1024)) + "MB";
    }
}