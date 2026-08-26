import {useState} from "react";
import {Edit, Eye, EyeClosed, Trash2, Upload, X} from "lucide-react";
import {Badge} from "@/components/ui/badge";
import {Button} from "@/components/ui/button";
import {Input} from "@/components/ui/input";
import {Textarea} from "@/components/ui/textarea";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
import {SERIAL_STATE_LABEL, SERIAL_STATES, SerialState, WorkDetailInfo} from "@/lib/models";
import Image from "next/image";
import {useMutation, useQueryClient} from "@tanstack/react-query";
import {fileUploadService} from "@/lib/file-upload-service";
import {workService, WorkUpdateRequest} from "@/lib/work-service";
import {useRouter} from "next/navigation";

export default function WorkInfo({workDetailInfo}: WorkInfoProps) {
    const queryClient = useQueryClient();
    const router = useRouter();

    const [isDetailsModalOpen, setIsDetailsModalOpen] = useState(false);
    const [isThumbnailModalOpen, setIsThumbnailModalOpen] = useState(false);
    const [thumbnailPreview, setThumbnailPreview] = useState<string | null>(null);
    const [thumbnailFile, setThumbnailFile] = useState<File | null>(null);
    const [detailsFormData, setDetailsFormData] = useState({
        id: workDetailInfo.id,
        thumbnailUrl: workDetailInfo.thumbnailUrl,
        title: workDetailInfo.title,
        description: workDetailInfo.description,
        status: workDetailInfo.serialState,
        visibility: workDetailInfo.visibility,
        episodeTotalSize: workDetailInfo.episodeTotalSize,
        lastUpdateDate: workDetailInfo.lastUpdateDate
    });

    const thumbnailFileUploadMutation = useMutation({
        mutationKey: ['thumbnailFileUpload', workDetailInfo.id, thumbnailFile],
        mutationFn: (variables: {
            workId: number,
            file: File
        }) => fileUploadService.uploadThumbnail(variables.workId, variables.file),
        onSuccess: async (response) => {
            if (response.status !== 200) {
                console.error('Failed update thumbnailFile: {}', response);
                alert(response.data);
            } else {
                await queryClient.invalidateQueries({queryKey: ['getWorks']});
                await queryClient.invalidateQueries({queryKey: ['getWork', workDetailInfo.id]});
                console.info('Updated thumbnailFile, workId = {}', workDetailInfo.id);
                alert('작품 썸네일 파일이 변경되었습니다.');
            }
        },
        onError: error => {
            console.error('Update error : {}', error);
            alert('작품 썸네일 파일 저장에 실패하였습니다.');
        }
    });

    const workUpdateMutation = useMutation({
        mutationKey: ['upldateWork', workDetailInfo.id],
        mutationFn: (variables: {
            workId: number,
            updateReq: WorkUpdateRequest
        }) => workService.updateWork(variables.workId, variables.updateReq),
        onSuccess: async (response) => {
            if (response.status !== 200) {
                console.error('Failed update work: {}', response);
                alert(response.data);
            } else {
                await queryClient.invalidateQueries({queryKey: ['getWorks']});
                await queryClient.invalidateQueries({queryKey: ['getWork', workDetailInfo.id]});
                alert('작품 정보가 변경되었습니다.');
            }
        },
        onError: error => {
            console.error('Update work error : {}', error);
            alert('작품 정보 저장에 실패하였습니다.');
        }
    });

    const workDeleteMutation = useMutation({
        mutationKey: ['deleteWork', workDetailInfo.id],
        mutationFn: (variables: { workId: number }) => workService.deleteWork(variables.workId),
        onSuccess: async (response) => {
            if (response.status !== 200) {
                console.error('Failed delete work: {}', response);
                alert(response.data);
            } else {
                await queryClient.invalidateQueries({queryKey: ['getWorks']});

                alert('작품이 삭제되었습니다.');
                router.replace('/works');
            }
        },
        onError: error => {
            console.error('Delete work error : {}', error);
            alert('작품 삭제에 실패하였습니다.');
        }
    });

    const handleThumbnailChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = (event) => {
                setThumbnailPreview(event.target?.result as string);
            };
            reader.readAsDataURL(file);
            setThumbnailFile(file);
        }
    };

    const handleDetailsSave = () => {
        console.log('Saving details:', detailsFormData);

        workUpdateMutation.mutate({
            workId: workDetailInfo.id,
            updateReq: {
                title: detailsFormData.title,
                description: detailsFormData.description,
                serialState: detailsFormData.status,
                visibility: detailsFormData.visibility
            }
        });

        setIsDetailsModalOpen(false);
    };

    const handleThumbnailSave = () => {
        console.log('Saving thumbnail:', thumbnailFile?.name);

        thumbnailFileUploadMutation.mutate({
            workId: workDetailInfo.id,
            file: thumbnailFile as File
        });

        setIsThumbnailModalOpen(false);
    };

    const handleWorkDelete = () => {
        if (confirm('작품을 삭제하시겠습니까?')) {
            workDeleteMutation.mutate({workId: workDetailInfo.id});
        }
    }

    return (
        <div className="grid grid-cols-3 gap-8 mb-12 bg-inherit">
            {/* Thumbnail */}
            <div className="col-span-1">
                <div className="relative group">
                    <div
                        className="relative w-full aspect-[3/4] bg-gray-300 rounded-lg overflow-hidden flex-shrink-0">
                        <Image src={thumbnailPreview ?? detailsFormData.thumbnailUrl ?? '/images/default-nob-image.png'}
                               alt={detailsFormData.title} fill={true}
                               className="w-full h-full object-cover"/>
                    </div>
                    <button
                        onClick={() => setIsThumbnailModalOpen(true)}
                        className="absolute bottom-3 right-3 bg-white rounded-lg p-2 shadow-lg hover:shadow-xl transition-shadow opacity-0 group-hover:opacity-100"
                    >
                        <Edit className="w-5 h-5 text-gray-700"/>
                    </button>
                </div>
            </div>

            {/* Work Information */}
            <div className="col-span-2">
                <div className="mb-6">
                    <Badge className="mb-4 bg-indigo-100 text-indigo-700 hover:bg-indigo-100">
                        {SERIAL_STATE_LABEL[detailsFormData.status]}
                    </Badge>
                    <h2 className="text-4xl font-bold text-gray-900 mb-4">
                        {detailsFormData.title}
                    </h2>
                    <p className="text-gray-600 leading-relaxed mb-8">
                        {detailsFormData.description}
                    </p>

                    {/* Stats */}
                    <div className="flex gap-12 mb-8">
                        <div>
                            <p className="text-sm text-gray-600 font-semibold mb-2">
                                총회차
                            </p>
                            <p className="text-3xl font-bold text-indigo-600">{detailsFormData.episodeTotalSize}</p>
                        </div>
                        <div>
                            <p className="text-sm text-gray-600 font-semibold mb-2">
                                수정일자
                            </p>
                            <p className="text-3xl font-bold text-gray-900">
                                {detailsFormData.lastUpdateDate ?? 'N/A'}
                            </p>
                        </div>
                        <div>
                            <p className="text-sm text-gray-600 font-semibold mb-2">
                                공개여부
                            </p>
                            <p className="text-3xl font-bold text-gray-900">
                                {workDetailInfo.visibility ? <Eye/> : <EyeClosed/>}
                            </p>
                        </div>
                    </div>

                    {/* Action Buttons */}
                    <div className="flex gap-4">
                        <Button
                            onClick={() => setIsDetailsModalOpen(true)}
                            className="bg-indigo-900 hover:bg-indigo-800 text-white gap-2"
                        >
                            <Edit className="w-4 h-4"/>
                            Edit Details
                        </Button>
                        <Button
                            onClick={handleWorkDelete}
                            variant="outline"
                            className="text-red-600 border-red-600 hover:bg-red-50"
                        >
                            <Trash2 className="w-4 h-4 mr-2"/>
                            Delete Series
                        </Button>
                    </div>
                </div>
            </div>
            {/* Details Edit Modal */}
            {isDetailsModalOpen && (
                <div className="fixed inset-0 bg-black/70 flex items-center justify-center z-50">
                    <div className="bg-white rounded-lg shadow-xl max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto">
                        {/* Modal Header */}
                        <div
                            className="flex items-center justify-between p-6 border-b border-gray-200 sticky top-0 bg-white">
                            <h2 className="text-xl font-bold text-gray-900">작품 정보 수정</h2>
                            <button
                                onClick={() => setIsDetailsModalOpen(false)}
                                className="p-1 hover:bg-gray-100 rounded"
                            >
                                <X className="w-5 h-5"/>
                            </button>
                        </div>

                        {/* Modal Content */}
                        <div className="p-6 space-y-6">
                            <div>
                                <label className="block text-sm font-semibold text-gray-900 mb-2">
                                    작품명
                                </label>
                                <Input
                                    value={detailsFormData.title}
                                    onChange={(e) =>
                                        setDetailsFormData({...detailsFormData, title: e.target.value})
                                    }
                                    className="w-full"
                                />
                            </div>

                            <div>
                                <label className="block text-sm font-semibold text-gray-900 mb-2">
                                    작품 내용
                                </label>
                                <Textarea
                                    value={detailsFormData.description}
                                    onChange={(e) =>
                                        setDetailsFormData({...detailsFormData, description: e.target.value})
                                    }
                                    className="w-full min-h-32"
                                />
                            </div>

                            <div>
                                <label className="block text-sm font-semibold text-gray-900 mb-2">
                                    연재상태
                                </label>
                                <Select
                                    value={detailsFormData.status}
                                    onValueChange={(value) =>
                                        setDetailsFormData({...detailsFormData, status: value as SerialState})
                                    }
                                >
                                    <SelectTrigger className="w-full">
                                        <SelectValue/>
                                    </SelectTrigger>
                                    <SelectContent>
                                        {SERIAL_STATES.map((serialState) => (
                                            <SelectItem key={serialState} value={serialState}>
                                                {SERIAL_STATE_LABEL[serialState]}
                                            </SelectItem>
                                        ))}
                                    </SelectContent>
                                </Select>
                            </div>

                            <div className="flex items-center gap-3">
                                <input
                                    type="checkbox"
                                    id="exposed"
                                    checked={detailsFormData.visibility}
                                    onChange={(e) =>
                                        setDetailsFormData({...detailsFormData, visibility: e.target.checked})
                                    }
                                    className="w-4 h-4"
                                />
                                <label htmlFor="exposed" className="text-sm font-medium text-gray-900">
                                    노출 여부
                                </label>
                            </div>
                        </div>

                        {/* Modal Footer */}
                        <div className="flex gap-3 p-6 border-t border-gray-200 bg-gray-50">
                            <Button
                                onClick={() => setIsDetailsModalOpen(false)}
                                variant="outline"
                                className="flex-1"
                            >
                                취소
                            </Button>
                            <Button
                                onClick={handleDetailsSave}
                                className="flex-1 bg-indigo-900 hover:bg-indigo-800 text-white"
                            >
                                저장
                            </Button>
                        </div>
                    </div>
                </div>
            )}

            {/* Thumbnail Edit Modal */}
            {isThumbnailModalOpen && (
                <div className="fixed inset-0 bg-black/70 flex items-center justify-center z-50 p-4">
                    <div className="bg-white rounded-lg shadow-xl max-w-md w-full max-h-[90vh] flex flex-col">
                        {/* Modal Header */}
                        <div className="flex items-center justify-between p-6 border-b border-gray-200 flex-shrink-0">
                            <h2 className="text-xl font-bold text-gray-900">썸네일 수정</h2>
                            <button
                                onClick={() => {
                                    setIsThumbnailModalOpen(false);
                                    setThumbnailPreview(null);
                                }}
                                className="p-1 hover:bg-gray-100 rounded"
                            >
                                <X className="w-5 h-5"/>
                            </button>
                        </div>

                        {/* Modal Content */}
                        <div className="p-6 space-y-6 overflow-y-auto flex-1">
                            <div>
                                <label className="block text-sm font-semibold text-gray-900 mb-4">
                                    새 썸네일 이미지
                                </label>
                                <label className="block">
                                    <div
                                        className="border-2 border-dashed border-gray-300 rounded-lg p-8 text-center cursor-pointer hover:border-indigo-400 hover:bg-indigo-50 transition-colors">
                                        {thumbnailPreview ? (
                                            <div className="space-y-3">
                                                <Image src={thumbnailPreview} alt="Preview" width={300} height={400}
                                                       className="w-full aspect-3/4 object-cover rounded"/>
                                                <p className="text-sm text-gray-600">클릭하여 다시 선택</p>
                                            </div>
                                        ) : (
                                            <div className="space-y-2">
                                                <Upload className="w-8 h-8 text-gray-400 mx-auto"/>
                                                <p className="text-sm font-medium text-gray-700">
                                                    파일을 드래그하거나 클릭
                                                </p>
                                                <p className="text-xs text-gray-500">
                                                    형식: JPG, PNG (3:4 비율)
                                                </p>
                                            </div>
                                        )}
                                    </div>
                                    <input
                                        type="file"
                                        accept="image/*"
                                        onChange={handleThumbnailChange}
                                        className="hidden"
                                    />
                                </label>
                            </div>

                            <div className="text-sm text-gray-600 bg-gray-50 p-3 rounded">
                                <p className="font-medium mb-2">권장 사양:</p>
                                <ul className="space-y-1">
                                    <li>• 비율: 3:4</li>
                                    <li>• 형식: JPG, PNG</li>
                                </ul>
                            </div>
                        </div>

                        {/* Modal Footer */}
                        <div className="flex gap-3 p-6 border-t border-gray-200 bg-gray-50 flex-shrink-0">
                            <Button
                                onClick={() => {
                                    setIsThumbnailModalOpen(false);
                                    setThumbnailPreview(null);
                                }}
                                variant="outline"
                                className="flex-1"
                            >
                                취소
                            </Button>
                            <Button
                                onClick={handleThumbnailSave}
                                className="flex-1 bg-indigo-900 hover:bg-indigo-800 text-white"
                                disabled={!thumbnailPreview}
                            >
                                저장
                            </Button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    )
}

interface WorkInfoProps {
    workDetailInfo: WorkDetailInfo
}