package org.github.guardjo.mypocketwebtoon.admin.api.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.github.guardjo.mypocketwebtoon.admin.exception.WorkFileStorageException;
import org.github.guardjo.mypocketwebtoon.admin.model.response.BaseResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@Slf4j
public class BaseEndpointHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(
            exception = {
                    IllegalArgumentException.class,
                    ValidationException.class,
                    DataIntegrityViolationException.class
            }
    )
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseResponse<String> handleBadRequest(Exception e) {
        log.error("BadRequest Exception : {}", e.getMessage(), e);

        return BaseResponse.of(HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다.");
    }

    @ExceptionHandler(
            exception = {
                    UsernameNotFoundException.class,
                    BadCredentialsException.class
            }
    )
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public BaseResponse<String> handleUnauthorized(Exception e) {
        log.error("Unauthorized Exception : {}", e.getMessage(), e);

        return BaseResponse.of(HttpStatus.UNAUTHORIZED, "인증 정보가 올바르지 않습니다.");
    }

    @ExceptionHandler(exception = EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public BaseResponse<String> handleNotFoundException(Exception e) {
        log.error("Not found Exception : {}", e.getMessage(), e);

        return BaseResponse.of(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(WorkFileStorageException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public BaseResponse<String> handleWorkFileException(WorkFileStorageException e) {
        log.error("WorkUpload Exception : {}", e.getMessage(), e);

        return BaseResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "작품 파일 처리 중 오류가 발생했습니다.");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  org.springframework.http.HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        return ResponseEntity.badRequest().body(BaseResponse.of(HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다."));
    }
}
