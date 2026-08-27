import {Meta, StoryObj} from "@storybook/nextjs-vite";
import MemberDetailModal from "@/components/member-detail-modal";
import {action} from "storybook/actions";

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
    }
}
