import {Meta, StoryObj} from "@storybook/nextjs-vite";
import MemberManagementTable from "@/components/member-management-table";

const meta = {
    title: 'components/member-management-table',
    component: MemberManagementTable,
} satisfies Meta<typeof MemberManagementTable>

export default meta;

type Story = StoryObj<typeof MemberManagementTable>

export const Default: Story = {
    args: {}
}