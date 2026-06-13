import {NextRequest, NextResponse} from "next/server";
import {authService} from "@/lib/auth-service";

export async function GET(request: NextRequest) {
    const cookies = request.cookies;
    const accessToken: string = cookies.get('accessToken')?.value ?? '';

    const response = await authService.me(accessToken);

    return NextResponse.json(response, {status: response.status});
}