import {BaseResponse, Pageable, toBaseResponse, WorkInfo} from "@/lib/models";

const API_BASE_URL = process.env.BACKEND_API_URL;

export const workService = {
    getWorks: async (page: number = 0, size: number = 10, accessToken: string): Promise<BaseResponse<Pageable<WorkInfo>>> => {
        const response = await fetch(`${API_BASE_URL}/api/v1/works?page=${page}&size=${size}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${accessToken}`,
            }
        });

        return toBaseResponse<Pageable<WorkInfo>>(response);
    }
};