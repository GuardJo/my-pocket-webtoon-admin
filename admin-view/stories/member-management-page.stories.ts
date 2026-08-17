import {Meta, StoryObj} from "@storybook/nextjs-vite";
import {MembersManagementPage} from "@/components/member-management-page";

const meta = {
    title: 'components/member-management-page',
    component: MembersManagementPage,
} satisfies Meta<typeof MembersManagementPage>

export default meta;

type Story = StoryObj<typeof MembersManagementPage>

export const Default: Story = {
    args: {}
}