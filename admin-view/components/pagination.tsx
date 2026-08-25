/*
페이지네이션 바
 */
export default function Pagination({totalPage, currentPage, totalElement, pageSize, onPageChange}: PaginationProps) {
    const lastPage = totalPage - 1
    const visiblePages = getVisiblePages(totalPage, currentPage)

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
                {visiblePages.map((page, index) => (
                    page === 'ellipsis' ? (
                        <span key={`ellipsis-${index}`} className="text-gray-400 px-1">...</span>
                    ) : (
                        <button
                            key={page}
                            onClick={() => onPageChange(page)}
                            className={`w-9 h-9 rounded-lg text-sm font-semibold transition-colors ${
                                currentPage === page
                                    ? 'bg-indigo-900 text-white'
                                    : 'text-gray-600 hover:bg-gray-100'
                            }`}
                        >
                            {page + 1}
                        </button>
                    )
                ))}
                <button
                    onClick={() => onPageChange(Math.min(lastPage, currentPage + 1))}
                    disabled={totalPage === 0 || currentPage >= lastPage}
                    className="flex items-center gap-1 text-sm font-semibold text-indigo-900 hover:text-indigo-700 disabled:opacity-50"
                >
                    Next
                </button>
            </div>
        </div>
    )
}

function getVisiblePages(totalPage: number, currentPage: number): Array<number | 'ellipsis'> {
    if (totalPage <= 5) {
        return Array.from({length: totalPage}, (_, index) => index)
    }

    if (currentPage < 2) {
        return [0, 1, 2, 'ellipsis', totalPage - 1]
    }

    if (currentPage >= totalPage - 3) {
        return [0, 'ellipsis', totalPage - 3, totalPage - 2, totalPage - 1]
    }

    return [0, 'ellipsis', currentPage - 1, currentPage, currentPage + 1, 'ellipsis', totalPage - 1]
}

interface PaginationProps {
    totalPage: number,
    currentPage: number,
    totalElement: number
    pageSize: number,
    onPageChange: (page: number) => void,
}
