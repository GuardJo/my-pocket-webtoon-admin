import {NextRequest, NextResponse} from "next/server";

const BACKEND_API_URL = process.env.BACKEND_API_URL;
const LOCAL_LOGIN_API_PATH = '/api/auth/login';

export function proxy(request: NextRequest) {
    const {pathname} = request.nextUrl;
    const accessToken = request.cookies.get('accessToken')?.value;

    if (pathname.startsWith('/api/') && pathname !== LOCAL_LOGIN_API_PATH) {
        if (!BACKEND_API_URL) {
            return NextResponse.json(
                {message: 'BACKEND_API_URL is not configured.'},
                {status: 500}
            );
        }

        const backendPathname = pathname.startsWith('/api/v1/')
            ? pathname
            : pathname.replace(/^\/api/, '/api/v1');
        const backendUrl = new URL(backendPathname, BACKEND_API_URL);
        backendUrl.search = request.nextUrl.search;

        const requestHeaders = new Headers(request.headers);

        if (accessToken) {
            requestHeaders.set('Authorization', `Bearer ${accessToken}`);
        }

        return NextResponse.rewrite(backendUrl, {
            request: {
                headers: requestHeaders,
            },
        });
    }

    if (!accessToken && pathname !== '/login' && pathname !== '/api/auth/login') {
        const loginUrl = new URL('/login', request.url);
        loginUrl.searchParams.set('redirect', pathname);

        return NextResponse.redirect(loginUrl);
    } else if (pathname === '/login' && accessToken) {
        return NextResponse.redirect(new URL('/', request.url));
    }

    return NextResponse.next();
}

export const config = {
    matcher: [
        '/api/:path*',
        '/((?!_next/static|_next/image|favicon.ico|.*\\..*).*)',
    ],
};
