package org.github.guardjo.mypocketwebtoon.admin.util;

import org.github.guardjo.mypocketwebtoon.admin.config.properties.LocalStorageProperties;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.StoredFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileStorageUploaderTest {
    private final static String UPLOAD_URL_PREFIX = "/uploads";
    private FileStorageUploader fileStorageUploader;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fileStorageUploader = new LocalStorageUploader(
                new LocalStorageProperties(tempDir.toString(), UPLOAD_URL_PREFIX, "http://localhost:8080")
        );
    }

    @DisplayName("정상 파일 저장 시 로컬 스토리지에 저장하고 공개 URL을 반환한다")
    @Test
    void test_upload_success() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "thumbnail.png",
                "image/png",
                "thumbnail-content".getBytes()
        );

        StoredFile storedFile = fileStorageUploader.upload(file, "thumbnails");

        Path savedFile = Path.of(storedFile.absolutePath());
        assertThat(storedFile.originalFilename()).isEqualTo("thumbnail.png");
        assertThat(storedFile.storedFilename()).endsWith(".png");
        assertThat(storedFile.size()).isEqualTo(file.getSize());
        assertThat(storedFile.publicUrl()).startsWith("http://localhost:8080/uploads/thumbnails/");
        assertThat(savedFile).exists();
        assertThat(savedFile.getParent()).isEqualTo(tempDir.resolve("thumbnails"));
        assertThat(Files.readAllBytes(savedFile)).isEqualTo(file.getBytes());
    }

    @DisplayName("입력 스트림 업로드 시 원본 파일명과 디렉터리 구조를 유지한다")
    @Test
    void test_upload_inputStream_success() throws IOException {
        byte[] content = "episode-image-content".getBytes();
        String originalFilename = "view-padding-02-img-001.jpg";
        String filePath = "works/10/1";

        StoredFile storedFile = fileStorageUploader.upload(
                new ByteArrayInputStream(content),
                originalFilename,
                filePath
        );

        Path savedFile = Path.of(storedFile.absolutePath());
        assertThat(storedFile.originalFilename()).isEqualTo(originalFilename);
        assertThat(storedFile.storedFilename()).isEqualTo(originalFilename);
        assertThat(storedFile.size()).isEqualTo(content.length);
        assertThat(storedFile.publicUrl()).isEqualTo("http://localhost:8080" + UPLOAD_URL_PREFIX + "/" + filePath + "/" + originalFilename);
        assertThat(savedFile).exists();
        assertThat(savedFile.getParent()).isEqualTo(tempDir.resolve("works/10/1"));
        assertThat(Files.readAllBytes(savedFile)).isEqualTo(content);
    }

    @DisplayName("비어 있거나 잘못된 파일 저장 시 예외가 발생한다")
    @Test
    void test_upload_fail_when_file_is_empty_or_null() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.txt",
                "text/plain",
                new byte[0]
        );

        assertThatThrownBy(() -> fileStorageUploader.upload(emptyFile, "docs"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("저장할 파일이 비어 있습니다.");

        assertThatThrownBy(() -> fileStorageUploader.upload((MockMultipartFile) null, "docs"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("저장할 파일이 비어 있습니다.");

        assertThatThrownBy(() -> fileStorageUploader.upload(null, "episode-001.jpg", "docs"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("저장할 파일 스트림이 비어 있습니다.");
    }

    @DisplayName("디렉터리 경로가 올바르지 않으면 예외가 발생한다")
    @Test
    void test_upload_fail_when_directory_is_invalid() {
        LocalStorageUploader uploader = new LocalStorageUploader(
                new LocalStorageProperties(tempDir.toString(), UPLOAD_URL_PREFIX, null)
        );
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "episode.zip",
                "application/zip",
                "zip-content".getBytes()
        );

        assertThatThrownBy(() -> uploader.upload(file, "../outside"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상위 경로를 포함한 디렉터리는 사용할 수 없습니다.");

        assertThatThrownBy(() -> uploader.upload(new ByteArrayInputStream("file".getBytes()), "../outside.jpg", "episodes"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("업로드 파일명이 올바르지 않습니다.");
    }

    @DisplayName("저장된 파일 삭제 요청 시 로컬 스토리지에서 파일을 제거한다")
    @Test
    void test_delete_success() {
        LocalStorageUploader uploader = new LocalStorageUploader(
                new LocalStorageProperties(tempDir.toString(), UPLOAD_URL_PREFIX, null)
        );
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "thumbnail.png",
                "image/png",
                "thumbnail-content".getBytes()
        );

        StoredFile storedFile = uploader.upload(file, "thumbnails");
        Path savedFile = Path.of(storedFile.absolutePath());

        uploader.delete(storedFile);

        assertThat(savedFile).doesNotExist();
    }
}
