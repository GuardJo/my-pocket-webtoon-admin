'use client'

import {redirect, useRouter} from "next/navigation";
import {ArrowLeft, Loader} from "lucide-react";
import WorkInfo from "@/components/work-info";
import EpisodeList from "@/components/episode-list";
import {BaseResponse, WorkDetailInfo} from "@/lib/models";
import {useQuery} from "@tanstack/react-query";
import {workService} from "@/lib/work-service";

export default function WorkDetail({workId}: WorkDetailProps) {
    const router = useRouter();
    const {data: work, isLoading} = useQuery<WorkDetailInfo>({
        queryKey: ['getWork', workId],
        queryFn: async () => {
            const response: BaseResponse<WorkDetailInfo> = await workService.getWork(workId)

            if (response.status !== 200) {
                console.error('Error:', response.status, ', cause: ', response.data ?? 'no data');
                throw new Error('작품 조회에 실패하였습니다.');
            }

            return response.data
        },
        enabled: Number.isFinite(workId),
    });

    if (!isLoading && !work) {
        alert('작품 정보를 찾을 수 없습니다.');
        return redirect('/works');
    }

    return (
        <div className="flex-1 flex flex-col bg-gray-50">
            {/* Header */}
            <div className="border-b border-gray-200 bg-white px-8 py-6">
                <div className="flex items-center justify-between">
                    <div className="flex items-center gap-4">
                        <button
                            onClick={() => router.back()}
                            className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
                        >
                            <ArrowLeft className="w-5 h-5 text-gray-600"/>
                        </button>
                        <h1 className="text-3xl font-bold text-gray-900">작품 관리</h1>
                    </div>
                </div>
            </div>

            {/* Content */}
            <div className="flex-1 overflow-auto px-8 py-6">
                <div className="max-w-6xl">
                    {isLoading ? (
                        <div className="flex items-center justify-center h-64">
                            <Loader className="w-12 h-12 animate-spin text-gray-500"/>
                        </div>
                    ) : (
                        <>
                            {/* Work Header Section */}
                            <WorkInfo workDetailInfo={work!}/>

                            {/* Episode List Section */}
                            <EpisodeList workId={workId} workTitle={work!.title}/>
                            {/* TOOD 404 페이지 별도 구성하기 */}
                        </>
                    )}
                </div>
            </div>
        </div>
    );
}

interface WorkDetailProps {
    workId: number
}