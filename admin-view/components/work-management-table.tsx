'use client';

import {useState} from 'react';
import {useRouter} from 'next/navigation';
import {Button} from '@/components/ui/button';
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow,} from '@/components/ui/table';
import {Badge} from '@/components/ui/badge';
import {Eye, EyeClosed, Plus} from 'lucide-react';
import {BaseResponse, Pageable, SerialState, WorkInfo} from "@/lib/models";
import Image from "next/image";
import {useQuery} from "@tanstack/react-query";
import {workService} from "@/lib/work-service";

function getStatusBadgeColor(
    status: SerialState
): 'default' | 'secondary' | 'destructive' {
    switch (status) {
        case 'PUBLISHED':
            return 'default';
        case 'SUSPENDED':
            return 'destructive';
        case 'COMPLETED':
            return 'secondary'
    }
}

function getStatusLabel(status: SerialState): string {
    switch (status) {
        case 'PUBLISHED':
            return '연재 중';
        case 'SUSPENDED':
            return '휴재';
        case 'COMPLETED':
            return '완결';
    }
}

export default function WorkManagementTable() {
    const router = useRouter();
    const itemsPerPage = 10;
    const [currentPage, setCurrentPage] = useState(0);

    const {data} = useQuery<BaseResponse<Pageable<WorkInfo>>>({
        queryKey: ['getWorks', currentPage, itemsPerPage],
        queryFn: async () => {
            const response = await workService.getWorks(currentPage, itemsPerPage);

            if (response.status !== 200) {
                console.error('Error:', response.status, ', cause: ', response.data ?? 'no data');
                throw new Error('작품 목록 조회에 실패하였습니다.');
            }

            return response;
        },
    });

    const works: WorkInfo[] = data?.data.content ?? [];
    const pageInfo = data?.data.page;
    const totalItems = pageInfo?.totalElements ?? 0;
    const totalPages = pageInfo?.totalPages ?? 0;


    return (
        <div className="flex-1 flex flex-col bg-gray-50">
            {/* Header */}
            <div className="border-b border-gray-200 bg-white px-8 py-6">
                <div className="flex items-center justify-between">
                    <p className="text-gray-600">
                        총 {totalItems}개의 작품이 등록되어 있습니다.
                    </p>
                    <Button
                        onClick={() => router.push('/works/register')}
                        className="bg-orange-500 hover:bg-orange-600 text-white"
                    >
                        <Plus className="w-4 h-4 mr-2"/>
                        신규 작품 등록
                    </Button>
                </div>
            </div>

            {/* Content */}
            <div className="flex-1 overflow-auto px-8 py-6">
                <div className="bg-white rounded-lg border border-gray-200">
                    <Table>
                        <TableHeader>
                            <TableRow className="border-b border-gray-200">
                                <TableHead
                                    className="w-24 px-6 py-4 text-xs font-semibold text-gray-600 uppercase tracking-wider">
                                    썸네일
                                </TableHead>
                                <TableHead
                                    className="px-6 py-4 text-xs font-semibold text-gray-600 uppercase tracking-wider">
                                    제목
                                </TableHead>
                                <TableHead
                                    className="px-6 py-4 text-xs font-semibold text-gray-600 uppercase tracking-wider">
                                    연재상태
                                </TableHead>
                                <TableHead
                                    className="w-20 px-6 py-4 text-xs font-semibold text-gray-600 uppercase tracking-wider">
                                    노출여부
                                </TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {works.map((work) => (
                                <TableRow
                                    key={work.id}
                                    className="border-b border-gray-100 hover:bg-gray-50 transition-colors cursor-pointer"
                                    onClick={() => router.push(`/works/${work.id}`)}
                                >
                                    <TableCell className="px-6 py-4">
                                        <div className="w-16 h-16 bg-gray-300 rounded overflow-hidden flex-shrink-0">
                                            <Image
                                                width={64}
                                                height={64}
                                                src={work.thumbnailUrl ?? '/images/default-nob-image.png'}
                                                alt={work.title}
                                                className="w-full h-full object-cover"
                                            />
                                        </div>
                                    </TableCell>
                                    <TableCell className="px-6 py-4">
                                        <div>
                                            <p className="font-semibold text-gray-900">
                                                {work.title}
                                            </p>
                                        </div>
                                    </TableCell>
                                    <TableCell className="px-6 py-4 text-gray-700">
                                        <Badge variant={getStatusBadgeColor(work.serialState)}>
                                            {getStatusLabel(work.serialState)}
                                        </Badge>
                                    </TableCell>
                                    <TableCell className="px-6 py-4">
                                        <p className='text-sm text-gray-500'>
                                            {work.visibility ? <Eye/> : <EyeClosed/>}
                                        </p>
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </div>

                {/* Pagination */}
                <div className="flex items-center justify-between mt-6">
                    <p className="text-sm text-gray-600">
                        Showing {currentPage + 1} to {works.length} of {totalItems} results
                    </p>
                    <div className="flex items-center gap-2">
                        <Button
                            variant="outline"
                            disabled={currentPage === 0}
                            onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
                        >
                            Previous
                        </Button>
                        {Array.from({length: Math.min(3, totalPages)}, (_, i) => (
                            <Button
                                key={i + 1}
                                variant={currentPage === i ? 'default' : 'outline'}
                                onClick={() => setCurrentPage(i)}
                                className={
                                    currentPage === i ? 'bg-blue-700 text-white' : ''
                                }
                            >
                                {i + 1}
                            </Button>
                        ))}
                        {totalPages > 3 && <span className="text-gray-600">...</span>}
                        <Button
                            variant="outline"
                            disabled={(currentPage + 1) === totalPages}
                            onClick={() =>
                                setCurrentPage(Math.min(totalPages, currentPage + 1))
                            }
                        >
                            Next
                        </Button>
                    </div>
                </div>
            </div>
        </div>
    );
}
