package org.github.guardjo.mypocketwebtoon.admin.api.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.github.guardjo.mypocketwebtoon.admin.model.request.WorkUploadRequest;
import org.github.guardjo.mypocketwebtoon.admin.model.response.BaseResponse;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.WorkInfo;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.WorkSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Tag(name = "작품 관리 API", description = "작품 관련 API 목록")
public interface WorkApiDocs {
    @Operation(summary = "작품 업로드", description = "인자로 주어진 데이터들을 기반으로 작품 정보를 업로드한다.")
    BaseResponse<String> uploadWork(WorkUploadRequest workUploadRequest);

    @Operation(summary = "작품 목록 조회", description = "현재 등록된 작품 목록을 조회한다.")
    BaseResponse<Page<WorkSummary>> getWorks(@Parameter(name = "페이지 처리") Pageable pageable);

    @Operation(summary = "특정 작품 조회", description = "식별키에 해당하는 작품 정보를 조회한다.")
    BaseResponse<WorkInfo> getWorkInfo(@Parameter(name = "workId", in = ParameterIn.PATH) Long workId);
}
