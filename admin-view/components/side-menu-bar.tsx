'use client';

import Link from 'next/link';
import {usePathname} from 'next/navigation';
import {BookOpen, MoreVertical, Settings, Shield, User, Users,} from 'lucide-react';
import {DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger,} from '@/components/ui/dropdown-menu';
import {useQuery} from "@tanstack/react-query";

export type MenuItem = {
    label: string;
    href: string;
    icon: React.ComponentType<{ className: string }>;
}

export default function SideMenuBar() {
    const {data, isLoading} = useQuery({
        queryKey: ['admin-profile'],
        queryFn: async () => {
            const response = await fetch('/auth/me');
            return response.json();
        }
    });

    const pathname = usePathname();
    const menuItems: MenuItem[] = [
        {
            label: '작품 관리',
            href: '/works',
            icon: BookOpen,
        },
        {
            label: '회원 관리',
            href: '/members',
            icon: Users,
        },
        {
            label: '관리자 관리',
            href: '/admins',
            icon: Shield,
        },
    ];

    const isActive = (href: string) => {
        return pathname === href || pathname.startsWith(href + '/');
    };

    return (
        <aside className="w-64 bg-gradient-to-b from-blue-700 to-blue-800 text-white min-h-screen flex flex-col">
            <div className="p-6 border-b border-blue-600">
                <h1 className="text-xl font-bold">My Pocket Webtoon</h1>
                <p className="text-blue-200 text-sm font-semibold tracking-wider">
                    MANAGEMENT
                </p>
            </div>

            <nav className="flex-1 px-4 py-6">
                <div className="space-y-2">
                    {menuItems.map((item) => {
                        const Icon = item.icon;
                        const active = isActive(item.href);
                        return (
                            <Link
                                key={item.href}
                                href={item.href}
                                className={`flex items-center gap-3 px-4 py-3 rounded-lg transition-colors ${
                                    active
                                        ? 'bg-blue-600 text-white'
                                        : 'text-blue-100 hover:bg-blue-600/50'
                                }`}
                            >
                                <Icon className="w-5 h-5"/>
                                <span className="font-medium">{item.label}</span>
                            </Link>
                        );
                    })}
                </div>
            </nav>

            <div className="px-4 py-4 border-t border-blue-600 space-y-2">
                <Link
                    href="#"
                    className="flex items-center gap-3 px-4 py-3 rounded-lg text-blue-100 hover:bg-blue-600/50 transition-colors"
                >
                    <Settings className="w-5 h-5"/>
                    <span className="font-medium">SETTINGS</span>
                </Link>
            </div>

            <div className="px-4 py-4 border-t border-blue-600">
                <div className="flex items-center gap-3">
                    <User
                        className="w-10 h-10 bg-green-400 rounded-full flex items-center justify-center text-sm font-bold text-white">
                    </User>
                    <div className="flex-1">
                        <p className="text-sm font-semibold">{isLoading ? 'loading...' : data?.data.id}</p>
                        <p className="text-xs text-blue-200">{isLoading ? 'loading...' : data?.data.roleName}</p>
                    </div>
                    <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                            <button className="p-1 hover:bg-blue-600/50 rounded">
                                <MoreVertical className="w-4 h-4"/>
                            </button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end" className="w-48">
                            <DropdownMenuItem>Profile Settings</DropdownMenuItem>
                            <DropdownMenuItem>Change Password</DropdownMenuItem>
                            <DropdownMenuItem>Logout</DropdownMenuItem>
                        </DropdownMenuContent>
                    </DropdownMenu>
                </div>
            </div>
        </aside>
    );
}
