import {useState} from "react";
import {BaseResponse, MemberDetailInfo} from "@/lib/models";
import {X} from "lucide-react";
import {Input} from "@/components/ui/input";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
import {Button} from "@/components/ui/button";
import {useQuery} from "@tanstack/react-query";
import {memberService} from "@/lib/member-service";

export default function MemberDetailModal({open, memberId, onClose}: MemberDetailModalProps) {
    const [isEditing, setIsEditing] = useState(false);
    const [editDraft, setEditDraft] = useState<MemberDetailInfo | null>(null);

    const {data} = useQuery<BaseResponse<MemberDetailInfo>>({
        queryKey: ['getMemberDetail', memberId],
        queryFn: async () => {
            const response = await memberService.getMemberDetail(memberId);

            if (response.status !== 200) {
                console.error('Error: ', response.status, ', cause: ', response.data ?? 'no data');
                throw new Error('회원 정보를 불러오는데 실패하였습니다.');
            }

            return response;
        }
    });

    const selectedMember: MemberDetailInfo | null = data ? data.data : null;

    const closeMemberModal = () => {
        setIsEditing(false);
        setEditDraft(null);
        onClose();
    };

    const startEditing = () => {
        if (selectedMember) {
            setEditDraft({...selectedMember});
            setIsEditing(true);
        }
    };

    const saveEditing = () => {
        if (editDraft) {
            // TODO API 연동하기
            console.log('[v0] Saved member:', editDraft);
        }
        setIsEditing(false);
        setEditDraft(null);
    };

    return (
        <>
            {open && selectedMember !== null && (
                <div
                    className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4"
                    onClick={closeMemberModal}
                >
                    <div
                        className="bg-white rounded-2xl shadow-xl w-full max-w-2xl"
                        onClick={(e) => e.stopPropagation()}
                    >
                        {/* Modal Header */}
                        <div className="flex items-center justify-between px-8 py-6 border-b border-gray-100">
                            <h2 className="text-xl font-bold text-gray-900">회원 상세 정보</h2>
                            <button
                                onClick={closeMemberModal}
                                className="p-1 hover:bg-gray-100 rounded-lg"
                                aria-label="닫기"
                            >
                                <X className="w-6 h-6 text-gray-500"/>
                            </button>
                        </div>

                        {/* Modal Body */}
                        <div className="px-8 py-8">
                            <div className="grid grid-cols-2 gap-x-8 gap-y-8">
                                <div>
                                    <p className="text-sm text-gray-500 mb-2">회원 이름</p>
                                    {isEditing && editDraft ? (
                                        <Input
                                            value={editDraft.name}
                                            onChange={(e) =>
                                                setEditDraft({...editDraft, name: e.target.value})
                                            }
                                        />
                                    ) : (
                                        <p className="text-lg font-semibold text-gray-900">
                                            {selectedMember.name}
                                        </p>
                                    )}
                                </div>
                                <div>
                                    <p className="text-sm text-gray-500 mb-2">아이디</p>
                                    {isEditing && editDraft ? (
                                        <Input
                                            value={editDraft.id}
                                            onChange={(e) =>
                                                setEditDraft({...editDraft, id: e.target.value})
                                            }
                                        />
                                    ) : (
                                        <p className="text-lg font-semibold text-indigo-900">
                                            #{selectedMember.id}
                                        </p>
                                    )}
                                </div>
                                <div>
                                    <p className="text-sm text-gray-500 mb-2">닉네임</p>
                                    {isEditing && editDraft ? (
                                        <Input
                                            value={editDraft.nickname}
                                            onChange={(e) =>
                                                setEditDraft({...editDraft, nickname: e.target.value})
                                            }
                                        />
                                    ) : (
                                        <p className="text-lg font-semibold text-indigo-500">
                                            {selectedMember.nickname}
                                        </p>
                                    )}
                                </div>
                                <div>
                                    <p className="text-sm text-gray-500 mb-2">생년월일</p>
                                    <p className="text-lg font-semibold text-gray-900">
                                        {selectedMember.birthday ?? 'N/A'}
                                    </p>
                                </div>
                                <div>
                                    <p className="text-sm text-gray-500 mb-2">가입일자</p>
                                    <p className="text-lg font-semibold text-gray-900">
                                        {selectedMember.signupDate}
                                    </p>
                                </div>
                                <div>
                                    <p className="text-sm text-gray-500 mb-2">수정일자</p>
                                    <p className="text-lg font-semibold text-gray-900">
                                        {selectedMember.lastUpdateDate}
                                    </p>
                                </div>
                                <div>
                                    <p className="text-sm text-gray-500 mb-2">상태 정보</p>
                                    {isEditing && editDraft ? (
                                        <Select
                                            value={String(editDraft.activate)}
                                            onValueChange={(value) =>
                                                setEditDraft({
                                                    ...editDraft,
                                                    activate: value === "true",
                                                })
                                            }
                                        >
                                            <SelectTrigger className="w-40">
                                                <SelectValue/>
                                            </SelectTrigger>
                                            <SelectContent>
                                                <SelectItem value="true">ACTIVE</SelectItem>
                                                <SelectItem value="false">PENDING</SelectItem>
                                            </SelectContent>
                                        </Select>
                                    ) : selectedMember.activate ? (
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
                                </div>
                                <div>
                                    <p className="text-sm text-gray-500 mb-2">승인 관리자 아이디</p>
                                    <p className="text-lg font-semibold text-gray-900">
                                        #{selectedMember.registerAdminId ?? 'N/A'}
                                    </p>
                                </div>
                            </div>
                        </div>

                        {/* Modal Footer */}
                        <div className="flex items-center justify-end gap-3 px-8 py-6 border-t border-gray-100">
                            <Button
                                variant="outline"
                                className="border-gray-300 text-gray-700"
                            >
                                비밀번호 초기화
                            </Button>
                            {isEditing ? (
                                <Button
                                    onClick={saveEditing}
                                    className="bg-indigo-900 hover:bg-indigo-800 text-white"
                                >
                                    저장
                                </Button>
                            ) : (
                                <Button
                                    onClick={startEditing}
                                    className="bg-indigo-900 hover:bg-indigo-800 text-white"
                                >
                                    수정
                                </Button>
                            )}
                        </div>
                    </div>
                </div>
            )}
        </>
    )
}

interface MemberDetailModalProps {
    open: boolean;
    memberId: string;
    onClose: () => void;
}
