import {Meta, StoryObj} from "@storybook/nextjs-vite";
import SideMenuBar from "@/components/side-menu-bar";

const meta = {
    title: 'components/side-menu-bar',
    component: SideMenuBar,
} satisfies Meta<typeof SideMenuBar>

export default meta;

type Story = StoryObj<typeof SideMenuBar>

export const Default: Story = {}