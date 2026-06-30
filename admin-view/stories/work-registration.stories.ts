import {Meta, StoryObj} from "@storybook/nextjs-vite";
import WorkRegistration from "@/components/work-registration";

const meta = {
    title: 'components/work-registration',
    component: WorkRegistration,
} satisfies Meta<typeof WorkRegistration>

export default meta;

type Story = StoryObj<typeof WorkRegistration>

export const Default: Story = {}