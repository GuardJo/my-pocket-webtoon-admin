import type {Meta, StoryObj} from '@storybook/nextjs-vite'
import LoginPage from '@/components/login-page'
import {http, HttpResponse} from "msw";

const meta = {
    title: 'components/login-page',
    component: LoginPage,
} satisfies Meta<typeof LoginPage>

export default meta

type Story = StoryObj<typeof LoginPage>

export const LoginSuccess: Story = {
    parameters: {
        msw: {
            handlers: [
                http.post('/auth/login', () => {
                    return HttpResponse.json({success: true});
                })
            ]
        }
    }
}

export const LoginFailed: Story = {
    parameters: {
        msw: {
            handlers: [
                http.post('/auth/login', () => {
                    return HttpResponse.json({
                        status: 401,
                        statusCode: 'UNAUTHORIZATION',
                        data: '인증에 실패하였습니다.'
                    }, {status: 401});
                })
            ]
        }
    }
}