import { useState, useEffect, useMemo } from 'react';
import { getUsers, getUserById } from '@/utils/api-helpers';
import type { Channel, User } from '@/types/messages';

interface UseChannelParticipantsProps {
  channel: Channel | null;
  currentUser: User | null;
}

export const useChannelParticipants = ({ channel, currentUser }: UseChannelParticipantsProps) => {
  const [participants, setParticipants] = useState<User[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchParticipants = async () => {
      if (!channel || !currentUser) {
        setParticipants([]);
        return;
      }

      setLoading(true);
      setError(null);

      try {
        if (channel.type === 'direct') {
          // For direct messages, include both users
          const participants = [
            currentUser,
            {
              id: channel.recipient_id,
              username: channel.recipient_name,
              email: channel.recipient_email,
              name: channel.recipient_name
            } as User
          ].filter(Boolean);

          setParticipants(participants);
        } else if (channel.type === 'group' && channel.participants) {
          // For group channels, fetch user details for all participants
          const participantIds = channel.participants;
          
          if (participantIds.length === 0) {
            setParticipants([currentUser]);
            return;
          }

          // Fetch user details for all participants
          const participantDetails = await Promise.all(
            participantIds.map(async (userId: string) => {
              try {
                // Try to get user by ID first
                const userResponse = await getUserById(userId);
                if (userResponse && !userResponse.error) {
                  const userData = userResponse.data || userResponse;
                  return {
                    id: userId,
                    username: userData.username || userData.name || `User ${userId}`,
                    email: userData.email || `user${userId}@example.com`,
                    name: userData.name || userData.username || `User ${userId}`,
                    avatar: userData.avatar
                  } as User;
                }

                // Fallback: get all users and find the one we need
                const allUsersResponse = await getUsers();
                if (allUsersResponse && !allUsersResponse.error) {
                  const allUsers = allUsersResponse.data || allUsersResponse;
                  if (Array.isArray(allUsers)) {
                    const user = allUsers.find(u => String(u.id) === String(userId));
                    if (user) {
                      return {
                        id: userId,
                        username: user.username || user.name || `User ${userId}`,
                        email: user.email || `user${userId}@example.com`,
                        name: user.name || user.username || `User ${userId}`,
                        avatar: user.avatar
                      } as User;
                    }
                  }
                }

                // Final fallback: create a basic user object
                return {
                  id: userId,
                  username: `User ${userId}`,
                  email: `user${userId}@example.com`,
                  name: `User ${userId}`,
                  avatar: undefined
                } as User;
              } catch (error) {
                console.error(`Error fetching user ${userId}:`, error);
                // Return a fallback user object
                return {
                  id: userId,
                  username: `User ${userId}`,
                  email: `user${userId}@example.com`,
                  name: `User ${userId}`,
                  avatar: undefined
                } as User;
              }
            })
          );

          // Filter out any null results and ensure current user is included
          const validParticipants = participantDetails.filter(p => p !== null);
          const allParticipants = [...validParticipants];
          
          // Ensure current user is included if not already present
          const currentUserExists = allParticipants.some(p => String(p.id) === String(currentUser.id));
          if (!currentUserExists) {
            allParticipants.unshift(currentUser);
          }

          setParticipants(allParticipants);
        } else {
          // Fallback: just include current user
          setParticipants([currentUser]);
        }
      } catch (error) {
        console.error('Error fetching channel participants:', error);
        setError('Failed to load participants');
        // Fallback: include at least the current user
        setParticipants([currentUser]);
      } finally {
        setLoading(false);
      }
    };

    fetchParticipants();
  }, [channel, currentUser]);

  // Memoized values for performance
  const otherParticipants = useMemo(() => {
    return participants.filter(p => String(p.id) !== String(currentUser?.id));
  }, [participants, currentUser]);

  const canStartCall = useMemo(() => {
    return participants.length >= 2 && !loading;
  }, [participants.length, loading]);

  const isGroupCall = useMemo(() => {
    return participants.length > 2;
  }, [participants.length]);

  return {
    participants,
    otherParticipants,
    canStartCall,
    isGroupCall,
    loading,
    error
  };
};
