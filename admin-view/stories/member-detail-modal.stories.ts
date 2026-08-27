import {Meta, StoryObj} from "@storybook/nextjs-vite";
import MemberDetailModal from "@/components/member-detail-modal";
import {action} from "storybook/actions";
import {http} from "msw";
import {MemberDetailInfo} from "@/lib/models";

const meta = {
    title: 'components/member-detail-modal',
    component: MemberDetailModal,
} satisfies Meta<typeof MemberDetailModal>

export default meta;

type Story = StoryObj<typeof MemberDetailModal>

export const Default: Story = {
    args: {
        open: true,
        memberId: 'tester',
        onClose: action('closeModal')
    },
    parameters: {
        msw: {
            handlers: [
                http.get('/api/users/:userId', ({params}) => {
                    const {userId} = params
                    const memberDetail: MemberDetailInfo = {
                        id: String(userId) ?? 'N/A',
                        name: '이현우',
                        nickname: '이현우22',
                        signupDate: '2023-11-04',
                        lastUpdateDate: '2023-11-04',
                        birthday: null,
                        activate: true,
                        registerAdminId: 'admin'
                    }

                    return Response.json({
                        status: 200,
                        statusCode: 'Ok',
                        data: memberDetail
                        ,
                    });
                })
            ]
        }
    }
}
