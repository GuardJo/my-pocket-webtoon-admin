package org.github.guardjo.mypocketwebtoon.admin.api.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.github.guardjo.mypocketwebtoon.admin.model.request.WorkUploadRequest;
import org.github.guardjo.mypocketwebtoon.admin.model.response.BaseResponse;

@Tag(name = "작품 관리 API", description = "작품 관련 API 목록")
public interface WorkApiDocs {
    @Operation(summary = "작품 업로드", description = "인자로 주어진 데이터들을 기반으로 작품 정보를 업로드한다.")
    BaseResponse<String> uploadWork(WorkUploadRequest workUploadRequest);
}
