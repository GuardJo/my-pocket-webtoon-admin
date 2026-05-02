package org.github.guardjo.mypocketwebtoon.admin.util;

import lombok.RequiredArgsConstructor;
import org.github.guardjo.mypocketwebtoon.admin.config.properties.LocalStorageProperties;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.StoredFile;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

@RequiredArgsConstructor
public class LocalStorageUploader extends AbstractStorageUploaderImpl {
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
    public StoredFile upload(InputStream inputStream, String originalFilename, String directory) {
        validateInputStream(inputStream);

        Path rootPath = Paths.get(localStorageProperties.uploadPath()).toAbsolutePath().normalize();
        String normalizedDirectory = normalizeDirectory(directory);
        Path targetDirectory = rootPath.resolve(normalizedDirectory).normalize();

        if (!targetDirectory.startsWith(rootPath)) {
            throw new IllegalArgumentException("업로드 디렉터리 경로가 올바르지 않습니다.");
        }

        String sanitizedFilename = sanitizeFilename(originalFilename);
        String storedFilename = createStoredFilename(sanitizedFilename);
        Path targetFile = targetDirectory.resolve(storedFilename).normalize();

        if (!targetFile.startsWith(targetDirectory)) {
            throw new IllegalArgumentException("업로드 파일 경로가 올바르지 않습니다.");
        }

        try {
            Files.createDirectories(targetDirectory);
            Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
            long fileSize = Files.size(targetFile);

            return new StoredFile(
                    sanitizedFilename,
                    storedFilename,
                    targetFile.toString(),
                    buildPublicUrl(normalizedDirectory, storedFilename),
                    fileSize
            );
        } catch (IOException e) {
            throw new UncheckedIOException("로컬 스토리지에 파일을 저장하지 못했습니다.", e);
        }
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
