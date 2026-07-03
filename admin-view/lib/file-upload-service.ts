import {BaseResponse, SerialState, toBaseResponse} from "@/lib/models";

const uploadBaseUrl = process.env.NEXT_PUBLIC_UPLOAD_BASE_URL;

/**
 * 대용량 파일 업로드와 같은 경우엔 proxy에서 메모리 리소스 제한이 걸리기 때문에 BE에 직접 요청하도록 하기 위한 서비스 레이어
 */
export const fileUploadService = {
    uploadWork: async (uploadFormData: WorkUploadFormData): Promise<BaseResponse<string>> => {
        const formData = new FormData();
        formData.append('title', uploadFormData.title);
        formData.append('description', uploadFormData.description)
        formData.append('serialState', uploadFormData.serialState);
        formData.append('visibility', uploadFormData.visibility.toString());

        if (uploadFormData.thumbnailFile) {
            formData.append('thumbnailFile', uploadFormData.thumbnailFile);
        }

        if (uploadFormData.episodeFile) {
            formData.append('episodeFile', uploadFormData.episodeFile);
        }

        const response = await fetch(`${uploadBaseUrl}/api/v1/works`, {
            method: 'POST',
            credentials: 'include', // cookie에 있는 인증 토큰 사용
            body: formData
        });

        return toBaseResponse<string>(response);
    }
}

/*
작품 업로드 요청 form 데이터
 */
export interface WorkUploadFormData {
    title: string;
    description: string;
    serialState: SerialState;
    visibility: boolean;
    thumbnailFile: File | null;
    episodeFile: File | null;
}