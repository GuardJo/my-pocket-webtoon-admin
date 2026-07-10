import {Meta, StoryObj} from "@storybook/nextjs-vite";
import WorkInfo from "@/components/work-info";
import {http} from "msw";

const meta = {
    title: 'components/work-info',
    component: WorkInfo,
} satisfies Meta<typeof WorkInfo>

export default meta;

type Story = StoryObj<typeof WorkInfo>;

const fileUploadUrl = process.env.NEXT_PUBLIC_UPLOAD_BASE_URL;

export const Default: Story = {
    args: {
        workDetailInfo: {
            id: 99,
            title: '애늙은이',
            thumbnailUrl: null,
            description: '애늙은이 이야기',
            serialState: 'COMPLETED',
            visibility: true,
            episodeTotalSize: 200,
            lastUpdateDate: '2026-06-06'
        }
    },
    parameters: {
        msw: {
            handlers: [
                http.post('/api/auth/upload-token', () => {
                    return Response.json({
                        status: 200,
                        statusCode: 'Ok',
                        data: 'upload-token'
                    })
                }),
                http.patch(`${fileUploadUrl}/api/v1/works/:workId/thumbnail`, () => {
                    return Response.json({
                        status: 200,
                        statusText: 'Ok',
                        data: 'Successes'
                    });
                })
            ]
        }
    }
}