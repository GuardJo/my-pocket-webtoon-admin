import {NextRequest, NextResponse} from "next/server";
import {workService} from "@/lib/work-service";
import {BaseResponse, Pageable, WorkInfo} from "@/lib/models";

export async function GET(request: NextRequest) {
    const {searchParams} = new URL(request.url);
    const accessToken = request.cookies.get('accessToken')?.value ?? '';

    const page: number | undefined = Number(searchParams.get('page')) || undefined;
    const size: number | undefined = Number(searchParams.get('size')) || undefined;

    const response: BaseResponse<Pageable<WorkInfo>> = await workService.getWorks(page, size, accessToken);

    return NextResponse.json(response, {status: response.status});
}