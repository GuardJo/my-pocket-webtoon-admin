import SideMenuBar from "@/components/side-menu-bar";

export default function MainLayout({children}: { children: React.ReactNode }) {
    return (
        <div className='flex h-full bg-gray-50'>
            <SideMenuBar/>
            {children}
        </div>
    );
}