import {Meta, StoryObj} from "@storybook/nextjs-vite";
import StatCard from "@/components/stat-card";
import {CheckCircle2, Clock, TrendingUp, Zap} from "lucide-react";

const meta = {
    title: 'components/stat-card',
    component: StatCard,
} satisfies Meta<typeof StatCard>

export default meta;

type Story = StoryObj<typeof StatCard>

export const TOTAL_USESE: Story = {
    args: {
        label: '전체 사용자',
        value: '12,482',
        caption: '12% from last month',
        statIcon: TrendingUp,
        valueColor: 'text-indigo-900',
        captionColor: 'text-orange-500'
    }
}

export const ACTIVE_NOW: Story = {
    args: {
        label: '활성 사용자',
        value: '1,892',
        caption: 'Real-time engagement',
        statIcon: Zap,
        valueColor: 'text-indigo-900',
        captionColor: 'text-gray-500',
    },
}

export const PENDING_APPROVAL: Story = {
    args: {
        label: '승인 대기중',
        value: '24',
        caption: 'Awaiting review',
        statIcon: Clock,
        valueColor: 'text-orange-500',
        captionColor: 'text-gray-500',
    }
}

export const RETENTION_RATE: Story = {
    args: {
        label: '유지율',
        value: '84.2%',
        caption: 'Target: 85%',
        statIcon: CheckCircle2,
        valueColor: 'text-indigo-900',
        captionColor: 'text-gray-500',
    }
}