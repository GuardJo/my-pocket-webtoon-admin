'use client';

import {useState} from 'react';
import {Button} from '@/components/ui/button';
import {Input} from '@/components/ui/input';
import {Textarea} from '@/components/ui/textarea';
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue,} from '@/components/ui/select';
import {ArrowLeft, CheckCircle2, FileArchive, Upload, X} from 'lucide-react';
import {useRouter} from 'next/navigation';
import {SERIAL_STATE_LABEL, SerialState} from "@/lib/models";

const serialStates: SerialState[] = [
    'COMPLETED',
    'PUBLISHED',
    'SUSPENDED'
];

const visibilities = [
    {label: '공개', value: true},
    {label: '비공개', value: false}
]

export default function WorkRegistration() {
    const router = useRouter();
    const [formData, setFormData] = useState({
        title: '',
        description: '',
        visibility: true,
        serialState: '',
        thumbnailFile: null as File | null,
        episodeFile: null as File | null,
    });
    const [thumbnailPreview, setThumbnailPreview] = useState<string | null>();

    const handleInputChange = (
        e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
    ) => {
        const {name, value} = e.target;
        setFormData((prev) => ({
            ...prev,
            [name]: value,
        }));
    };

    const handleSelectChange = (name: string, value: string) => {
        if (name === 'visibility') {
            setFormData((prev) => ({
                ...prev,
                visibility: value === 'true',
            }));
        } else if (name === 'serialState') {
            setFormData((prev) => ({
                ...prev,
                [name]: value,
            }));
        }
    };

    const handleFileChange = (
        e: React.ChangeEvent<HTMLInputElement>,
        fileType: 'thumbnailFile' | 'episodeFile'
    ) => {
        const file = e.target.files?.[0];
        if (file) {
            setFormData((prev) => ({
                ...prev,
                [fileType]: file,
            }));

            if (fileType === 'thumbnailFile') {
                const reader = new FileReader();
                reader.onloadend = () => {
                    setThumbnailPreview(reader.result as string);
                };
                reader.readAsDataURL(file);
            }
        }
    };

    const handleRemoveThumbnail = (e: React.MouseEvent) => {
        e.preventDefault();
        e.stopPropagation();
        setFormData((prev) => ({...prev, thumbnail: null}));
        setThumbnailPreview(null);
    };

    const handleRemoveEpisodeData = (e: React.MouseEvent) => {
        e.preventDefault();
        e.stopPropagation();
        setFormData((prev) => ({...prev, episodeFile: null}));
    };

    const handleSubmit = (action: 'register' | 'draft' | 'cancel') => {
        if (action === 'cancel') {
            router.back();
            return;
        }

        // TODO formData 값 검증 추가하기
        // TODO API 연동하기
        console.log('Form submitted:', {formData, action});
    };

    return (
        <div className="flex-1 flex flex-col bg-gray-50">
            {/* Header */}
            <div className="border-b border-gray-200 bg-white px-8 py-6">
                <div className="flex items-center gap-4 mb-4">
                    <button
                        onClick={() => router.back()}
                        className="p-2 hover:bg-gray-100 rounded-lg"
                    >
                        <ArrowLeft className="w-5 h-5 text-gray-600"/>
                    </button>
                    <div>
                        <p className="text-sm text-gray-500">CURATION PORTAL</p>
                        <h1 className="text-3xl font-bold text-gray-900">새 작품 등록</h1>
                    </div>
                </div>
                <p className="text-gray-600 ml-12">
                    플랫폼에 게시할 새로운 웹툰 시리즈를 등록합니다. 고해상도 썸네일과 원본 이미지 업로드를 준비하세요.
                </p>
            </div>

            {/* Content */}
            <div className="flex-1 overflow-auto px-8 py-6">
                <div className="max-w-7xl">
                    <div className="grid grid-cols-3 gap-8">
                        {/* Left Column - Basic Information */}
                        <div className="col-span-2 space-y-8">
                            {/* Title Input */}
                            <div>
                                <label className="block text-sm font-semibold text-gray-900 mb-3">
                                    작품 제목
                                </label>
                                <Input
                                    type="text"
                                    name="title"
                                    required={true}
                                    value={formData.title}
                                    onChange={handleInputChange}
                                    placeholder="작품의 정식 타이틀을 입력하세요"
                                    className="w-full px-4 py-3 bg-gray-100 border-0 rounded-lg"
                                />
                            </div>

                            {/* Description Input */}
                            <div>
                                <label className="block text-sm font-semibold text-gray-900 mb-3">
                                    작품 설명
                                </label>
                                <Textarea
                                    name="description"
                                    value={formData.description}
                                    onChange={handleInputChange}
                                    placeholder="작품의 시놉시스와 주요 특징을 입력해 주세요"
                                    className="w-full min-h-32 px-4 py-3 bg-gray-100 border-0 rounded-lg resize-none"
                                />
                            </div>

                            {/* Genre and Publication Day */}
                            <div className="grid grid-cols-2 gap-6">
                                <div>
                                    <label className="block text-sm font-semibold text-gray-900 mb-3">
                                        연재상태 선택
                                    </label>
                                    <Select
                                        value={formData.serialState}
                                        onValueChange={(value) =>
                                            handleSelectChange('serialState', value)
                                        }
                                    >
                                        <SelectTrigger className="bg-gray-100 border-0 py-6 px-4">
                                            <SelectValue placeholder="연재상태를 선택하세요"/>
                                        </SelectTrigger>
                                        <SelectContent>
                                            {serialStates.map((serialState) => (
                                                <SelectItem key={serialState} value={serialState}>
                                                    {SERIAL_STATE_LABEL[serialState]}
                                                </SelectItem>
                                            ))}
                                        </SelectContent>
                                    </Select>
                                </div>

                                <div>
                                    <label className="block text-sm font-semibold text-gray-900 mb-3">
                                        공개 여부
                                    </label>
                                    <Select
                                        value={String(formData.visibility)}
                                        onValueChange={(value) =>
                                            handleSelectChange('visibility', value)
                                        }
                                    >
                                        <SelectTrigger className="bg-gray-100 border-0 py-6 px-4">
                                            <SelectValue placeholder="공개여부를 선택하세요"/>
                                        </SelectTrigger>
                                        <SelectContent>
                                            {visibilities.map((visibility) => (
                                                <SelectItem key={String(visibility.value)}
                                                            value={String(visibility.value)}>
                                                    {visibility.label}
                                                </SelectItem>
                                            ))}
                                        </SelectContent>
                                    </Select>
                                </div>
                            </div>

                            {/* Episode Data Upload */}
                            <div>
                                <h3 className="text-sm font-semibold text-gray-900 mb-4 flex items-center gap-2">
                                    <span>📁</span>
                                    에피소드 데이터
                                </h3>
                                {formData.episodeFile ? (
                                    <div className="w-full p-6 border-2 border-green-200 rounded-lg bg-green-50">
                                        <div className="flex items-center gap-4">
                                            <div
                                                className="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center flex-shrink-0">
                                                <FileArchive className="w-6 h-6 text-green-600"/>
                                            </div>
                                            <div className="flex-1 min-w-0">
                                                <div className="flex items-center gap-2 mb-1">
                                                    <CheckCircle2 className="w-4 h-4 text-green-600 flex-shrink-0"/>
                                                    <p className="text-sm font-semibold text-green-900">
                                                        업로드 완료
                                                    </p>
                                                </div>
                                                <p className="text-sm text-gray-700 truncate">
                                                    {formData.episodeFile.name}
                                                </p>
                                                <p className="text-xs text-gray-500 mt-0.5">
                                                    {(formData.episodeFile.size / 1024 / 1024).toFixed(2)} MB
                                                </p>
                                            </div>
                                            <button
                                                onClick={handleRemoveEpisodeData}
                                                className="p-2 hover:bg-green-100 rounded-lg flex-shrink-0"
                                                aria-label="파일 삭제"
                                            >
                                                <X className="w-5 h-5 text-gray-500"/>
                                            </button>
                                        </div>
                                    </div>

                                ) : (
                                    <label
                                        className="block w-full p-8 border-2 border-dashed border-gray-200 rounded-lg text-center cursor-pointer hover:border-gray-300 transition-colors bg-gray-50">
                                        <input
                                            type="file"
                                            accept=".tar"
                                            onChange={(e) => handleFileChange(e, 'episodeFile')}
                                            className="hidden"
                                        />
                                        <Upload className="w-8 h-8 text-gray-400 mx-auto mb-3"/>
                                        <p className="text-gray-600 font-medium mb-1">
                                            파일을 드래그하거나 클릭하여 업로드
                                        </p>
                                        <p className="text-xs text-gray-500">
                                            500MB, 20개 파일 이상
                                        </p>
                                    </label>
                                )}
                            </div>
                        </div>

                        {/* Right Column - Thumbnail */}
                        <div className="col-span-1">
                            <h3 className="text-sm font-semibold text-gray-900 mb-6 flex items-center gap-2">
                                <span>📁</span>
                                작품 썸네일
                            </h3>
                            <div className="space-y-6">
                                {/* Thumbnail Upload Section */}
                                <div className="flex justify-center items-center">
                                    <label
                                        className="relative w-full aspect-[3/4] border-2 border-dashed border-gray-200 rounded-lg flex flex-col items-center justify-center cursor-pointer hover:border-gray-300 transition-colors bg-gray-100 overflow-hidden group">
                                        <input
                                            type="file"
                                            accept="image/*"
                                            onChange={(e) => handleFileChange(e, 'thumbnailFile')}
                                            className="hidden"
                                        />
                                        {thumbnailPreview ? (
                                            <>
                                                <img
                                                    src={thumbnailPreview || "/images/default-nob-image.png"}
                                                    alt="썸네일 미리보기"
                                                    className="absolute inset-0 w-full h-full object-cover"
                                                />
                                                <div
                                                    className="absolute inset-0 bg-black/0 group-hover:bg-black/40 transition-colors flex items-center justify-center">
                                                    <p className="text-white font-medium opacity-0 group-hover:opacity-100 transition-opacity">
                                                        이미지 변경
                                                    </p>
                                                </div>
                                                <button
                                                    onClick={handleRemoveThumbnail}
                                                    className="absolute top-2 right-2 p-1.5 bg-white/90 hover:bg-white rounded-lg shadow-sm z-10"
                                                    aria-label="썸네일 삭제"
                                                >
                                                    <X className="w-4 h-4 text-gray-700"/>
                                                </button>
                                            </>

                                        ) : (<div className="text-center">
                                            <div
                                                className="w-16 h-16 bg-blue-50 rounded-lg flex items-center justify-center mx-auto mb-3">
                                                <span className="text-2xl">📷</span>
                                            </div>
                                            <p className="text-gray-700 font-medium mb-1">
                                                대표 이미지 업로드
                                            </p>
                                            <p className="text-xs text-gray-500">
                                                1000 × 1333px (3:4 비율)
                                            </p>
                                        </div>)}
                                    </label>
                                </div>

                                {/* Specifications */}
                                <div className="space-y-4">
                                    <div className="bg-white rounded-lg p-4 border border-gray-200">
                                        <div className="flex items-center gap-2 mb-2">
                      <span className="text-sm font-semibold text-gray-900">
                        RGB 색상표
                      </span>
                                            <span className="text-xs text-gray-500 ml-auto">
                        REQUIRED
                      </span>
                                        </div>
                                        <p className="text-xs text-gray-600">
                                            RGB 색상 모드로 저장된 이미지를 업로드하세요.
                                        </p>
                                    </div>

                                    <div className="bg-white rounded-lg p-4 border border-gray-200">
                                        <div className="flex items-center gap-2 mb-2">
                      <span className="text-sm font-semibold text-gray-900">
                        해상도
                      </span>
                                            <span className="text-xs text-gray-500 ml-auto">
                        REQUIRED
                      </span>
                                        </div>
                                        <p className="text-xs text-gray-600">
                                            최소 800px 이상의 해상도를 유지하세요.
                                        </p>
                                    </div>

                                    {formData.thumbnailFile && (
                                        <div className="bg-green-50 rounded-lg p-4 border border-green-200">
                                            <div className="flex items-center gap-2 mb-1">
                                                <CheckCircle2 className="w-4 h-4 text-green-600 flex-shrink-0"/>
                                                <p className="text-sm text-green-900 font-medium truncate">
                                                    {formData.thumbnailFile.name}
                                                </p>
                                            </div>
                                            <p className="text-xs text-green-700 ml-6">
                                                크기: {(formData.thumbnailFile.size / 1024 / 1024).toFixed(2)} MB
                                            </p>
                                        </div>
                                    )}
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* Action Buttons */}
                    <div className="mt-12 flex flex-col gap-3">
                        <Button
                            onClick={() => handleSubmit('register')}
                            className="w-full bg-blue-900 hover:bg-blue-950 text-white py-6 text-base font-semibold rounded-lg"
                        >
                            작품 등록하기
                        </Button>
                        <button
                            onClick={() => handleSubmit('cancel')}
                            className="w-full text-red-500 hover:text-red-700 font-semibold py-3 transition-colors"
                        >
                            취소
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}
