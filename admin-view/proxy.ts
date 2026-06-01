import {NextRequest, NextResponse} from "next/server";

export function proxy(request: NextRequest) {
    const {pathname} = request.nextUrl;
    const accessToken = request.cookies.get('accessToken')?.value;

    if (!accessToken && pathname !== '/login') {
        const loginUrl = new URL('/login', request.url);
        loginUrl.searchParams.set('redirect', pathname);

        return NextResponse.redirect(loginUrl);
    }

    return NextResponse.next();
}

export const config = {
    matcher: ['/((?!api|_next/static|_next/image|favicon.ico|.*\\..*).*)'],
};
