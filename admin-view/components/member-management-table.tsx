import {BaseResponse, MemberInfo, Pageable} from "@/lib/models";
import {useState} from "react";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table";
import Pagination from "@/components/pagination";
import {useQuery} from "@tanstack/react-query";
import {memberService} from "@/lib/member-service";
import MemberDetailModal from "@/components/member-detail-modal";

const filterTabs = [
    {key: 'all', label: 'All Users'},
    {key: 'pending', label: 'Pending'},
    {key: 'active', label: 'Active'},
] as const;

type FilterKey = (typeof filterTabs)[number]['key'];

export default function MemberManagementTable() {
    const itemsPerPage = 10;
    const [activeFilter, setActiveFilter] = useState<FilterKey>('all');
    const [currentPage, setCurrentPage] = useState(0);
    const [selectedMemberId, setSelectedMemberId] = useState<string | null>(null);
    const [openMemberModal, setOpenMemberModal] = useState(false);

    const {data} = useQuery<BaseResponse<Pageable<MemberInfo>>>({
        queryKey: ['getMembers', currentPage, itemsPerPage],
        queryFn: async () => {
            const response = await memberService.getMembers(currentPage, itemsPerPage);

            if (response.status !== 200) {
                console.error('Error: ', response.status, ', cause: ', response.data ?? 'no data');
                throw new Error('회원 목록 조회에 실패하였습니다.');
            }

            return response;
        }
    })

    const members: MemberInfo[] = data?.data.content ?? [];
    const pageInfo = data?.data.page;
    const totalItems = pageInfo?.totalElements ?? 0;
    const totalPages = pageInfo?.totalPages ?? 0;

    const onClickMember = (memberId: string) => {
        setSelectedMemberId(memberId);
        setOpenMemberModal(true);
    }

    return (
        <>
            {/* Filters Row */}
            <div className="flex items-center justify-between mb-6">
                <div className="flex items-center gap-2">
                    {filterTabs.map((tab) => (
                        <button
                            key={tab.key}
                            onClick={() => {
                                setActiveFilter(tab.key);
                                setCurrentPage(0);
                            }}
                            className={`px-5 py-2.5 rounded-full text-sm font-semibold transition-colors ${
                                activeFilter === tab.key
                                    ? 'bg-indigo-900 text-white'
                                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                            }`}
                        >
                            {tab.label}
                        </button>
                    ))}
                </div>
            </div>

            <div className="bg-white rounded-xl border border-gray-200">
                <Table>
                    <TableHeader>
                        <TableRow className="border-b border-gray-200 hover:bg-transparent">
                            <TableHead
                                className="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">
                                User ID
                            </TableHead>
                            <TableHead
                                className="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">
                                Name
                            </TableHead>
                            <TableHead
                                className="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">
                                Nickname
                            </TableHead>
                            <TableHead
                                className="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">
                                Signup Date
                            </TableHead>
                            <TableHead
                                className="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">
                                Status
                            </TableHead>
                            <TableHead
                                className="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider text-right">
                                Actions
                            </TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {members.map((member) => (
                            <TableRow
                                key={member.id}
                                className="border-b border-gray-100 hover:bg-gray-50 transition-colors cursor-pointer"
                                onClick={() => onClickMember(member.id)}
                            >
                                <TableCell className="px-6 py-5 text-sm text-gray-500">
                                    #{member.id}
                                </TableCell>
                                <TableCell className="px-6 py-5">
                                    <span className="font-semibold text-gray-900">
                                      {member.name}
                                    </span>
                                </TableCell>
                                <TableCell className="px-6 py-5 text-sm text-gray-600">
                                    {member.nickname}
                                </TableCell>
                                <TableCell className="px-6 py-5 text-sm text-gray-600">
                                    {member.signupDate}
                                </TableCell>
                                <TableCell className="px-6 py-5">
                                    {member.activate ? (
                                        <span
                                            className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-indigo-100 text-indigo-700">
                                            <span className="w-1.5 h-1.5 rounded-full bg-indigo-500"/>
                                            ACTIVE
                                        </span>
                                    ) : (
                                        <span
                                            className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-orange-100 text-orange-700">
                                            <span className="w-1.5 h-1.5 rounded-full bg-orange-500"/>
                                            PENDING
                                        </span>
                                    )}
                                </TableCell>
                                <TableCell className="px-6 py-5">
                                    <div className="flex items-center justify-end gap-3">
                                        {member.activate ? (
                                            <button
                                                onClick={(e) => e.stopPropagation()}
                                                className="text-sm font-semibold text-red-600 hover:text-red-800"
                                            >
                                                탈퇴 처리
                                            </button>
                                        ) : (
                                            <>
                                                <button
                                                    onClick={(e) => e.stopPropagation()}
                                                    className="px-4 py-1.5 rounded-lg text-sm font-semibold bg-indigo-100 text-indigo-700 hover:bg-indigo-200"
                                                >
                                                    수락
                                                </button>
                                                <button
                                                    onClick={(e) => e.stopPropagation()}
                                                    className="text-sm font-semibold text-red-600 hover:text-red-800"
                                                >
                                                    거절
                                                </button>
                                            </>
                                        )}
                                    </div>
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>

                {/* Pagination */}
                <Pagination totalPage={totalPages} currentPage={currentPage} totalElement={totalItems}
                            pageSize={members.length} onPageChange={setCurrentPage}/>

                {/* Member Detail Modal */}
                {selectedMemberId && (
                    <MemberDetailModal
                        open={openMemberModal}
                        memberId={selectedMemberId}
                        onClose={() => {
                            setOpenMemberModal(false);
                            setSelectedMemberId(null);
                        }}
                    />
                )}
            </div>
        </>
    )
}
