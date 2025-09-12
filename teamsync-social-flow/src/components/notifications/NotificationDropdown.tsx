import { useState } from 'react';
import { useNotifications } from '@/contexts/NotificationContext';
import { Button } from '@/components/ui/button';
import { Separator } from '@/components/ui/separator';

export default function NotificationDropdown() {
  const { notifications, markAsRead, markAllAsRead } = useNotifications();
  const [busyId, setBusyId] = useState<string | null>(null);
  const [busyAll, setBusyAll] = useState(false);

  const handleMarkAll = async () => {
    try {
      setBusyAll(true);
      await markAllAsRead();
    } finally {
      setBusyAll(false);
    }
  };

  return (
    <div className="w-96 max-h-[28rem] overflow-auto bg-background border border-border rounded-md shadow-lg">
      <div className="flex items-center justify-between px-3 py-2">
        <div className="font-semibold">Notifications</div>
        <Button variant="ghost" size="sm" onClick={handleMarkAll} disabled={busyAll}>
          Mark all as read
        </Button>
      </div>
      <Separator />
      <div className="p-2 space-y-1">
        {notifications.length === 0 && (
          <div className="text-center text-muted-foreground py-8">No notifications</div>
        )}
        {notifications.map(n => (
          <div key={n.id} className={`p-3 rounded-md border ${!n.isRead ? 'bg-accent/30 border-primary/40' : 'bg-muted/20 border-border'}`}>
            <div className="flex items-start justify-between gap-2">
              <div>
                <div className="text-sm font-medium">{n.title}</div>
                <div className="text-sm text-muted-foreground">{n.message}</div>
                <div className="text-[10px] text-muted-foreground mt-1">{new Date(n.createdAt).toLocaleString()}</div>
              </div>
              {!n.isRead && (
                <Button
                  variant="outline"
                  size="sm"
                  onClick={async () => { setBusyId(n.id); await markAsRead(n.id); setBusyId(null); }}
                  disabled={busyId === n.id}
                >
                  Mark read
                </Button>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
