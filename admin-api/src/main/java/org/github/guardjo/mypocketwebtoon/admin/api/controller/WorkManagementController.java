package org.github.guardjo.mypocketwebtoon.admin.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.github.guardjo.mypocketwebtoon.admin.api.docs.WorkApiDocs;
import org.github.guardjo.mypocketwebtoon.admin.model.request.WorkUploadRequest;
import org.github.guardjo.mypocketwebtoon.admin.model.response.BaseResponse;
import org.github.guardjo.mypocketwebtoon.admin.service.WorkService;
import org.springframework.http.MediaType;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
