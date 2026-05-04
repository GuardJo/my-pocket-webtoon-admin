package org.github.guardjo.mypocketwebtoon.admin.api.controller;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.github.guardjo.mypocketwebtoon.admin.config.StaticResourceConfig;
import org.github.guardjo.mypocketwebtoon.admin.exception.WorkUploadException;
import org.github.guardjo.mypocketwebtoon.admin.model.request.WorkUploadRequest;
import org.github.guardjo.mypocketwebtoon.admin.model.response.BaseResponse;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.WorkSummary;
import org.github.guardjo.mypocketwebtoon.admin.service.WorkService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = WorkManagementController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = StaticResourceConfig.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
class WorkManagementControllerTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkService workService;

    @DisplayName("POST: /api/v1/works - 정상 요청이면 작품 업로드를 수행한다")
    @Test
    void test_uploadWork_success() throws Exception {
        WorkUploadRequest uploadRequest = new WorkUploadRequest(
                "테스트 작품",
                "작품 설명",
                "COMPLETED",
                true,
                mockThumbnailFile(),
                mockEpisodeFile()
        );

        String response = mockMvc.perform(multipart("/api/v1/works")
                        .file((MockMultipartFile) uploadRequest.thumbnailFile())
                        .file((MockMultipartFile) uploadRequest.episodeFile())
                        .param("title", uploadRequest.title())
                        .param("description", uploadRequest.description())
                        .param("serialState", uploadRequest.serialState())
                        .param("visibility", String.valueOf(uploadRequest.visibility()))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        JavaType baseResponseType = objectMapper.getTypeFactory().constructParametricType(BaseResponse.class, String.class);
        BaseResponse<String> actual = objectMapper.readValue(response, baseResponseType);

        assertThat(actual.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.OK.name());
        assertThat(actual.getData()).isEqualTo("Successes");

        then(workService).should().uploadWork(eq(uploadRequest));
    }

    @DisplayName("POST: /api/v1/works - 요청 값 검증 실패(빈 제목)")
    @Test
    void test_uploadWork_fail_invalidRequest_blankTitle() throws Exception {
        WorkUploadRequest uploadRequest = new WorkUploadRequest(
                "",
                "작품 설명",
                "COMPLETED",
                true,
                null,
                mockEpisodeFile()
        );

        String response = mockMvc.perform(multipart("/api/v1/works")
                        .file((MockMultipartFile) uploadRequest.episodeFile())
                        .param("title", uploadRequest.title())
                        .param("description", uploadRequest.description())
                        .param("serialState", uploadRequest.serialState())
                        .param("visibility", String.valueOf(uploadRequest.visibility()))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        JavaType baseResponseType = objectMapper.getTypeFactory().constructParametricType(BaseResponse.class, String.class);
        BaseResponse<String> actual = objectMapper.readValue(response, baseResponseType);

        assertThat(actual.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST.name());
        assertThat(actual.getData()).isEqualTo("요청 값이 올바르지 않습니다.");

        then(workService).should(never()).uploadWork(any());
    }

    @DisplayName("POST: /api/v1/works - 요청 값 검증 실패(회차 파일 누락)")
    @Test
    void test_uploadWork_fail_invalidRequest_missingEpisodeFile() throws Exception {
        WorkUploadRequest uploadRequest = new WorkUploadRequest(
                "테스트 작품",
                "작품 설명",
                "COMPLETED",
                true,
                null,
                null
        );

        String response = mockMvc.perform(multipart("/api/v1/works")
                        .param("title", uploadRequest.title())
                        .param("description", uploadRequest.description())
                        .param("serialState", uploadRequest.serialState())
                        .param("visibility", String.valueOf(uploadRequest.visibility()))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        JavaType baseResponseType = objectMapper.getTypeFactory().constructParametricType(BaseResponse.class, String.class);
        BaseResponse<String> actual = objectMapper.readValue(response, baseResponseType);

        assertThat(actual.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST.name());
        assertThat(actual.getData()).isEqualTo("요청 값이 올바르지 않습니다.");

        then(workService).should(never()).uploadWork(any());
    }

    @DisplayName("POST: /api/v1/works - 서비스 예외 발생 시 500 응답을 반환한다")
    @Test
    void test_uploadWork_fail_when_serviceThrowsException() throws Exception {
        willThrow(new WorkUploadException("작품 업로드 처리에 실패했습니다.", new IllegalStateException("upload failed")))
                .given(workService)
                .uploadWork(any(WorkUploadRequest.class));

        String response = mockMvc.perform(multipart("/api/v1/works")
                        .file(mockEpisodeFile())
                        .file(mockThumbnailFile())
                        .param("title", "테스트 작품")
                        .param("description", "작품 설명")
                        .param("serialState", "COMPLETED")
                        .param("visibility", "true")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        JavaType baseResponseType = objectMapper.getTypeFactory().constructParametricType(BaseResponse.class, String.class);
        BaseResponse<String> actual = objectMapper.readValue(response, baseResponseType);

        assertThat(actual.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.name());
        assertThat(actual.getData()).isEqualTo("작품 업로드 처리 중 오류가 발생했습니다.");

        then(workService).should().uploadWork(any(WorkUploadRequest.class));
    }

    @DisplayName("POST: /api/v1/works - 데이터 무결성 예외 발생 시 400 응답을 반환한다")
    @Test
    void test_uploadWork_fail_when_dataIntegrityViolationOccurs() throws Exception {
        willThrow(new DataIntegrityViolationException("duplicated title"))
                .given(workService)
                .uploadWork(any(WorkUploadRequest.class));

        String response = mockMvc.perform(multipart("/api/v1/works")
                        .file(mockEpisodeFile())
                        .file(mockThumbnailFile())
                        .param("title", "테스트 작품")
                        .param("description", "작품 설명")
                        .param("serialState", "COMPLETED")
                        .param("visibility", "true")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        JavaType baseResponseType = objectMapper.getTypeFactory().constructParametricType(BaseResponse.class, String.class);
        BaseResponse<String> actual = objectMapper.readValue(response, baseResponseType);

        assertThat(actual.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST.name());
        assertThat(actual.getData()).isEqualTo("요청 값이 올바르지 않습니다.");

        then(workService).should().uploadWork(any(WorkUploadRequest.class));
    }

    @DisplayName("GET : /api/v1/works - 작품 목록 반환")
    @Test
    @WithMockUser
    void test_getWorks() throws Exception {
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("modifiedAt")));
        List<WorkSummary> content = List.of(new WorkSummary(
                1L,
                "/uploads/thumbnail/work-list.png",
                "목록 조회용 작품",
                "COMPLETED",
                true
        ));
        Page<WorkSummary> workSummaries = new PageImpl<>(content, pageRequest, 1);

        given(workService.getWorkSummaries(eq(pageRequest))).willReturn(workSummaries);

        String response = mockMvc.perform(get("/api/v1/works")
                        .param("page", String.valueOf(pageRequest.getPageNumber()))
                        .param("size", String.valueOf(pageRequest.getPageSize())))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        JavaType pageResponseType = objectMapper.getTypeFactory()
                .constructParametricType(Page.class, WorkSummary.class);
        JavaType baseResponseType = objectMapper.getTypeFactory()
                .constructParametricType(BaseResponse.class, pageResponseType);
        BaseResponse<WorkSummaryPageResponse<WorkSummary>> actual = objectMapper.readValue(response, baseResponseType);

        assertThat(actual.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.OK.name());
        assertThat(actual.getData().totalElements()).isEqualTo(1L);
        assertThat(actual.getData().content()).isEqualTo(content);

        then(workService).should().getWorkSummaries(eq(pageRequest));
    }

    @DisplayName("GET : /api/v1/works - 작품 데이터가 없는 경우")
    @Test
    void test_getWorks_empty_data() throws Exception {
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("modifiedAt")));
        Page<WorkSummary> workSummaries = Page.empty(pageRequest);

        given(workService.getWorkSummaries(eq(pageRequest))).willReturn(workSummaries);

        String response = mockMvc.perform(get("/api/v1/works")
                        .param("page", String.valueOf(pageRequest.getPageNumber()))
                        .param("size", String.valueOf(pageRequest.getPageSize())))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        JavaType pageResponseType = objectMapper.getTypeFactory()
                .constructParametricType(WorkSummaryPageResponse.class, WorkSummary.class);
        JavaType baseResponseType = objectMapper.getTypeFactory()
                .constructParametricType(BaseResponse.class, pageResponseType);
        BaseResponse<WorkSummaryPageResponse<WorkSummary>> actual = objectMapper.readValue(response, baseResponseType);

        assertThat(actual.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.OK.name());
        assertThat(actual.getData().totalElements()).isZero();
        assertThat(actual.getData().content()).isEmpty();

        then(workService).should().getWorkSummaries(eq(pageRequest));
    }

    private MockMultipartFile mockThumbnailFile() {
        return new MockMultipartFile(
                "thumbnailFile",
                "thumbnail.png",
                "image/png",
                "thumbnail-content".getBytes()
        );
    }

    private MockMultipartFile mockEpisodeFile() {
        return new MockMultipartFile(
                "episodeFile",
                "episodes.tar",
                "application/tar",
                "episode-content".getBytes()
        );
    }

    private record WorkSummaryPageResponse<T>(
            List<T> content,
            long totalElements
    ) {
    }
}
