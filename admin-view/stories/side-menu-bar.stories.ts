import {Meta, StoryObj} from "@storybook/nextjs-vite";
import SideMenuBar from "@/components/side-menu-bar";
import {http} from "msw";

const meta = {
    title: 'components/side-menu-bar',
    component: SideMenuBar,
    parameters: {
        msw: {
            handlers: [
                http.get('/auth/me', () => {
                    return Response.json({
                        'status': 200,
                        'statusCode': 'OK',
                        'data': {
                            'id': 'admin',
                            'roleName': 'ADMIN',
                        }
                    })
                })
            ]
        }
    }
} satisfies Meta<typeof SideMenuBar>

export default meta;

type Story = StoryObj<typeof SideMenuBar>

export const Default: Story = {}