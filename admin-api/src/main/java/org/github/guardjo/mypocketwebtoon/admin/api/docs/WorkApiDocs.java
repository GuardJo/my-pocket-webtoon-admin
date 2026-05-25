package org.github.guardjo.mypocketwebtoon.admin.api.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.github.guardjo.mypocketwebtoon.admin.model.request.WorkUpdateRequest;
import org.github.guardjo.mypocketwebtoon.admin.model.request.WorkUploadRequest;
import org.github.guardjo.mypocketwebtoon.admin.model.response.BaseResponse;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.EpisodeInfo;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.WorkInfo;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.WorkSummary;
import org.github.guardjo.mypocketwebtoon.admin.security.AdminUserPrincipal;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;

@Tag(name = "작품 관리 API", description = "작품 관련 API 목록")
public interface WorkApiDocs {
    @Operation(summary = "작품 업로드", description = "인자로 주어진 데이터들을 기반으로 작품 정보를 업로드한다.")
    BaseResponse<String> uploadWork(WorkUploadRequest workUploadRequest);

    @Operation(summary = "작품 목록 조회", description = "현재 등록된 작품 목록을 조회한다.")
    BaseResponse<PagedModel<WorkSummary>> getWorks(@ParameterObject Pageable pageable);

    @Operation(summary = "특정 작품 조회", description = "식별키에 해당하는 작품 정보를 조회한다.")
    BaseResponse<WorkInfo> getWorkInfo(@Parameter(description = "작품 식별키") Long workId);

    @Operation(summary = "특정 작품 내 에피소드 목록 조회", description = "특정 작품 내 에피소드 정보 목록을 조회한다")
    BaseResponse<PagedModel<EpisodeInfo>> getEpisodes(@ParameterObject Pageable pageable,
                                                      @Parameter(description = "작품 식별키") Long workId);

    @Operation(summary = "작품 삭제", description = "식별키에 해당하는 작품 및 관련 데이터를 삭제한다.")
    BaseResponse<String> removeWork(Long workId, @Parameter(hidden = true) AdminUserPrincipal principal);

    @Operation(summary = "작품 정보 수정", description = "주어진 데이터들을 기반으로 작품 정보를 업데이트한다.")
    BaseResponse<String> updateWork(Long workId, WorkUpdateRequest workUpdateRequest);
}
