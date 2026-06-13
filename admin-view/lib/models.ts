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