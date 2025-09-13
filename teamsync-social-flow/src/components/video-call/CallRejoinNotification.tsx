import React, { useState, useEffect } from 'react';
import { Video, Phone, X, Clock } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import type { Channel, User } from '@/types/messages';

interface ActiveCall {
  roomID: string;
  channel: Channel;
  caller: User;
  participants: User[];
  callType: 'video' | 'audio';
  startTime: Date;
  isActive: boolean;
}

interface CallRejoinNotificationProps {
  activeCall: ActiveCall;
  onRejoin: () => void;
  onDismiss: () => void;
  autoHideAfter?: number; // milliseconds
}

const CallRejoinNotification: React.FC<CallRejoinNotificationProps> = ({
  activeCall,
  onRejoin,
  onDismiss,
  autoHideAfter = 30000 // 30 seconds default
}) => {
  const [timeLeft, setTimeLeft] = useState(Math.floor(autoHideAfter / 1000));
  const [isVisible, setIsVisible] = useState(true);

  useEffect(() => {
    if (!isVisible) return;

    const timer = setInterval(() => {
      setTimeLeft(prev => {
        if (prev <= 1) {
          onDismiss();
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(timer);
  }, [isVisible, onDismiss]);

  const handleRejoin = () => {
    setIsVisible(false);
    onRejoin();
  };

  const handleDismiss = () => {
    setIsVisible(false);
    onDismiss();
  };

  if (!isVisible) return null;

  const getChannelDisplayName = () => {
    if (activeCall.channel.type === 'direct') {
      return 'Direct Message';
    }
    return activeCall.channel.name || 'Channel';
  };

  const getCallDuration = () => {
    const now = new Date();
    const duration = Math.floor((now.getTime() - activeCall.startTime.getTime()) / 1000);
    const minutes = Math.floor(duration / 60);
    const seconds = duration % 60;
    return `${minutes}:${seconds.toString().padStart(2, '0')}`;
  };

  const getParticipantCount = () => {
    return activeCall.participants.length;
  };

  return (
    <div className="fixed bottom-4 right-4 z-50 bg-white dark:bg-gray-800 rounded-lg shadow-2xl border border-gray-200 dark:border-gray-700 p-4 max-w-sm w-full animate-in slide-in-from-bottom-full duration-300">
      {/* Header */}
      <div className="flex items-center justify-between mb-3">
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 bg-gradient-to-br from-green-500 to-blue-600 rounded-full flex items-center justify-center text-white text-sm">
            {activeCall.callType === 'video' ? (
              <Video className="h-4 w-4" />
            ) : (
              <Phone className="h-4 w-4" />
            )}
          </div>
          <div>
            <h4 className="font-semibold text-gray-900 dark:text-white text-sm">
              Ongoing Call
            </h4>
            <p className="text-xs text-gray-500 dark:text-gray-400">
              {getChannelDisplayName()}
            </p>
          </div>
        </div>
        <Button
          onClick={handleDismiss}
          variant="ghost"
          size="sm"
          className="h-6 w-6 p-0"
        >
          <X className="h-3 w-3" />
        </Button>
      </div>

      {/* Call Info */}
      <div className="mb-3 space-y-1">
        <div className="flex items-center justify-between text-xs text-gray-600 dark:text-gray-300">
          <span>Duration: {getCallDuration()}</span>
          <span>{getParticipantCount()} participant{getParticipantCount() !== 1 ? 's' : ''}</span>
        </div>
        <div className="flex items-center justify-between text-xs text-gray-500 dark:text-gray-400">
          <span>Auto-hide in {timeLeft}s</span>
          <Badge variant="outline" className="text-xs">
            {activeCall.callType === 'video' ? 'Video' : 'Audio'}
          </Badge>
        </div>
      </div>

      {/* Action Button */}
      <Button
        onClick={handleRejoin}
        className="w-full bg-blue-500 hover:bg-blue-600 text-white"
        size="sm"
      >
        {activeCall.callType === 'video' ? (
          <Video className="h-4 w-4 mr-2" />
        ) : (
          <Phone className="h-4 w-4 mr-2" />
        )}
        Rejoin Call
      </Button>
    </div>
  );
};

export default CallRejoinNotification;
