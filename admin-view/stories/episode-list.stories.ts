import {Meta, StoryObj} from "@storybook/nextjs-vite";
import EpisodeList from "@/components/episode-list";

const meta = {
    title: 'components/episode-list',
    component: EpisodeList
} satisfies Meta<typeof EpisodeList>

export default meta;

type Story = StoryObj<typeof EpisodeList>

export const Default: Story = {
    args: {
        workId: 1,
        workTitle: '애늙은이'
    }
}