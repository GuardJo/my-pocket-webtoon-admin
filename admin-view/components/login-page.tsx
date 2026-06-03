'use client';

import {useState} from 'react';
import {Lock, Shield, User} from 'lucide-react';
import {Button} from '@/components/ui/button';
import {Input} from '@/components/ui/input';
import {Card} from '@/components/ui/card';
import {useRouter} from "next/navigation";
import {BaseResponse} from "@/lib/models";

export default function LoginPage() {
    const router = useRouter();

    const [adminId, setAdminId] = useState('');
    const [password, setPassword] = useState('');
    const [isLoading, setIsLoading] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsLoading(true);

        try {
            const response = await fetch('/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({username: adminId, password}),
            });

            if (!response.ok) {
                const errorBody = await response.json() as BaseResponse<string>;
                alert(errorBody.data ?? errorBody.data ?? '로그인에 실패하였습니다.');
                return;
            }

            router.replace('/');
        } catch (error) {
            const message = error instanceof Error ? error.message : '알 수 없는 오류가 발생했습니다.';
            alert(message);
            console.error('Error:', error);
        } finally {
            setTimeout(() => setIsLoading(false), 1000);
        }
    };

    return (
        <div
            className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100 flex flex-col items-center justify-center px-4">
            {/* Header Section */}
            <div className="text-center mb-12">
                <div className="flex justify-center mb-6">
                    <div className="bg-blue-600 rounded-lg p-4 shadow-lg">
                        <Shield className="w-8 h-8 text-white"/>
                    </div>
                </div>
                <h1 className="text-4xl font-bold text-slate-900 mb-2">
                    My Pocket Webtoon
                </h1>
                <p className="text-sm font-semibold text-slate-500 tracking-wider">
                    ADMINISTRATIVE ACCESS ONLY
                </p>
            </div>

            {/* Login Card */}
            <Card className="w-full max-w-md shadow-xl">
                <div className="p-8">
                    {/* Card Header */}
                    <div className="mb-8">
                        <h2 className="text-2xl font-bold text-slate-900 mb-2">
                            관리자 로그인
                        </h2>
                        <p className="text-sm text-slate-600">
                            웹툰 관리 시스템에 접속하세요.
                        </p>
                    </div>

                    {/* Form */}
                    <form onSubmit={handleSubmit} className="space-y-6">
                        {/* Email Field */}
                        <div className="space-y-2">
                            <label className="text-sm font-medium text-slate-700">
                                아이디
                            </label>
                            <div className="relative">
                                <User
                                    className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-slate-400"/>
                                <Input
                                    type="text"
                                    placeholder="admin"
                                    value={adminId}
                                    onChange={(e) => setAdminId(e.target.value)}
                                    className="pl-10 bg-slate-50 border-slate-200 text-slate-900 placeholder:text-slate-400"
                                    required
                                />
                            </div>
                        </div>

                        {/* Password Field */}
                        <div className="space-y-2">
                            <label className="text-sm font-medium text-slate-700">
                                비밀번호
                            </label>
                            <div className="relative">
                                <Lock
                                    className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-slate-400"/>
                                <Input
                                    type="password"
                                    placeholder="••••••••"
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    className="pl-10 bg-slate-50 border-slate-200 text-slate-900 placeholder:text-slate-400"
                                    required
                                />
                            </div>
                        </div>

                        {/* Submit Button */}
                        <Button
                            type="submit"
                            disabled={isLoading}
                            className="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-3 rounded-lg transition-colors"
                        >
                            {isLoading ? '로그인 중...' : '로그인'}
                        </Button>
                    </form>
                </div>
            </Card>

            {/* Footer */}
            <p className="text-center text-sm text-slate-500 mt-8">
                © 2026 My Pocket Webtoon. All rights reserved.
            </p>
        </div>
    );
}
