export interface BaseResponse<T> {
    status: number,
    statusCode: string,
    data: T
}

export async function toBaseResponse<T>(response: Response): Promise<BaseResponse<T>> {
    try {
        return await response.json() as BaseResponse<T>;
    } catch {
        throw new Error('Invalid response format');
    }
}
