package org.github.guardjo.mypocketwebtoon.admin.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.github.guardjo.mypocketwebtoon.admin.api.docs.WorkApiDocs;
import org.github.guardjo.mypocketwebtoon.admin.model.request.WorkUploadRequest;
import org.github.guardjo.mypocketwebtoon.admin.model.response.BaseResponse;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.WorkSummary;
import org.github.guardjo.mypocketwebtoon.admin.service.WorkService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/works")
@Slf4j
@RequiredArgsConstructor
public class WorkManagementController implements WorkApiDocs {
    private final WorkService workService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Override
    public BaseResponse<String> uploadWork(@Valid @ModelAttribute WorkUploadRequest workUploadRequest) {
        log.info("POST : /api/v1/works, title = {}, episodesSize = {}MB",
                workUploadRequest.title(),
                DataSize.ofBytes(workUploadRequest.episodeFile().getSize()).toMegabytes());

        workService.uploadWork(workUploadRequest);

        return BaseResponse.defaultSuccessResponse();
    }

    @GetMapping
    @Override
    public BaseResponse<Page<WorkSummary>> getWorks(@PageableDefault PageRequest pageRequest) {
        log.info("GET : /api/v1/works, pageNumber = {}, pageSize = {}", pageRequest.getPageNumber(), pageRequest.getPageSize());

        // 수정일자 기준 내림차순 정렬
        pageRequest.withSort(Sort.by(Sort.Order.desc("modifiedAt")));

        // TODO 기능 연동
        return null;
    }
}
