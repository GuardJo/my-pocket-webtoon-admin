export interface BaseResponse<T> {
    status: number,
    statusCode: string,
    data: T
}

export interface Pageable<T> {
    content: T[],
    page: PageInfo
}

export interface PageInfo {
    size: number,
    page: number,
    totalElements: number,
    totalPages: number,
}

export async function toBaseResponse<T>(response: Response): Promise<BaseResponse<T>> {
    try {
        return await response.json() as BaseResponse<T>;
    } catch {
        throw new Error('Invalid response format');
    }
}

/*
작품 연재 상태
 */
export type SerialState = 'PUBLISHED' | 'SUSPENDED' | 'COMPLETED'
export const SERIAL_STATE_LABEL = {
    PUBLISHED: '연재중',
    SUSPENDED: '휴재',
    COMPLETED: '완결'
} satisfies Record<SerialState, string>
export const SERIAL_STATES: SerialState[] = [
    'COMPLETED',
    'PUBLISHED',
    'SUSPENDED'
];

/*
작품 정보
 */
export interface WorkInfo {
    id: number,
    thumbnailUrl: string | null,
    title: string,
    serialState: SerialState,
    visibility: boolean
}

/*
작품 상세 정보
 */
export interface WorkDetailInfo extends WorkInfo {
    description: string,
    episodeTotalSize: number,
    lastUpdateDate: string
}

/*
에피소드 정보
 */
export interface EpisodeInfo {
    id: number,
    workId: number,
    episodeThumbnailUrl: string | null,
    episodeNo: number,
    episodeImageTotalCount: number,
    lastUpdateDate: string
}

/*
회원 정보
 */
export interface MemberInfo {
    id: string,
    name: string,
    nickname: string,
    signupDate: string,
    activate: boolean
}

/*
회원 매트릭 정보
*/
export interface MemberMetrics {
    totalUsers: number,
    activateUsers: number,
    pendingUsers: number,
    retentionRate: number,
    monthlyMemberGrowth: number,
}