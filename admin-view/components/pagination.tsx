/*
페이지네이션 바
 */
export default function Pagination({totalPage, currentPage, totalElement, pageSize, onPageChange}: PaginationProps) {
    return (
        <div className="flex items-center justify-between px-6 py-4 border-t border-gray-100">
            <p className="text-xs font-semibold text-gray-500 uppercase tracking-wider">
                Showing {currentPage + 1} to {pageSize} of {totalElement} entries
            </p>
            <div className="flex items-center gap-2">
                <button
                    onClick={() => onPageChange(Math.max(0, currentPage - 1))}
                    disabled={currentPage === 0}
                    className="flex items-center gap-1 text-sm text-gray-400 hover:text-gray-600 disabled:opacity-50"
                >
                    Previous
                </button>
                {Array.from({length: Math.min(3, totalPage)}, (_, i) => (
                    <button
                        key={i + 1}
                        onClick={() => onPageChange(i)}
                        className={`w-9 h-9 rounded-lg text-sm font-semibold transition-colors ${
                            currentPage === i
                                ? 'bg-indigo-900 text-white'
                                : 'text-gray-600 hover:bg-gray-100'
                        }`}
                    >
                        {i + 1}
                    </button>
                ))}
                {totalPage > 3 && (
                    <>
                        <span className="text-gray-400 px-1">...</span>
                        <button
                            onClick={() => onPageChange(totalPage - 1)}
                            className={`w-9 h-9 rounded-lg text-sm font-semibold transition-colors ${
                                currentPage + 1 === totalPage
                                    ? 'bg-indigo-900 text-white'
                                    : 'text-gray-600 hover:bg-gray-100'
                            }`}
                        >
                            {totalPage}
                        </button>
                    </>
                )}
                <button
                    onClick={() => onPageChange(Math.min(totalPage, currentPage + 1))}
                    className="flex items-center gap-1 text-sm font-semibold text-indigo-900 hover:text-indigo-700"
                >
                    Next
                </button>
            </div>
        </div>
    )
}

interface PaginationProps {
    totalPage: number,
    currentPage: number,
    totalElement: number
    pageSize: number,
    onPageChange: (page: number) => void,
}