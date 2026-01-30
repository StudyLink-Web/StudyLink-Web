import React, { useState, useEffect, useCallback } from 'react';
import { Bell, X, Check } from 'lucide-react';

interface Notification {
    id: number;
    type: string;
    message: string;
    isRead: boolean;
    relatedId: number | null;
    createdAt: string;
}

interface NotificationCenterProps {
    isOpen: boolean;
    onClose: () => void;
    onUnreadCountChange: (count: number) => void;
    pushToken: string | null;
    onTestPush: () => void;
    onTestMine: () => void;
    onTestAll: () => void;
}

const NotificationCenter = React.forwardRef<HTMLDivElement, NotificationCenterProps>(({ 
    isOpen, 
    onClose, 
    onUnreadCountChange,
    pushToken,
    onTestPush,
    onTestMine,
    onTestAll
}, ref) => {
    const [notifications, setNotifications] = useState<Notification[]>([]);
    const [loading, setLoading] = useState(false);
    const [isAdminMode] = useState(false);

    const fetchNotifications = useCallback(async () => {
        setLoading(true);
        try {
            const response = await fetch('/api/notifications');
            if (response.ok) {
                const data = await response.json();
                setNotifications(data);
                onUnreadCountChange(data.filter((n: Notification) => !n.isRead).length);
            }
        } catch (error) {
            console.error('Failed to fetch notifications:', error);
        } finally {
            setLoading(false);
        }
    }, [onUnreadCountChange]);

    useEffect(() => {
        if (isOpen) {
            fetchNotifications();
        }
    }, [isOpen, fetchNotifications]);

    const markAsRead = async (id: number) => {
        try {
            const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

            const response = await fetch(`/api/notifications/${id}/read`, { 
                method: 'PUT',
                headers: {
                    ...(csrfHeader && csrfToken ? { [csrfHeader]: csrfToken } : {})
                }
            });
            if (response.ok) {
                const updated = notifications.map(n => n.id === id ? { ...n, isRead: true } : n);
                setNotifications(updated);
                const unreadCount = updated.filter(n => !n.isRead).length;
                onUnreadCountChange(unreadCount);
                
                // ⭐ 헤더 동기화를 위한 커스텀 이벤트 발송 (현재 숫자 포함)
                window.dispatchEvent(new CustomEvent('notificationUpdate', { 
                    detail: { count: unreadCount } 
                }));
            }
        } catch (error) {
            console.error('Failed to mark as read:', error);
        }
    };

    const markAllAsRead = async () => {
        try {
            const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

            const response = await fetch('/api/notifications/read-all', { 
                method: 'PUT',
                headers: {
                    ...(csrfHeader && csrfToken ? { [csrfHeader]: csrfToken } : {})
                }
            });
            if (response.ok) {
                const updated = notifications.map(n => ({ ...n, isRead: true }));
                setNotifications(updated);
                onUnreadCountChange(0);
                
                // ⭐ 헤더 동기화를 위한 커스텀 이벤트 발송 (0으로 초기화)
                window.dispatchEvent(new CustomEvent('notificationUpdate', { 
                    detail: { count: 0 } 
                }));
            }
        } catch (error) {
            console.error('Failed to mark all as read:', error);
        }
    };

    const formatTime = (dateString: string) => {
        const date = new Date(dateString);
        const now = new Date();
        const diffInSeconds = Math.floor((now.getTime() - date.getTime()) / 1000);

        if (diffInSeconds < 60) return '방금 전';
        if (diffInSeconds < 3600) return `${Math.floor(diffInSeconds / 60)}분 전`;
        if (diffInSeconds < 86400) return `${Math.floor(diffInSeconds / 3600)}시간 전`;
        return `${Math.floor(diffInSeconds / 86400)}일 전`;
    };

    if (!isOpen) return null;

    return (
        <div ref={ref} className="fixed inset-y-0 right-0 w-80 sm:w-96 bg-white/80 dark:bg-slate-900/90 backdrop-blur-xl border-l border-white/20 dark:border-white/5 z-[10000] shadow-2xl animate-in slide-in-from-right duration-300">
            <div className="flex flex-col h-full">
                {/* Header */}
                <div className="p-6 border-b border-slate-200 dark:border-white/5 flex items-center justify-between">
                    <div className="flex items-center gap-3">
                        <div className="p-2 bg-blue-500/10 rounded-xl">
                            <Bell size={20} className="text-blue-500" />
                        </div>
                        <h2 className="text-lg font-bold text-slate-900 dark:text-white">알림 센터</h2>
                    </div>
                    <button 
                        onClick={(e) => {
                            e.stopPropagation();
                            onClose();
                        }} 
                        className="p-2.5 bg-slate-100 dark:bg-white/10 hover:bg-slate-200 dark:hover:bg-white/20 rounded-full transition-all active:scale-90"
                    >
                        <X size={18} className="text-slate-900 dark:text-white" strokeWidth={3} />
                    </button>
                </div>

                {/* Actions */}
                {notifications.length > 0 && (
                    <div className="px-6 py-3 border-b border-slate-200 dark:border-white/5 flex justify-end">
                        <button 
                            onClick={markAllAsRead}
                            className="text-xs font-medium text-blue-500 hover:text-blue-600 flex items-center gap-1"
                        >
                            <Check size={14} /> 모두 읽음 처리
                        </button>
                    </div>
                )}

                {/* List */}
                <div className="flex-1 overflow-y-auto p-4 space-y-3 custom-scrollbar">
                    {loading ? (
                        <div className="flex flex-col items-center justify-center h-40 space-y-3">
                            <div className="w-8 h-8 border-2 border-blue-500 border-t-transparent rounded-full animate-spin" />
                            <p className="text-sm text-slate-500">알림을 불러오는 중...</p>
                        </div>
                    ) : notifications.length === 0 && !isAdminMode ? (
                        <div className="flex flex-col items-center justify-center h-full text-center space-y-4 px-6">
                            <div className="w-16 h-16 bg-slate-100 dark:bg-white/5 rounded-full flex items-center justify-center">
                                <Bell size={32} className="text-slate-300 dark:text-slate-600" />
                            </div>
                            <div>
                                <h3 className="text-base font-semibold text-slate-900 dark:text-white">새로운 알림이 없습니다</h3>
                                <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">중요한 업데이트가 있으면 여기에 표시됩니다.</p>
                            </div>
                        </div>
                    ) : (
                        <>
                            {isAdminMode && (
                                <div className="p-4 bg-slate-50 dark:bg-white/[0.03] rounded-3xl border border-slate-200 dark:border-white/10 space-y-4 mb-4 animate-in fade-in zoom-in-95 duration-300">
                                    <h3 className="text-[10px] font-black text-blue-500 uppercase tracking-widest flex items-center gap-2">
                                        <div className="w-1.5 h-1.5 bg-blue-500 rounded-full animate-pulse" />
                                        관리자 발송 도구
                                    </h3>
                                    
                                    <div className="bg-white dark:bg-slate-950 p-3 rounded-xl border border-slate-100 dark:border-white/5">
                                        <p className="text-[9px] font-bold text-slate-400 mb-1.5 uppercase">Push Token</p>
                                        <div className="font-mono text-[9px] text-slate-500 dark:text-slate-400 break-all leading-tight max-h-20 overflow-y-auto">
                                            {pushToken || '토큰이 없습니다.'}
                                        </div>
                                    </div>

                                    <div className="grid grid-cols-1 gap-2">
                                        <button onClick={onTestPush} className="w-full py-3 bg-slate-900 text-white text-[10px] font-black rounded-xl hover:bg-blue-600 transition-all flex items-center justify-center gap-2">
                                            🚀 나에게 테스트 발송
                                        </button>
                                        <button onClick={onTestMine} className="w-full py-3 bg-teal-600 text-white text-[10px] font-black rounded-xl hover:bg-teal-700 transition-all flex items-center justify-center gap-2">
                                            🔗 내 모든 기기 발송
                                        </button>
                                        <button onClick={onTestAll} className="w-full py-3 bg-blue-600 text-white text-[10px] font-black rounded-xl hover:bg-blue-700 transition-all flex items-center justify-center gap-2">
                                            📢 전체 공지 발송 (Admin)
                                        </button>
                                    </div>
                                </div>
                            )}
                            
                            {!isAdminMode && notifications.map((notification) => (
                                <div 
                                    key={notification.id}
                                    onClick={() => !notification.isRead && markAsRead(notification.id)}
                                    className={`p-4 rounded-2xl border transition-all duration-200 cursor-pointer ${
                                        notification.isRead 
                                        ? 'bg-transparent border-slate-100 dark:border-white/5 grayscale-[0.5] opacity-70' 
                                        : 'bg-white dark:bg-white/5 border-blue-100 dark:border-blue-500/20 shadow-sm hover:border-blue-300 dark:hover:border-blue-400/40'
                                    }`}
                                >
                                    <div className="flex gap-4">
                                        <div className={`mt-1 h-2 w-2 rounded-full shrink-0 ${notification.isRead ? 'bg-transparent' : 'bg-blue-500 animate-pulse'}`} />
                                        <div className="flex-1 space-y-1">
                                            <div className="flex items-center justify-between">
                                                <span className="text-[10px] font-black uppercase tracking-wider text-blue-500/90">{notification.type}</span>
                                                <span className="text-[10px] text-slate-500 dark:text-slate-400 font-medium">{formatTime(notification.createdAt)}</span>
                                            </div>
                                            <p className="text-sm text-slate-900 dark:text-white leading-relaxed font-bold">
                                                {notification.message}
                                            </p>
                                            {!notification.isRead && (
                                                <div className="mt-3 text-[10px] font-black text-blue-500 flex items-center gap-1">
                                                    클릭하여 읽음 처리
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </>
                    )}
                </div>

                {/* Footer Toggle - Commented out for now
                <div className="p-4 border-t border-slate-200 dark:border-white/5 bg-slate-50/50 dark:bg-white/[0.02]">
                    <button 
                        onClick={() => setIsAdminMode(!isAdminMode)}
                        className={`w-full py-3 rounded-xl text-[10px] font-black transition-all flex items-center justify-center gap-2 ${
                            isAdminMode 
                            ? 'bg-slate-200 dark:bg-white/10 text-slate-700 dark:text-white' 
                            : 'bg-white dark:bg-white/5 text-slate-500 dark:text-slate-400 border border-slate-200 dark:border-white/5'
                        }`}
                    >
                        {isAdminMode ? '알림 목록 보기' : '관리자 도구 열기'}
                    </button>
                </div>
                */}
            </div>
        </div>
    );
});

export default NotificationCenter;
