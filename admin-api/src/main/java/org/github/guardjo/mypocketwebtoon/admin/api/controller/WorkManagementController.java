package org.github.guardjo.mypocketwebtoon.admin.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.github.guardjo.mypocketwebtoon.admin.api.docs.WorkApiDocs;
import org.github.guardjo.mypocketwebtoon.admin.model.request.WorkUploadRequest;
import org.github.guardjo.mypocketwebtoon.admin.model.response.BaseResponse;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.WorkInfo;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.WorkSummary;
import org.github.guardjo.mypocketwebtoon.admin.service.WorkService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
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
    public BaseResponse<Page<WorkSummary>> getWorks(@PageableDefault(sort = "modifiedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET : /api/v1/works, pageNumber = {}, pageSize = {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<WorkSummary> workSummaries = workService.getWorkSummaries(pageable);

        return BaseResponse.of(HttpStatus.OK, workSummaries);
    }

    @GetMapping("/{workId}")
    @Override
    public BaseResponse<WorkInfo> getWorkInfo(@PathVariable Long workId) {
        log.info("GET : /api/v1/works/" + workId + ", workId = {}", workId);

        // TODO 기능 연동
        return null;
    }
}
