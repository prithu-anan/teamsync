-- FeedPosts table
CREATE TABLE FeedPosts (
    id BIGSERIAL PRIMARY KEY,
    type TEXT NOT NULL CHECK (type IN ('text', 'photo', 'event', 'appreciation', 'poll', 'birthday', 'highlight')),
    author_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    media_urls TEXT[],
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    event_date DATE,
    poll_options TEXT[],
    is_ai_generated BOOLEAN NOT NULL DEFAULT FALSE,
    ai_summary TEXT
);

-- Events table
CREATE TABLE Events (
    id BIGSERIAL PRIMARY KEY,
    parent_post_id BIGINT REFERENCES FeedPosts(id) ON DELETE SET NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    type TEXT NOT NULL CHECK (type IN ('Birthday', 'Workiversary', 'Outing')),
    date DATE NOT NULL,
    participants BIGINT[]
);

-- Appreciations table
CREATE TABLE Appreciations (
    id BIGSERIAL PRIMARY KEY,
    parent_post_id BIGINT REFERENCES FeedPosts(id) ON DELETE SET NULL,
    from_user_id BIGINT NOT NULL,
    to_user_id BIGINT NOT NULL,
    message TEXT NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Comments table
CREATE TABLE Comments (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL REFERENCES FeedPosts(id) ON DELETE CASCADE,
    author_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    parent_comment_id BIGINT REFERENCES Comments(id) ON DELETE SET NULL,
    reply_count BIGINT NOT NULL DEFAULT 0
);

-- Reactions table
CREATE TABLE Reactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    reaction_type TEXT NOT NULL CHECK (reaction_type IN ('like', 'love', 'haha', 'wow', 'sad', 'angry', 'celebrate', 'support', 'insightful')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    post_id BIGINT REFERENCES FeedPosts(id) ON DELETE CASCADE,
    comment_id BIGINT REFERENCES Comments(id) ON DELETE CASCADE,
    CHECK ((post_id IS NOT NULL AND comment_id IS NULL) OR (post_id IS NULL AND comment_id IS NOT NULL))
);

-- PollVotes table
CREATE TABLE PollVotes (
    id BIGSERIAL PRIMARY KEY,
    poll_id BIGINT NOT NULL REFERENCES FeedPosts(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL,
    selected_option TEXT NOT NULL
);

ALTER TABLE Events
ADD COLUMN tentative_starting_date DATE;
