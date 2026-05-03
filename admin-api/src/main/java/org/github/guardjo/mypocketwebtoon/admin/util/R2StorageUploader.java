package org.github.guardjo.mypocketwebtoon.admin.util;

import lombok.extern.slf4j.Slf4j;
import org.github.guardjo.mypocketwebtoon.admin.config.properties.R2StorageProperties;
import org.github.guardjo.mypocketwebtoon.admin.exception.WorkUploadException;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.StoredFile;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URI;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

@Slf4j
public class R2StorageUploader extends AbstractStorageUploader {
    private final S3Client r2Client;
    private final String bucketName;
    private final String publicBaseUrl;

    public R2StorageUploader(R2StorageProperties r2StorageProperties) {
        this(r2StorageProperties, S3Client.builder()
                .endpointOverride(URI.create(String.format("https://%s.r2.cloudflarestorage.com", r2StorageProperties.accountId())))
                .region(Region.of("auto"))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(r2StorageProperties.accessKey(), r2StorageProperties.secretKey())
                ))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .chunkedEncodingEnabled(false)
                        .build())
                .build());
    }

    R2StorageUploader(R2StorageProperties r2StorageProperties, S3Client r2Client) {
        this.r2Client = r2Client;
        this.bucketName = r2StorageProperties.bucketName();
        this.publicBaseUrl = r2StorageProperties.publicBaseUrl();
    }


    @Override
    public StoredFile upload(MultipartFile file, String directory) {
        validateFile(file);

        try {
            log.debug("Upload file, fileName = {}", file.getOriginalFilename());
            PutObjectRequest putObjectRequest = generatePutObjectRequest(file, directory);

            r2Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

            log.debug("Uploaded file, fileName = {}, storedName = {}", file.getOriginalFilename(), putObjectRequest.key());
            return generateStoredFile(putObjectRequest, file.getOriginalFilename(), file.getSize());
        } catch (IOException e) {
            throw new WorkUploadException("R2 스토리지에 파일을 저장하지 못했습니다.", e);
        }
    }

    @Override
    public StoredFile upload(byte[] content, String originalFilename, String directory) {
        log.debug("upload fileContent, fileName = {}", originalFilename);
        validateContent(content);

        Path targetFile = generateTargetFilePath(originalFilename, directory);
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(targetFile.toString())
                    .contentType(resolveContentType(targetFile))
                    .contentLength((long) content.length)
                    .build();

            r2Client.putObject(putObjectRequest, RequestBody.fromBytes(content));

            log.debug("Uploaded fileContent, fileName = {}, storedName = {}", originalFilename, putObjectRequest.key());
            return generateStoredFile(putObjectRequest, originalFilename, putObjectRequest.contentLength());
        } catch (IOException e) {
            throw new WorkUploadException("R2 스토리지에 파일을 저장하지 못했습니다.", e);
        }
    }

    @Override
    public void delete(StoredFile file) {
        String objectKey = file.storedFilename();

        log.debug("Delete file, storedName = {}", objectKey);

        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        r2Client.deleteObject(deleteObjectRequest);

        log.debug("Deleted storedName = {}", file.storedFilename());
    }

    /*
    R2 스토리지 업로드 요청 객체 생성
     */
    private PutObjectRequest generatePutObjectRequest(MultipartFile file, String directory) {
        Path targetDirectory = generateTargetDirectory(directory);

        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String storedFilename = createStoredFilename(originalFilename);
        Path targetFile = targetDirectory.resolve(storedFilename).normalize();

        return PutObjectRequest.builder()
                .bucket(bucketName)
                .key(targetFile.toString())
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();
    }

    /*
    R2 스토리지에 업로드할 파일 구성
     */
    private Path generateTargetFilePath(String fileName, String directory) {
        Path targetDirectory = generateTargetDirectory(directory);

        String sanitizedFilename = sanitizeFilename(fileName);
        String storedFilename = createStoredFilename(sanitizedFilename);

        return targetDirectory.resolve(storedFilename).normalize();
    }

    /*
    업로드 파일의 Content-Type 추정
     */
    private String resolveContentType(Path targetFile) throws IOException {
        String contentType = Files.probeContentType(targetFile);
        if (StringUtils.hasText(contentType)) {
            return contentType;
        }

        return URLConnection.guessContentTypeFromName(targetFile.getFileName().toString());
    }

    /*
    업로드 디렉터리 경로 구성
     */
    private Path generateTargetDirectory(String directory) {
        String normaizedDirectory = normalizeDirectory(directory);
        Path targetDirectory = Paths.get(normaizedDirectory).normalize();

        if (!targetDirectory.startsWith(targetDirectory)) {
            throw new IllegalArgumentException("업로드 디렉터리 경로가 올바르지 않습니다.");
        }

        return targetDirectory;
    }

    /*
    파일 저장 정보 VO 객체 생성
     */
    private StoredFile generateStoredFile(PutObjectRequest putObjectRequest, String originalFilename, long fileSize) {
        return new StoredFile(
                originalFilename,
                putObjectRequest.key(),
                putObjectRequest.key(),
                publicBaseUrl + "/" + putObjectRequest.key(),
                fileSize
        );
    }
}
