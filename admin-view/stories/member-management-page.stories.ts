import {Meta, StoryObj} from "@storybook/nextjs-vite";
import {MembersManagementPage} from "@/components/member-management-page";
import {http} from "msw";
import {MemberInfo, MemberMetrics, Pageable, PageInfo} from "@/lib/models";

const meta = {
    title: 'components/member-management-page',
    component: MembersManagementPage,
} satisfies Meta<typeof MembersManagementPage>

export default meta;

type Story = StoryObj<typeof MembersManagementPage>

export const HasMetricAndData: Story = {
    parameters: {
        msw: {
            handlers: [
                http.get('/api/users/metric', () => {
                    const mockMetrics: MemberMetrics = {
                        totalUsers: 12893,
                        activateUsers: 1892,
                        pendingUsers: 24,
                        retentionRate: 80.32,
                        monthlyMemberGrowth: 123,
                    }

                    return Response.json({
                        'status': 200,
                        'statusCode': 'Ok',
                        data: mockMetrics
                    })
                }),
                http.get('/api/users', () => {
                    const currentPage = 0;
                    const pageSize = 5;

                    const pageInfo: PageInfo = {
                        page: currentPage,
                        size: pageSize,
                        totalElements: 999,
                        totalPages: 99
                    }

                    const memberInfos: MemberInfo[] = [
                        {
                            id: 'USR-8912',
                            name: '김이름',
                            nickname: '김이룸',
                            signupDate: '2023-10-15',
                            activate: true
                        },
                        {
                            id: 'USR-9021',
                            name: '박지호',
                            nickname: '박지호캡',
                            signupDate: '2023-11-02',
                            activate: false
                        },
                        {
                            id: 'USR-8763',
                            name: '최수정',
                            nickname: '수정공주',
                            signupDate: '2023-09-28',
                            activate: true
                        },
                        {
                            id: 'USR-9102',
                            name: '이현우',
                            nickname: '이현우22',
                            signupDate: '2023-11-04',
                            activate: true
                        },
                        {
                            id: 'USR-8551',
                            name: 'Rose',
                            nickname: 'Sienna Rose',
                            signupDate: '2023-08-12',
                            activate: false
                        }
                    ]

                    const pageable: Pageable<MemberInfo> = {
                        page: pageInfo,
                        content: memberInfos
                    }

                    return Response.json({
                        'status': 200,
                        'statusCode': 'Ok',
                        'data': pageable
                    });
                })
            ]
        }
    }
}

export const NoData: Story = {
    args: {}
}