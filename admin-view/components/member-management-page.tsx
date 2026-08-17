'use client';

import {CheckCircle2, Clock, TrendingUp, Zap,} from 'lucide-react';
import StatCard from "@/components/stat-card";
import MemberManagementTable from "@/components/member-management-table";
import {MemberMetrics} from "@/lib/models";


export function MembersManagementPage() {
    // TODO API 연동하기
    const mockMetrics: MemberMetrics = {
        totalUsers: 12893,
        activeUsers: 1892,
        pendindUsers: 24,
        retentionRate: 80.32
    }

    return (
        <div className="flex-1 flex flex-col bg-gray-50">
            {/* Header */}
            <div className="bg-gray-50 px-8 pt-6">
                <h1 className="text-3xl font-bold text-gray-900 mb-2">회원 관리</h1>
                <p className="text-gray-600">
                    웹툰 서비스에 가입한 회원 관리 대시보드
                </p>
            </div>

            {/* Content */}
            <div className="flex-1 overflow-auto px-8 py-6">
                {/* Stat Cards */}
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
                    <StatCard label="전체 사용자" value={mockMetrics.totalUsers.toLocaleString()}
                              caption="12% from last month"
                              statIcon={TrendingUp}
                              valueColor="text-indigo-900" captionColor="text-orange-500"/>
                    <StatCard label="활성 사용자" value={mockMetrics.activeUsers.toLocaleString()}
                              caption="Real-time engagement" statIcon={Zap}
                              valueColor="text-indigo-900" captionColor="text-gray-500"/>
                    <StatCard label="승인 대기중" value={mockMetrics.pendindUsers.toLocaleString()} caption="Awaiting review"
                              statIcon={Clock}
                              valueColor="text-orange-500" captionColor="text-gray-500"/>
                    <StatCard label="유지율" value={mockMetrics.retentionRate.toLocaleString()} caption="Target: 85%"
                              statIcon={CheckCircle2}
                              valueColor="text-indigo-900" captionColor="text-gray-500"/>
                </div>
                {/* Member Table */}
                <MemberManagementTable/>
            </div>
        </div>
    );
}
