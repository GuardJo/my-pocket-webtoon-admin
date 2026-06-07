import {Meta, StoryObj} from "@storybook/nextjs-vite";
import WorkManagementTable from "@/components/work-management-table";

const meta = {
    title: 'components/work-management-table',
    component: WorkManagementTable,
} satisfies Meta<typeof WorkManagementTable>

export default meta;

type Story = StoryObj<typeof WorkManagementTable>

export const Default: Story = {}