import type {Meta, StoryObj} from '@storybook/nextjs-vite'
import LoginPage from '@/components/login-page'

const meta = {
    title: 'components/login-page',
    component: LoginPage,
} satisfies Meta<typeof LoginPage>

export default meta

type Story = StoryObj<typeof LoginPage>

export const Primary: Story = {
    args: {}
}