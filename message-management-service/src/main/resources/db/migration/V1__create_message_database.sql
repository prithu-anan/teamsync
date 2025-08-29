-- Channels table
CREATE TABLE Channels (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('direct', 'group')),
    project_id BIGINT, -- Removed REFERENCES, kept the column
    members BIGINT[] -- Could be normalized into a ChannelMembers junction table
);

-- Messages table
CREATE TABLE Messages (
    id BIGSERIAL PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    channel_id BIGINT REFERENCES Channels(id) ON DELETE SET NULL,
    recipient_id BIGINT,
    content TEXT NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    thread_parent_id BIGINT REFERENCES Messages(id) ON DELETE SET NULL,
    CHECK (channel_id IS NOT NULL OR recipient_id IS NOT NULL) -- Ensure at least one is set
);

-- Reactions table
CREATE TABLE Reactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    reaction_type VARCHAR(20) NOT NULL CHECK (reaction_type IN ('like', 'love', 'haha', 'wow', 'sad', 'angry', 'celebrate', 'support', 'insightful')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    message_id BIGINT REFERENCES Messages(id) ON DELETE CASCADE
);

ALTER TABLE messages
ADD COLUMN file_url TEXT,
ADD COLUMN file_type VARCHAR(255);
