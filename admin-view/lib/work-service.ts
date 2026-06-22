import {BaseResponse, Pageable, toBaseResponse, WorkInfo} from "@/lib/models";

export const workService = {
    getWorks: async (page: number = 0, size: number = 10): Promise<BaseResponse<Pageable<WorkInfo>>> => {
        const response = await fetch(`/api/works?page=${page}&size=${size}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        return toBaseResponse<Pageable<WorkInfo>>(response);
    }
};