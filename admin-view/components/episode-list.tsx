"use client";

import {EpisodeInfo} from "@/lib/models";
import {useState} from "react";
import EpisodeCard from "@/components/episode-card";
import {Button} from "@/components/ui/button";

const mockEpisodes: EpisodeInfo[] = [
    {
        id: 128,
        workId: 1,
        episodeNo: 128,
        episodeThumbnailUrl: 'https://images.unsplash.com/photo-1549887534-f2cb8ff0bbb1?w=300&h=200&fit=crop',
        episodeImageTotalCount: 72,
        lastUpdateDate: '2023.10.24',
    },
    {
        id: 127,
        workId: 1,
        episodeNo: 127,
        episodeThumbnailUrl: 'https://images.unsplash.com/photo-1518895949257-7621c3c786d7?w=300&h=200&fit=crop',
        episodeImageTotalCount: 68,
        lastUpdateDate: '2023.10.17',
    },
    {
        id: 126,
        workId: 1,
        episodeNo: 126,
        episodeThumbnailUrl: 'https://images.unsplash.com/photo-1507842211343-583f20270319?w=300&h=200&fit=crop',
        episodeImageTotalCount: 65,
        lastUpdateDate: '2023.10.10',
    },
    {
        id: 125,
        workId: 1,
        episodeNo: 125,
        episodeThumbnailUrl: 'https://images.unsplash.com/photo-1518895949257-7621c3c786d7?w=300&h=200&fit=crop',
        episodeImageTotalCount: 74,
        lastUpdateDate: '2023.10.03',
    },
];

export default function EpisodeList({workId, workTitle}: EpisodeListProps) {
    const [currentPage, setCurrentPage] = useState(1);
    const [currentEpisodes] = useState<EpisodeInfo[]>(mockEpisodes);

    const itemsPerPage = 4;
    const totalEpisodes = 128;
    const totalPages = Math.ceil(totalEpisodes / itemsPerPage);
    const visiblePageCount = 5;
    const halfVisiblePageCount = Math.floor(visiblePageCount / 2);
    const startPage = Math.max(
        1,
        Math.min(currentPage - halfVisiblePageCount, totalPages - visiblePageCount + 1)
    );
    const endPage = Math.min(totalPages, startPage + visiblePageCount - 1);
    const visiblePages = Array.from(
        {length: endPage - startPage + 1},
        (_, index) => startPage + index
    );
    
    const requestEpisodePage = (page: number) => {
        const nextPage = Math.min(Math.max(page, 1), totalPages);

        // TODO API 연동하기
        console.info(`${nextPage - 1} page, in works(${workId})`);

        setCurrentPage(nextPage);
    };

    return (
        <div>
            <div className="flex items-center justify-between mb-6">
                <h3 className="text-2xl font-bold text-gray-900">Episode List</h3>
            </div>
            {/* Episode Grid */}
            <div className="grid grid-cols-4 gap-6 mb-8">
                {currentEpisodes.map((episode) => (
                    <EpisodeCard key={episode.id} workTitle={workTitle} episodeInfo={episode}/>
                ))}
            </div>

            {/* Pagination */}
            <div className="flex items-center justify-center gap-2">
                <Button
                    variant="outline"
                    disabled={currentPage === 1}
                    onClick={() => requestEpisodePage(currentPage - 1)}
                >
                    ←
                </Button>
                {startPage > 1 && (
                    <Button
                        variant={currentPage === 1 ? 'default' : 'outline'}
                        onClick={() => requestEpisodePage(1)}
                        className={
                            currentPage === 1
                                ? 'bg-indigo-900 text-white'
                                : 'text-gray-700'
                        }
                    >
                        1
                    </Button>
                )}
                {startPage > 2 && <span className="text-gray-600">...</span>}
                {visiblePages.map((page) => (
                    <Button
                        key={page}
                        variant={currentPage === page ? 'default' : 'outline'}
                        onClick={() => requestEpisodePage(page)}
                        className={
                            currentPage === page
                                ? 'bg-indigo-900 text-white'
                                : 'text-gray-700'
                        }
                    >
                        {page}
                    </Button>
                ))}
                {endPage < totalPages - 1 && <span className="text-gray-600">...</span>}
                {endPage < totalPages && (
                    <Button
                        variant={currentPage === totalPages ? 'default' : 'outline'}
                        onClick={() => requestEpisodePage(totalPages)}
                        className={
                            currentPage === totalPages
                                ? 'bg-indigo-900 text-white'
                                : 'text-gray-700'
                        }
                    >
                        {totalPages}
                    </Button>
                )}
                <Button
                    variant="outline"
                    disabled={currentPage === totalPages}
                    onClick={() => requestEpisodePage(currentPage + 1)}
                >
                    →
                </Button>
            </div>
        </div>
    )
}

interface EpisodeListProps {
    workId: number,
    workTitle: string,
}
