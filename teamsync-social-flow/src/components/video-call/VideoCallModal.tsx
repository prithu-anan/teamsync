import React, { useEffect, useRef, useState } from 'react';
import { X, Video } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { ZegoUIKitPrebuilt } from '@zegocloud/zego-uikit-prebuilt';
import type { Channel, User } from '@/types/messages';

interface VideoCallModalProps {
  isOpen: boolean;
  onClose: () => void;
  channel: Channel;
  currentUser: User;
  participants: User[];
}

// Helper function to generate random ID
function randomID(len: number = 5): string {
  let result = '';
  const chars = '12345qwertyuiopasdfgh67890jklmnbvcxzMNBVCZXASDQWERTYHGFUIOLKJP';
  const maxPos = chars.length;
  len = len || 5;
  for (let i = 0; i < len; i++) {
    result += chars.charAt(Math.floor(Math.random() * maxPos));
  }
  return result;
}

const VideoCallModal: React.FC<VideoCallModalProps> = ({
  isOpen,
  onClose,
  channel,
  currentUser,
  participants
}) => {
  const containerRef = useRef<HTMLDivElement>(null);
  const zpRef = useRef<any>(null);
  const [isInitialized, setIsInitialized] = useState(false);
  const [isMinimized, setIsMinimized] = useState(false);

  useEffect(() => {
    if (isOpen && containerRef.current && !isInitialized) {
      initializeVideoCall();
    } else if (!isOpen && zpRef.current) {
      // Clean up when modal is closed
      try {
        if (containerRef.current) {
          containerRef.current.innerHTML = '';
        }
        zpRef.current.destroy();
        zpRef.current = null;
        setIsInitialized(false);
      } catch (error) {
        console.error('Error during modal close cleanup:', error);
      }
    }
  }, [isOpen, isInitialized]);

  // Cleanup effect to properly destroy Zego instance
  useEffect(() => {
    return () => {
      if (zpRef.current) {
        try {
          // Clear the container content first
          if (containerRef.current) {
            containerRef.current.innerHTML = '';
          }
          // Destroy the Zego instance to prevent duplicate joins
          zpRef.current.destroy();
          zpRef.current = null;
        } catch (error) {
          console.error('Error during Zego cleanup:', error);
        }
      }
    };
  }, []);


  const initializeVideoCall = async () => {
    if (!containerRef.current || isInitialized) return;

    try {
      // Get environment variables
      const appID = import.meta.env.VITE_ZEGO_APP_ID;
      const serverSecret = import.meta.env.VITE_ZEGO_SERVER_SECRET;

      if (!appID || !serverSecret) {
        console.error('ZEGO credentials not found in environment variables');
        return;
      }

      // Generate room ID based on channel
      const roomID = `room_${channel.id}`;
      
      // Generate Kit Token
      const kitToken = ZegoUIKitPrebuilt.generateKitTokenForTest(
        parseInt(appID),
        serverSecret,
        roomID,
        currentUser.id.toString(),
        currentUser.name || currentUser.email || 'User'
      );

      // Create ZegoUIKitPrebuilt instance
      const zp = ZegoUIKitPrebuilt.create(kitToken);
      zpRef.current = zp;

      // Determine call mode based on number of participants
      const isGroupCall = participants.length > 2;
      const callMode = isGroupCall ? ZegoUIKitPrebuilt.GroupCall : ZegoUIKitPrebuilt.OneONoneCall;

      // Start the call
      await zp.joinRoom({
        container: containerRef.current,
        scenario: {
          mode: callMode,
        },
        showPreJoinView: true,
        preJoinViewConfig: {
          title: `Calling ${channel.name || 'Channel'}`,
        },
        turnOnMicrophoneWhenJoining: true,
        turnOnCameraWhenJoining: true,
        showTextChat: true,
        showUserList: true,
        showScreenSharingButton: true,
        showRoomDetailsButton: true,
        branding: {
          logoURL: undefined, // You can add your logo URL here
        },
        onJoinRoom: () => {
          console.log('Successfully joined the call room');
        },
        onLeaveRoom: () => {
          console.log('Left the call room');
          // Close the window immediately when leaving the call
          onClose();
        },
        onUserJoin: (users) => {
          console.log('User joined:', users);
        },
        onUserLeave: (users) => {
          console.log('User left:', users);
        },
      });

      setIsInitialized(true);
    } catch (error) {
      console.error('Failed to initialize video call:', error);
    }
  };

  const handleMinimize = () => {
    setIsMinimized(true);
  };

  const handleMaximize = () => {
    setIsMinimized(false);
  };

  const handleClose = () => {
    // Clean up Zego instance before closing
    if (zpRef.current) {
      try {
        // Clear the container content first
        if (containerRef.current) {
          containerRef.current.innerHTML = '';
        }
        // Add a small delay to ensure DOM cleanup
        setTimeout(() => {
          if (zpRef.current) {
            zpRef.current.destroy();
            zpRef.current = null;
          }
        }, 100);
      } catch (error) {
        console.error('Error destroying Zego instance:', error);
      }
    }
    // Reset initialization state
    setIsInitialized(false);
    // Close the modal
    onClose();
  };

  if (!isOpen) return null;

  if (isMinimized) {
    return (
      <div className="fixed bottom-4 right-4 z-50">
        <div className="bg-gray-800 rounded-lg shadow-2xl border border-gray-600 p-3 min-w-[180px">
          {/* Minimized header */}
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <div className="w-2 h-2 bg-red-500 rounded-full"></div>
              <span className="text-white text-xs font-medium">Call in progress</span>
            </div>
            <Button
              onClick={handleMaximize}
              variant="ghost"
              size="sm"
              className="h-6 w-6 p-0 text-gray-400 hover:text-white"
            >
              <Video className="h-3 w-3" />
            </Button>
          </div>
          
          {/* Minimized controls */}
          <div className="flex justify-center mt-2">
            <Button
              onClick={handleMaximize}
              size="sm"
              className="bg-blue-600 hover:bg-blue-700 text-white text-xs px-3 py-1"
            >
              Restore
            </Button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80">
      <div className="relative w-full h-full max-w-7xl max-h-[90vh] bg-white rounded-lg overflow-hidden">
        {/* Header */}
        <div className="absolute top-4 right-4 z-10 flex gap-2">
          <Button
            variant="ghost"
            size="icon"
            onClick={handleMinimize}
            className="bg-white hover:bg-gray-100 text-gray-700"
            title="Minimize call"
          >
            <Video className="h-4 w-4" />
          </Button>
          <Button
            variant="ghost"
            size="icon"
            onClick={handleClose}
            className="bg-white hover:bg-gray-100 text-gray-700"
            title="End call"
          >
            <X className="h-4 w-4" />
          </Button>
        </div>

        {/* Video Call Container */}
        <div 
          ref={containerRef}
          className="w-full h-full"
          // style={{ minHeight: '600px' }}
        />
      </div>
    </div>
  );
};

export default VideoCallModal;
