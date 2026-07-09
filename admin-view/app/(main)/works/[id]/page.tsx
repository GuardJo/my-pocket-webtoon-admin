import WorkDetail from "@/components/work-detail";

export default async function WorkDetailPage({params}: { params: Promise<{ id: string }> }) {
    const {id} = await params;
    return <WorkDetail workId={Number(id)}/>;
}