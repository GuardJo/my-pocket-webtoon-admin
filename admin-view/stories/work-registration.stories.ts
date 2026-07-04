import {Meta, StoryObj} from "@storybook/nextjs-vite";
import WorkRegistration from "@/components/work-registration";
import {http} from "msw";

const meta = {
    title: 'components/work-registration',
    component: WorkRegistration,
} satisfies Meta<typeof WorkRegistration>

export default meta;

type Story = StoryObj<typeof WorkRegistration>

const fileUploadUrl = process.env.NEXT_PUBLIC_UPLOAD_BASE_URL;
export const Default: Story = {
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
                http.post(`${fileUploadUrl}/api/v1/works`, () => {
                    return Response.json({
                        status: 200,
                        statusCode: 'Ok',
                        data: 'Successes'
                    })
                })
            ]
        }
    }
}