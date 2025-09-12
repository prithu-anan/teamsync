import { Bell } from 'lucide-react';
import { useState } from 'react';
import { useNotifications } from '@/contexts/NotificationContext';
import { Button } from '@/components/ui/button';

interface Props {
  onClick?: () => void;
}

export default function NotificationBell({ onClick }: Props) {
  const { unreadCount } = useNotifications();
  const [hover, setHover] = useState(false);

  return (
    <div className="relative">
      <Button
        variant="ghost"
        className="relative h-8 w-8 rounded-full"
        onMouseEnter={() => setHover(true)}
        onMouseLeave={() => setHover(false)}
        onClick={onClick}
        aria-label="Notifications"
      >
        <Bell className="h-5 w-5" />
        {unreadCount > 0 && (
          <span className="absolute -top-1 -right-1 inline-flex items-center justify-center rounded-full bg-red-500 text-white text-[10px] h-4 min-w-4 px-1">
            {unreadCount > 99 ? '99+' : unreadCount}
          </span>
        )}
      </Button>
    </div>
  );
}
