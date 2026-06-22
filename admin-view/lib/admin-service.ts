import {BaseResponse, toBaseResponse} from "@/lib/models";

export const adminService = {
    me: async (): Promise<BaseResponse<AdminProfile>> => {

        const response = await fetch('/api/auth/me', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            },
        });

        return toBaseResponse<AdminProfile>(response);
    }
}

// 관리자 프로필 정보
export interface AdminProfile {
    id: string;
    roleName: string;
}