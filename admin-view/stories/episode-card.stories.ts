import {Meta, StoryObj} from "@storybook/nextjs-vite";
import EpisodeCard from "@/components/episode-card";

const meta = {
    title: 'components/episode-card',
    component: EpisodeCard
} satisfies Meta<typeof EpisodeCard>

export default meta;

type Story = StoryObj<typeof EpisodeCard>

export const Default: Story = {
    args: {
        workTitle: '애늙은이',
        episodeInfo: {
            id: 127,
            workId: 1,
            episodeNo: 127,
            episodeThumbnailUrl: 'https://images.unsplash.com/photo-1518895949257-7621c3c786d7?w=300&h=200&fit=crop',
            episodeImageTotalCount: 68,
            lastUpdateDate: '2023.10.17',
        },
    }
}