/* 상태 정보 카드 컴포넌트 */
import {ForwardRefExoticComponent} from "react";
import {LucideProps} from "lucide-react";

export default function StatCard({label, value, caption, statIcon, valueColor, captionColor}: StatCardProps) {
    const StatIcon = statIcon;
    return (
        <div
            key={label}
            className="bg-white rounded-xl border border-gray-200 p-6"
        >
            <p className="text-xs font-semibold text-gray-500 uppercase tracking-wider mb-3">
                {label}
            </p>
            <p className={`text-4xl font-bold mb-3 ${valueColor}`}>
                {value}
            </p>
            <div
                className={`flex items-center gap-1.5 text-sm ${captionColor}`}
            >
                <StatIcon className="w-4 h-4"/>
                <span>{caption}</span>
            </div>
        </div>
    )
}

interface StatCardProps {
    label: string;
    value: string;
    caption: string;
    statIcon: ForwardRefExoticComponent<Omit<LucideProps, "ref">>
    valueColor: string;
    captionColor: string;
}