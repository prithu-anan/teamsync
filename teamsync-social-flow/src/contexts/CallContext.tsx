import React, { createContext, useContext, useState, useCallback, useEffect } from 'react';
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

interface CallContextType {
  activeCalls: Map<string, ActiveCall>;
  currentCall: ActiveCall | null;
  joinCall: (call: ActiveCall) => void;
  leaveCall: () => void;
  startCall: (call: ActiveCall) => void;
  endCall: (roomID: string) => void;
  getActiveCallForChannel: (channelId: string) => ActiveCall | null;
}

const CallContext = createContext<CallContextType | undefined>(undefined);

export const useCallContext = () => {
  const context = useContext(CallContext);
  if (!context) {
    throw new Error('useCallContext must be used within a CallProvider');
  }
  return context;
};

interface CallProviderProps {
  children: React.ReactNode;
}

export const CallProvider: React.FC<CallProviderProps> = ({ children }) => {
  const [activeCalls, setActiveCalls] = useState<Map<string, ActiveCall>>(new Map());
  const [currentCall, setCurrentCall] = useState<ActiveCall | null>(null);

  const startCall = useCallback((call: ActiveCall) => {
    setActiveCalls(prev => new Map(prev).set(call.roomID, call));
    setCurrentCall(call);
  }, []);

  const joinCall = useCallback((call: ActiveCall) => {
    setCurrentCall(call);
  }, []);

  const leaveCall = useCallback(() => {
    setCurrentCall(null);
  }, []);

  const endCall = useCallback((roomID: string) => {
    setActiveCalls(prev => {
      const newMap = new Map(prev);
      newMap.delete(roomID);
      return newMap;
    });
    
    if (currentCall?.roomID === roomID) {
      setCurrentCall(null);
    }
  }, [currentCall]);

  const getActiveCallForChannel = useCallback((channelId: string) => {
    for (const call of activeCalls.values()) {
      if (call.channel.id === channelId) {
        return call;
      }
    }
    return null;
  }, [activeCalls]);

  const contextValue: CallContextType = {
    activeCalls,
    currentCall,
    joinCall,
    leaveCall,
    startCall,
    endCall,
    getActiveCallForChannel
  };

  return (
    <CallContext.Provider value={contextValue}>
      {children}
    </CallContext.Provider>
  );
};
