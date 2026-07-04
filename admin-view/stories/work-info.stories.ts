import {Meta, StoryObj} from "@storybook/nextjs-vite";
import WorkInfo from "@/components/work-info";

const meta = {
    title: 'components/work-info',
    component: WorkInfo,
} satisfies Meta<typeof WorkInfo>

export default meta;

type Story = StoryObj<typeof WorkInfo>;

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
    }
}