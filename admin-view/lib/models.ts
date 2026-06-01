export interface BaseResponse<T> {
    status: number,
    statusCode: string,
    data: T
}

export async function toBaseResponse<T>(response: Response): Promise<BaseResponse<T>> {
    return await response.json() as BaseResponse<T>;
}
