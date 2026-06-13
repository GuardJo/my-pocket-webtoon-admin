import {Meta, StoryObj} from "@storybook/nextjs-vite";
import WorkManagementTable from "@/components/work-management-table";
import {http} from "msw";
import {Pageable, PageInfo, WorkInfo} from "@/lib/models";

const meta = {
    title: 'components/work-management-table',
    component: WorkManagementTable,
} satisfies Meta<typeof WorkManagementTable>

export default meta;

type Story = StoryObj<typeof WorkManagementTable>

export const HasData: Story = {
    parameters: {
        msw: {
            handlers: [
                http.get('/api/works', () => {
                    const currentPage = 0;
                    const pageSize = 10;

                    const pageInfo: PageInfo = {
                        page: currentPage,
                        size: pageSize,
                        totalElements: 2,
                        totalPages: 1
                    }

                    const workInfos: WorkInfo[] = [
                        {
                            id: 2,
                            thumbnailUrl: null,
                            title: "애늙은이",
                            serialState: "COMPLETED",
                            visibility: false
                        },
                        {
                            id: 1,
                            thumbnailUrl: "http://localhost:8080/uploads/thumbnail/afab1c268eb049ef9733be35c223cdd1.jpg",
                            title: "마신",
                            serialState: "PUBLISHED",
                            visibility: true
                        },
                        {
                            id: 2,
                            thumbnailUrl: null,
                            title: "마음의소리",
                            serialState: "SUSPENDED",
                            visibility: true
                        }
                    ]

                    const pageable: Pageable<WorkInfo> = {
                        content: workInfos,
                        page: pageInfo
                    }

                    return Response.json({
                        'status': 200,
                        'statusCode': 'OK',
                        'data': pageable
                    })
                })
            ]
        }
    }
}

export const NoData: Story = {
    parameters: {
        msw: {
            handlers: [
                http.get('/api/works', () => {
                    const currentPage = 0;
                    const pageSize = 10;

                    const pageInfo: PageInfo = {
                        page: currentPage,
                        size: pageSize,
                        totalElements: 0,
                        totalPages: 1
                    }

                    const pageable: Pageable<WorkInfo> = {
                        content: [],
                        page: pageInfo
                    }

                    return Response.json({
                        'status': 200,
                        'statusCode': 'OK',
                        'data': pageable
                    })
                })
            ]
        }
    }
}