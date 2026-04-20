package org.github.guardjo.mypocketwebtoon.admin.util;

import org.github.guardjo.mypocketwebtoon.admin.model.vo.StoredFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface FileStorageUploader {
    /**
     * 주어진 파일에 대해 스토리지 내 해당 디렉터리 경로에 업로드
     *
     * @param file      업로드할 파일
     * @param directory 저장할 파일 디렉터리 경로
     * @return 스토리지에 저장돤 파일 경로
     */
    StoredFile upload(MultipartFile file, String directory);

    /**
     * 주어진 입력 스트림을 원본 파일명 기준으로 스토리지 내 해당 디렉터리에 업로드한다.
     *
     * @param inputStream      업로드할 입력 스트림
     * @param originalFilename 저장할 원본 파일명
     * @param directory        저장할 파일 디렉터리 경로
     * @return 스토리지에 저장된 파일 경로
     */
    StoredFile upload(InputStream inputStream, String originalFilename, String directory);

    /**
     * 주어진 저장 파일 정보를 기반으로 파일을 삭제한다.
     *
     * @param file 삭제할 저장 파일 정보
     */
    void delete(StoredFile file);
}
