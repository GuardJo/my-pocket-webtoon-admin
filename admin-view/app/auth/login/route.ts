import {NextRequest, NextResponse} from "next/server";
import {authService} from "@/lib/auth-service";

export async function POST(request: NextRequest) {
    const loginRequestContent = await request.json();

    const loginResponse = await authService.login(loginRequestContent.username, loginRequestContent.password);

    if (loginResponse.status !== 200) {
        return NextResponse.json(
            {message: '로그인에 실패하였습니다.'},
            {status: loginResponse.status}
        );
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
