import {NextRequest, NextResponse} from "next/server";
import {authService} from "@/lib/auth-service";

export async function POST(request: NextRequest) {
    const loginRequestContent = await request.json();

    if (!loginRequestContent.username || !loginRequestContent.password) {
        return NextResponse.json(
            {message: '로그인 요청 정보가 올바르지 않습니다.'},
            {status: 400}
        )
    }

    const loginResponse = await authService.login(loginRequestContent.username, loginRequestContent.password);

    if (loginResponse.status !== 200) {
        return NextResponse.json(loginResponse, {status: 401});
    }

    const accessToken = loginResponse.data;
    const response = NextResponse.json({success: true});

    response.cookies.set('accessToken', accessToken, {
        httpOnly: true,
        secure: process.env.NODE_ENV === 'production',
        sameSite: 'lax',
        path: '/',
        maxAge: 1800
    });

    return response;
}
