'use client'

import {useRouter} from "next/navigation";
import {ArrowLeft} from "lucide-react";
import WorkInfo from "@/components/work-info";
import EpisodeList from "@/components/episode-list";
import {WorkDetailInfo} from "@/lib/models";

export default function WorkDetailPage({params}: { params: { id: number } }) {
    const router = useRouter();

    // TODO API 연동
    const mockWork: WorkDetailInfo = {
        id: params.id,
        title: '애늙은이',
        thumbnailUrl: 'https://images.unsplash.com/photo-1549887534-f2cb8ff0bbb1?w=300&h=200&fit=crop',
        description: '애늙은이 이야기',
        serialState: 'COMPLETED',
        visibility: true,
        episodeTotalSize: 200,
        lastUpdateDate: '2026-06-06'
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
                    {/* Work Header Section */}
                    <WorkInfo workDetailInfo={mockWork}/>

                    {/* Episode List Section */}
                    <EpisodeList workId={params.id} workTitle={mockWork.title}/>
                </div>
            </div>
        </div>
    );
}