import {EpisodeInfo} from "@/lib/models";
import Image from "next/image";

export default function EpisodeCard({workTitle, episodeInfo}: EpisodeCardProps) {
    return (
        <div
            key={episodeInfo.id}
            className="bg-white rounded-lg overflow-hidden hover:shadow-lg transition-shadow cursor-pointer"
        >
            <div className="aspect-video bg-gray-300 overflow-hidden">
                <Image src={episodeInfo.episodeThumbnailUrl ?? '/images/default-nob-image.png'} alt='에피소드 썸네일'
                       width={100} height={60} className="w-full h-full object-cover"/>
            </div>
            <div className="p-4">
                <p className="text-sm font-semibold text-indigo-600 mb-2">
                    {workTitle}
                </p>
                <p className="font-semibold text-gray-900 mb-3 line-clamp-2">
                    {episodeInfo.episodeNo}화
                </p>
                <div className="flex items-center gap-4 text-xs text-gray-600">
                    <span>📄 {episodeInfo.episodeImageTotalCount} images</span>
                    <span>{episodeInfo.lastUpdateDate}</span>
                </div>
            </div>
        </div>
    );
}

interface EpisodeCardProps {
    workTitle: string,
    episodeInfo: EpisodeInfo
}