import {NextRequest, NextResponse} from "next/server";
import {workService} from "@/lib/work-service";
import {BaseResponse, Pageable, WorkInfo} from "@/lib/models";

export async function GET(request: NextRequest) {
    const {searchParams} = new URL(request.url);
    const accessToken = request.cookies.get('accessToken')?.value ?? '';

    const response: BaseResponse<Pageable<WorkInfo>> = await workService.getWorks(Number(searchParams.get('page')), Number(searchParams.get('size')), accessToken);

    return NextResponse.json(response, {status: response.status});
}