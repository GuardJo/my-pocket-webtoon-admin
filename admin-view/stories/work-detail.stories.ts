import {Meta, StoryObj} from "@storybook/nextjs-vite";
import WorkDetail from "@/components/work-detail";
import {http} from "msw";

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