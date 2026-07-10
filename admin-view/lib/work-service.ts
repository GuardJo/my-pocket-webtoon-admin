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
    }
};