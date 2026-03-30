package org.github.guardjo.mypocketwebtoon.admin.model.response;

import lombok.*;
import org.springframework.http.HttpStatus;

/* 응답 기본 모델 VO */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class BaseResponse<T> {
    int status;
    String statusCode;
    T data;

    /**
     * 기본 성공 응답
     *
     * @return 200 OK 응답
     */
    public static BaseResponse<String> defaultSuccessResponse() {
        return new BaseResponse<>(
                HttpStatus.OK.value(),
                HttpStatus.OK.name(),
                "Successes"
        );
    }

    public static <D> BaseResponse<D> of(HttpStatus httpStatus, D data) {
        return new BaseResponse<>(
                httpStatus.value(),
                httpStatus.name(),
                data
        );
    }
}
