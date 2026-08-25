import {BaseResponse, MemberInfo, Pageable, toBaseResponse} from "@/lib/models";
import {MemberMetrics} from "./models";

export const memberService = {
    /**
     * 회원 목록 조회
     * @param page 조회할 페이지
     * @param size 페이지 크기
     */
    getMembers: async (page: number = 0, size: number = 10): Promise<BaseResponse<Pageable<MemberInfo>>> => {
        const response = await fetch(`/api/users?page=${page}&size=${size}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        return toBaseResponse<Pageable<MemberInfo>>(response);
    },
    getMemberMetrics: async (): Promise<BaseResponse<MemberMetrics>> => {
        const response = await fetch('/api/users/metric', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        return toBaseResponse<MemberMetrics>(response);
    }
}