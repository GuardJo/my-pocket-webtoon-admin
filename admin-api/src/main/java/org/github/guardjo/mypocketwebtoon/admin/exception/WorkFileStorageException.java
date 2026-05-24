package org.github.guardjo.mypocketwebtoon.admin.exception;

public class WorkFileStorageException extends RuntimeException {
    public WorkFileStorageException(String message) {
        super(message);
    }

    public WorkFileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
