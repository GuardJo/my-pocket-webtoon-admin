import {BaseResponse, EpisodeInfo, Pageable, toBaseResponse, WorkDetailInfo, WorkInfo} from "@/lib/models";

export const workService = {
    getWorks: async (page: number = 0, size: number = 10): Promise<BaseResponse<Pageable<WorkInfo>>> => {
        const response = await fetch(`/api/works?page=${page}&size=${size}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        return toBaseResponse<Pageable<WorkInfo>>(response);
    },
    getWork: async (workId: number): Promise<BaseResponse<WorkDetailInfo>> => {
        const response = await fetch(`/api/works/${workId}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        return toBaseResponse<WorkDetailInfo>(response);
    },
    getEpisodes: async (workId: number, page: number = 0, size: number = 10): Promise<BaseResponse<Pageable<EpisodeInfo>>> => {
        const response = await fetch(`/api/works/${workId}/episodes?page=${page}&size=${size}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        return toBaseResponse<Pageable<EpisodeInfo>>(response);
    },
    updateWork: async (workId: number, workUpdateRequest: WorkUpdateRequest): Promise<BaseResponse<string>> => {
        const response = await fetch(`/api/works/${workId}`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(workUpdateRequest)
        });

        return toBaseResponse<string>(response);
    },
    deleteWork: async (workId: number): Promise<BaseResponse<string>> => {
        const response = await fetch(`/api/works/${workId}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        return toBaseResponse<string>(response);
    }
};

// 작품 정보 수정 요청
export interface WorkUpdateRequest {
    title: string;
    description: string;
    serialState: string;
    visibility: boolean;
}