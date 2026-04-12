package org.github.guardjo.mypocketwebtoon.admin.util;

import lombok.RequiredArgsConstructor;
import org.github.guardjo.mypocketwebtoon.admin.config.properties.LocalStorageProperties;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.StoredFile;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LocalStorageUploader implements FileStorageUploader {
    private final LocalStorageProperties localStorageProperties;

    @Override
    public StoredFile upload(MultipartFile file, String directory) {
        validateFile(file);

        Path rootPath = Paths.get(localStorageProperties.uploadPath()).toAbsolutePath().normalize();
        String normalizedDirectory = normalizeDirectory(directory);
        Path targetDirectory = rootPath.resolve(normalizedDirectory).normalize();

        if (!targetDirectory.startsWith(rootPath)) {
            throw new IllegalArgumentException("업로드 디렉터리 경로가 올바르지 않습니다.");
        }

        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String storedFilename = createStoredFilename(originalFilename);
        Path targetFile = targetDirectory.resolve(storedFilename).normalize();

        try {
            Files.createDirectories(targetDirectory);
            file.transferTo(targetFile);
        } catch (IOException e) {
            throw new UncheckedIOException("로컬 스토리지에 파일을 저장하지 못했습니다.", e);
        }

        String publicUrl = buildPublicUrl(normalizedDirectory, storedFilename);

        return new StoredFile(
                originalFilename,
                storedFilename,
                targetFile.toString(),
                publicUrl,
                file.getSize()
        );
    }

    @Override
    public void delete(StoredFile file) {
        if (file == null || !StringUtils.hasText(file.absolutePath())) {
            return;
        }

        Path targetFile = Paths.get(file.absolutePath()).toAbsolutePath().normalize();
        try {
            Files.deleteIfExists(targetFile);
        } catch (IOException e) {
            throw new UncheckedIOException("로컬 스토리지에서 파일을 삭제하지 못했습니다.", e);
        }
    }

    /*
    파일 검증
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("저장할 파일이 비어 있습니다.");
        }
    }

    /*
    절대 경로 치환
     */
    private String normalizeDirectory(String directory) {
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

    /*
    저장할 파일명 생성
     */
    private String createStoredFilename(String originalFilename) {
        String extension = StringUtils.getFilenameExtension(originalFilename);
        String uniqueName = UUID.randomUUID().toString().replace("-", "");

        if (!StringUtils.hasText(extension)) {
            return uniqueName;
        }
        return uniqueName + "." + extension;
    }

    /*
    저장된 파일에 대한 외부 접근 url 구성
     */
    private String buildPublicUrl(String directory, String storedFilename) {
        StringBuilder pathBuilder = new StringBuilder(localStorageProperties.urlPrefix());
        if (!localStorageProperties.urlPrefix().endsWith("/")) {
            pathBuilder.append('/');
        }
        if (StringUtils.hasText(directory)) {
            pathBuilder.append(directory).append('/');
        }
        pathBuilder.append(storedFilename);

        String resourcePath = pathBuilder.toString().replace("//", "/");
        if (!StringUtils.hasText(localStorageProperties.publicBaseUrl())) {
            return resourcePath;
        }
        return localStorageProperties.publicBaseUrl().replaceAll("/+$", "") + resourcePath;
    }
}
