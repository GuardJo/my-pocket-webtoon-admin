package org.github.guardjo.mypocketwebtoon.admin.util;

import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

public abstract class AbstractStorageUploaderImpl implements FileStorageUploader {
    /**
     * 파일 검증
     */
    void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("저장할 파일이 비어 있습니다.");
        }
    }

    /**
     * 절대 경로 치환
     */
    String normalizeDirectory(String directory) {
        if (!StringUtils.hasText(directory)) {
            return "";
        }

        String sanitized = directory.replace("\\", "/").trim();
        while (sanitized.startsWith("/")) {
            sanitized = sanitized.substring(1);
        }
        while (sanitized.endsWith("/")) {
            sanitized = sanitized.substring(0, sanitized.length() - 1);
        }

        if (sanitized.contains("..")) {
            throw new IllegalArgumentException("상위 경로를 포함한 디렉터리는 사용할 수 없습니다.");
        }

        return sanitized;
    }

    /**
     * 저장 파일명 검증
     */
    String sanitizeFilename(String originalFilename) {
        String cleanedFilename = StringUtils.cleanPath(Objects.requireNonNull(originalFilename)).trim();

        if (!StringUtils.hasText(cleanedFilename)) {
            throw new IllegalArgumentException("업로드 파일명이 비어 있습니다.");
        }
        if (cleanedFilename.contains("..") || cleanedFilename.contains("/") || cleanedFilename.contains("\\")) {
            throw new IllegalArgumentException("업로드 파일명이 올바르지 않습니다.");
        }

        return cleanedFilename;
    }

    /**
     * 저장할 파일명 생성
     */
    String createStoredFilename(String originalFilename) {
        String extension = StringUtils.getFilenameExtension(originalFilename);
        String uniqueName = UUID.randomUUID().toString().replace("-", "");

        if (!StringUtils.hasText(extension)) {
            return uniqueName;
        }
        return uniqueName + "." + extension;
    }

    /**
     * InputStream 검증
     */
    void validateInputStream(InputStream inputStream) {
        if (inputStream == null) {
            throw new IllegalArgumentException("저장할 파일 스트림이 비어 있습니다.");
        }
    }
}
