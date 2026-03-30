package org.github.guardjo.mypocketwebtoon.admin.api.controller;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.github.guardjo.mypocketwebtoon.admin.model.response.BaseResponse;
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
            exception = ValidationException.class
    )
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseResponse<String> handleBadRequest(Exception e) {
        log.error("BadRequest Exception : {}", e.getMessage(), e);

        return badRequestResponse();
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

        return BaseResponse.<String>builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .statusCode(HttpStatus.UNAUTHORIZED.name())
                .data("인증 정보가 올바르지 않습니다.")
                .build();
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  org.springframework.http.HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        return ResponseEntity.badRequest().body(badRequestResponse());
    }

    private BaseResponse<String> badRequestResponse() {
        return BaseResponse.<String>builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .statusCode(HttpStatus.BAD_REQUEST.name())
                .data("요청 값이 올바르지 않습니다.")
                .build();
    }
}
