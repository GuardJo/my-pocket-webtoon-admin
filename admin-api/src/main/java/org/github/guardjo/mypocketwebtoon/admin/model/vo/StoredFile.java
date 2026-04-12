package org.github.guardjo.mypocketwebtoon.admin.model.vo;

public record StoredFile(
        String originalFilename,
        String storedFilename,
        String absolutePath,
        String publicUrl,
        long size
) {
}
