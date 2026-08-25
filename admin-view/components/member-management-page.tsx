'use client';

import {CheckCircle2, Clock, TrendingUp, Zap,} from 'lucide-react';
import StatCard from "@/components/stat-card";
import MemberManagementTable from "@/components/member-management-table";
import {BaseResponse, MemberMetrics} from "@/lib/models";
import {useQuery} from "@tanstack/react-query";
import {memberService} from "@/lib/member-service";


export function MembersManagementPage() {
    const {data} = useQuery<BaseResponse<MemberMetrics>>({
        queryKey: ['getMemberMetrics'],
        queryFn: async () => {
            const response = await memberService.getMemberMetrics();

            if (response.status !== 200) {
                console.error('Error: ', response.status, ', cause: ', response.data ?? 'no data');
                throw new Error('회원 매트릭 정보 조회에 실패하였습니다.');
            }

            return response;
        }
    });

    const calculateGrowthUserCount = (memberMetric: MemberMetrics) => {
        const value = memberMetric.monthlyMemberGrowth / (memberMetric.totalUsers / 100)

        return Number(value.toFixed(2))
    }

    const metrics: MemberMetrics = data?.data ?? {
        totalUsers: 0,
        activateUsers: 0,
        pendingUsers: 0,
        retentionRate: 0,
        monthlyMemberGrowth: 0,
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
                    <StatCard label="전체 사용자" value={metrics.totalUsers.toLocaleString()}
                              caption={calculateGrowthUserCount(metrics) + "% from last month"}
                              statIcon={TrendingUp}
                              valueColor="text-indigo-900" captionColor="text-orange-500"/>
                    <StatCard label="활성 사용자" value={metrics.activateUsers.toLocaleString()}
                              caption="Real-time engagement" statIcon={Zap}
                              valueColor="text-indigo-900" captionColor="text-gray-500"/>
                    <StatCard label="승인 대기중" value={metrics.pendingUsers.toLocaleString()} caption="Awaiting review"
                              statIcon={Clock}
                              valueColor="text-orange-500" captionColor="text-gray-500"/>
                    <StatCard label="유지율" value={metrics.retentionRate.toLocaleString()} caption="Target: 85%"
                              statIcon={CheckCircle2}
                              valueColor="text-indigo-900" captionColor="text-gray-500"/>
                </div>
                {/* Member Table */}
                <MemberManagementTable/>
            </div>
        </div>
    );
}
