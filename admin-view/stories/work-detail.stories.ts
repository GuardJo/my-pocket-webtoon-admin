import {Meta, StoryObj} from "@storybook/nextjs-vite";
import WorkDetail from "@/components/work-detail";
import {http} from "msw";
import {EpisodeInfo, Pageable} from "@/lib/models";

const meta = {
    title: 'components/work-detail',
    component: WorkDetail,
} satisfies Meta<typeof WorkDetail>

export default meta;

type Story = StoryObj<typeof WorkDetail>

export const Default: Story = {
    args: {
        workId: 1
    },
    parameters: {
        msw: {
            handlers: [
                http.get('/api/works/:id', (id) => {
                    const mockWork = {
                        id: id,
                        title: '애늙은이',
                        thumbnailUrl: 'https://images.unsplash.com/photo-1549887534-f2cb8ff0bbb1?w=300&h=200&fit=crop',
                        description: '애늙은이 이야기',
                        serialState: 'COMPLETED',
                        visibility: true,
                        episodeTotalSize: 200,
                        lastUpdateDate: '2026-06-06'
                    }

                    return Response.json({
                        status: 200,
                        statusCode: 'Ok',
                        data: mockWork
                    });
                }),
                http.get('/api/works/:id/episodes', ({request}) => {
                    const url = new URL(request.url);
                    const page = Number(url.searchParams.get('page'));
                    const size = Number(url.searchParams.get('size'));

                    const pageContent: Pageable<EpisodeInfo> = {
                        content: [
                            {
                                id: 128,
                                workId: 1,
                                episodeNo: 1 + (page * size),
                                episodeThumbnailUrl: 'https://images.unsplash.com/photo-1549887534-f2cb8ff0bbb1?w=300&h=200&fit=crop',
                                episodeImageTotalCount: 72,
                                lastUpdateDate: '2023.10.24',
                            },
                            {
                                id: 127,
                                workId: 1,
                                episodeNo: 2 + (page * size),
                                episodeThumbnailUrl: 'https://images.unsplash.com/photo-1518895949257-7621c3c786d7?w=300&h=200&fit=crop',
                                episodeImageTotalCount: 68,
                                lastUpdateDate: '2023.10.17',
                            },
                            {
                                id: 126,
                                workId: 1,
                                episodeNo: 3 + (page * size),
                                episodeThumbnailUrl: 'https://images.unsplash.com/photo-1507842211343-583f20270319?w=300&h=200&fit=crop',
                                episodeImageTotalCount: 65,
                                lastUpdateDate: '2023.10.10',
                            },
                            {
                                id: 125,
                                workId: 1,
                                episodeNo: 4 + (page * size),
                                episodeThumbnailUrl: 'https://images.unsplash.com/photo-1518895949257-7621c3c786d7?w=300&h=200&fit=crop',
                                episodeImageTotalCount: 74,
                                lastUpdateDate: '2023.10.03',
                            },
                        ],
                        page: {
                            size: size ?? 0,
                            page: page ?? 0,
                            totalElements: 100,
                            totalPages: 10
                        }
                    }

                    return Response.json({
                        status: 200,
                        statusText: 'Ok',
                        data: pageContent
                    });
                })
            ]
        }
    }
}

export const NotFound: Story = {
    args: {
        workId: 1
    },
    parameters: {
        msw: {
            handlers: [
                http.get('/api/works/:id', (id) => {
                    return Response.json({
                        status: 404,
                        statusCode: 'NotFound',
                        data: '작품 조회에 실패하였습니다.'
                    });
                })
            ]
        }
    }
}