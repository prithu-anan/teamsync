import React, { useState, useEffect } from 'react';
import { Video, Phone, PhoneOff, Users, Clock } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import type { Channel, User } from '@/types/messages';

interface CallInvitationMessage {
  roomID: string;
  channel: Channel;
  caller: User;
  callType: 'video' | 'audio';
  timestamp: Date;
}

interface IncomingCallNotificationProps {
  invitation: CallInvitationMessage;
  onAccept: () => void;
  onDecline: () => void;
  autoDeclineAfter?: number; // milliseconds
}

const IncomingCallNotification: React.FC<IncomingCallNotificationProps> = ({
  invitation,
  onAccept,
  onDecline,
  autoDeclineAfter = 60000 // 60 seconds default
}) => {
  const [timeLeft, setTimeLeft] = useState(Math.floor(autoDeclineAfter / 1000));
  const [isVisible, setIsVisible] = useState(true);

  useEffect(() => {
    if (!isVisible) return;

    const timer = setInterval(() => {
      setTimeLeft(prev => {
        if (prev <= 1) {
          onDecline();
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(timer);
  }, [isVisible, onDecline]);

  const handleAccept = () => {
    setIsVisible(false);
    onAccept();
  };

  const handleDecline = () => {
    setIsVisible(false);
    onDecline();
  };

  if (!isVisible) return null;

  const getCallerDisplayName = () => {
    return invitation.caller.username || invitation.caller.email || invitation.caller.name || 'Unknown User';
  };

  const getChannelDisplayName = () => {
    if (invitation.channel.type === 'direct') {
      return 'Direct Message';
    }
    return invitation.channel.name || 'Channel';
  };

  return (
    <div className="fixed top-4 right-4 z-50 bg-white dark:bg-gray-800 rounded-lg shadow-2xl border border-gray-200 dark:border-gray-700 p-6 max-w-sm w-full animate-in slide-in-from-right-full duration-300">
      {/* Header */}
      <div className="flex items-center gap-3 mb-4">
        <div className="w-12 h-12 bg-gradient-to-br from-blue-500 to-purple-600 rounded-full flex items-center justify-center text-white font-bold text-lg">
          {getCallerDisplayName().charAt(0).toUpperCase()}
        </div>
        <div className="flex-1">
          <h3 className="font-semibold text-gray-900 dark:text-white text-lg">
            Incoming {invitation.callType === 'video' ? 'Video' : 'Audio'} Call
          </h3>
          <p className="text-sm text-gray-600 dark:text-gray-300">
            {getCallerDisplayName()} is calling you
          </p>
        </div>
      </div>

      {/* Channel Info */}
      <div className="mb-4 p-3 bg-gray-50 dark:bg-gray-700 rounded-lg">
        <div className="flex items-center gap-2 text-sm text-gray-600 dark:text-gray-300">
          <Users className="h-4 w-4" />
          <span>{getChannelDisplayName()}</span>
        </div>
      </div>

      {/* Timer */}
      <div className="mb-4 flex items-center justify-center">
        <div className="flex items-center gap-2 text-sm text-gray-500 dark:text-gray-400">
          <Clock className="h-4 w-4" />
          <span>Auto-decline in {timeLeft}s</span>
        </div>
      </div>

      {/* Action Buttons */}
      <div className="flex gap-3">
        <Button
          onClick={handleAccept}
          className="flex-1 bg-green-500 hover:bg-green-600 text-white"
          size="lg"
        >
          {invitation.callType === 'video' ? (
            <Video className="h-4 w-4 mr-2" />
          ) : (
            <Phone className="h-4 w-4 mr-2" />
          )}
          Accept
        </Button>
        <Button
          onClick={handleDecline}
          variant="destructive"
          className="flex-1"
          size="lg"
        >
          <PhoneOff className="h-4 w-4 mr-2" />
          Decline
        </Button>
      </div>

      {/* Call Type Badge */}
      <div className="mt-3 flex justify-center">
        <Badge variant="secondary" className="text-xs">
          {invitation.callType === 'video' ? 'Video Call' : 'Audio Call'}
        </Badge>
      </div>
    </div>
  );
};

export default IncomingCallNotification;
