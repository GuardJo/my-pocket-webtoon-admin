import {Meta, StoryObj} from "@storybook/nextjs-vite";
import Pagination from "@/components/pagination";
import {action} from "storybook/actions";

const meta = {
    title: 'components/pagination',
    component: Pagination,
} satisfies Meta<typeof Pagination>

export default meta;

type Story = StoryObj<typeof Pagination>

export const Default: Story = {
    args: {
        currentPage: 0,
        totalElement: 999,
        totalPage: 99,
        pageSize: 10,
        onPageChange: action("page changed")
    }
}