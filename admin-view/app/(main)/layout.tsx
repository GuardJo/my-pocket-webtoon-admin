import SideMenuBar from "@/components/side-menu-bar";

export default function MainLayout({children}: { children: React.ReactNode }) {
    return (
        <div className='flex h-screen overflow-hidden bg-gray-50'>
            <SideMenuBar/>
            <main className="min-w-0 flex-1 overflow-y-auto">
                {children}
            </main>
        </div>
    );
}
