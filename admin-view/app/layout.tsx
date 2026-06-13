import type {Metadata} from "next";
import {Geist, Geist_Mono, Noto_Sans, Playfair_Display} from "next/font/google";
import "./globals.css";
import {cn} from "@/lib/utils";
import QueryProvider from "@/providers/query-provider";

const playfairDisplayHeading = Playfair_Display({subsets: ['latin'], variable: '--font-heading'});

const notoSans = Noto_Sans({subsets: ['latin'], variable: '--font-sans'});

const geistSans = Geist({
    variable: "--font-geist-sans",
    subsets: ["latin"],
});

const geistMono = Geist_Mono({
    variable: "--font-geist-mono",
    subsets: ["latin"],
});

export const metadata: Metadata = {
    title: "My Pocket Webtoon Admin",
    description: "My Pocket Webtoon 관리자 서비스",
};

export default function RootLayout({
                                       children,
                                   }: Readonly<{
    children: React.ReactNode;
}>) {
    return (
        <html
            lang="ko"
            className={cn("h-full", "antialiased", geistSans.variable, geistMono.variable, "font-sans", notoSans.variable, playfairDisplayHeading.variable)}
        >
        <body className="min-h-full flex flex-col">
        <QueryProvider>
            {children}
        </QueryProvider>
        </body>
        </html>
    );
}
