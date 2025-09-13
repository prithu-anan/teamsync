import React, { useState } from 'react';
import { Video, Phone, Users } from 'lucide-react';
import { Button } from '@/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { callInvitationService } from '@/services/callInvitationService';
import type { Channel, User } from '@/types/messages';

interface CallButtonProps {
  channel: Channel;
  currentUser: User;
  participants: User[];
  otherParticipants?: User[];
  canStartCall?: boolean;
  isGroupCall?: boolean;
  loading?: boolean;
  onCallStarted?: (roomID: string) => void;
}

const CallButton: React.FC<CallButtonProps> = ({
  channel,
  currentUser,
  participants,
  otherParticipants = [],
  canStartCall = false,
  isGroupCall = false,
  loading = false,
  onCallStarted
}) => {
  const [isCalling, setIsCalling] = useState(false);

  const generateRoomID = (): string => {
    return `room_${channel.id}`;
  };

  const handleStartCall = async (callType: 'video' | 'audio') => {
    try {
      setIsCalling(true);
      const roomID = generateRoomID();

      // Use otherParticipants if available, otherwise filter from participants
      const invitees = otherParticipants.length > 0 ? otherParticipants : participants.filter(p => p.id !== currentUser.id);
      
      if (invitees.length === 0) {
        console.warn('No other participants to call');
        return;
      }

      await callInvitationService.sendCallInvitation({
        roomID,
        channel,
        caller: currentUser,
        invitees,
        callType
      });

      // Notify parent component that call has started
      onCallStarted?.(roomID);

      console.log(`${callType} call initiated successfully`);
      
      // Show success message
      console.log(`✅ ${callType} call room created: ${roomID}`);
      console.log('📞 Other participants can join by navigating to the same room ID');
    } catch (error) {
      console.error(`Failed to start ${callType} call:`, error);
    } finally {
      setIsCalling(false);
    }
  };

  const getChannelDisplayName = (): string => {
    if (channel.type === 'direct') {
      const otherParticipant = otherParticipants.length > 0 ? otherParticipants[0] : participants.find(p => p.id !== currentUser.id);
      return otherParticipant?.username || otherParticipant?.email || 'Unknown User';
    }
    return channel.name || 'Channel';
  };

  // Determine if button should be disabled
  const isDisabled = loading || !canStartCall || isCalling;

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button
          variant="ghost"
          size="icon"
          disabled={isDisabled}
          title={loading ? "Loading participants..." : !canStartCall ? "Not enough participants" : "Start a call"}
        >
          {isCalling || loading ? (
            <div className="animate-spin rounded-full h-4 w-4 border-2 border-current border-t-transparent" />
          ) : (
            <Video className="h-4 w-4" />
          )}
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end" className="w-48">
        <DropdownMenuItem 
          onClick={() => handleStartCall('video')}
          disabled={isDisabled}
          className="flex items-center gap-2"
        >
          <Video className="h-4 w-4" />
          Start Video Call
          {isGroupCall && <Users className="h-3 w-3 ml-auto" />}
        </DropdownMenuItem>
        <DropdownMenuItem 
          onClick={() => handleStartCall('audio')}
          disabled={isDisabled}
          className="flex items-center gap-2"
        >
          <Phone className="h-4 w-4" />
          Start Audio Call
          {isGroupCall && <Users className="h-3 w-3 ml-auto" />}
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );
};

export default CallButton;
