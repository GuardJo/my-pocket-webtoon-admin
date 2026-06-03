package org.github.guardjo.mypocketwebtoon.admin.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.http.HttpStatus;

/* 응답 기본 모델 VO */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class BaseResponse<T> {
    @Schema(description = "응답 상태", example = "200")
    int status;

    @Schema(description = "응답 상태 코드", example = "OK")
    String statusCode;

    @Schema(description = "응답 데이터")
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
