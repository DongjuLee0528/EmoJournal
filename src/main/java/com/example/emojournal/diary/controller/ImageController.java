package com.example.emojournal.diary.controller;

import com.example.emojournal.diary.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/uploads")
@RequiredArgsConstructor
public class ImageController {

    private final FileUploadService fileUploadService;

    /**
     * 이미지 파일 서빙
     */
    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> serveImage(@PathVariable String filename) {
        try {
            log.debug("[IMAGE_SERVE] 이미지 요청 - 파일명: {}", filename);

            // 웹 경로 생성
            String webPath = "/uploads/" + filename;

            // 실제 파일 경로 가져오기
            Path filePath = fileUploadService.getActualFilePath(webPath);

            if (filePath == null || !Files.exists(filePath)) {
                log.warn("[IMAGE_SERVE] 파일을 찾을 수 없음 - 파일명: {}", filename);
                return ResponseEntity.notFound().build();
            }

            // 파일을 Resource로 로드
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                log.warn("[IMAGE_SERVE] 파일을 읽을 수 없음 - 파일명: {}", filename);
                return ResponseEntity.notFound().build();
            }

            // 파일 확장자에 따른 Content-Type 설정
            String contentType = determineContentType(filename);

            log.info("[IMAGE_SERVE] 이미지 서빙 성공 - 파일명: {}, ContentType: {}, 크기: {}bytes",
                    filename, contentType, Files.size(filePath));

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000") // 1년 캐시
                    .header(HttpHeaders.ETAG, "\"" + filename + "_" + Files.getLastModifiedTime(filePath).toMillis() + "\"")
                    .body(resource);

        } catch (IOException e) {
            log.error("[IMAGE_SERVE] 이미지 서빙 중 오류 - 파일명: {}, 오류: {}", filename, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error("[IMAGE_SERVE] 예상치 못한 오류 - 파일명: {}, 오류: {}", filename, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping("/{filename:.+}/info")
    public ResponseEntity<Map<String, Object>> getImageInfo(@PathVariable String filename) {
        try {
            log.debug("[IMAGE_INFO] 이미지 정보 요청 - 파일명: {}", filename);

            String webPath = "/uploads/" + filename;
            Path filePath = fileUploadService.getActualFilePath(webPath);

            if (filePath == null || !Files.exists(filePath)) {
                log.warn("[IMAGE_INFO] 파일을 찾을 수 없음 - 파일명: {}", filename);
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> info = new HashMap<>();
            info.put("filename", filename);
            info.put("size", Files.size(filePath));
            info.put("contentType", determineContentType(filename));
            info.put("lastModified", Files.getLastModifiedTime(filePath).toMillis());
            info.put("url", fileUploadService.getFileUrl(webPath));

            return ResponseEntity.ok(info);

        } catch (IOException e) {
            log.error("[IMAGE_INFO] 이미지 정보 조회 중 오류 - 파일명: {}, 오류: {}", filename, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 파일 확장자에 따른 Content-Type 결정
     */
    private String determineContentType(String filename) {
        String extension = getFileExtension(filename).toLowerCase();

        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "bmp":
                return "image/bmp";
            case "webp":
                return "image/webp";
            default:
                return "application/octet-stream";
        }
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
}