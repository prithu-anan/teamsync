-- Tasks table
CREATE TABLE Tasks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL 
        CONSTRAINT valid_task_status CHECK (status IN ('backlog', 'todo', 'in_progress', 'in_review', 'blocked', 'completed')),
    deadline TIMESTAMP WITH TIME ZONE,
    priority VARCHAR(10) 
        CONSTRAINT valid_task_priority CHECK (priority IN ('low', 'medium', 'high', 'urgent')),
    time_estimate VARCHAR(50),
    ai_time_estimate VARCHAR(50),
    ai_priority VARCHAR(10) 
        CONSTRAINT valid_ai_priority CHECK (ai_priority IN ('low', 'medium', 'high', 'urgent')),
    smart_deadline TIMESTAMP WITH TIME ZONE,
    project_id BIGINT NOT NULL,
    assigned_to BIGINT,
    assigned_by BIGINT,
    assigned_at TIMESTAMP WITH TIME ZONE,
    parent_task_id BIGINT REFERENCES Tasks(id) ON DELETE SET NULL,
    attachments TEXT[],
    tentative_starting_date DATE
);

-- TaskStatusHistory table
CREATE TABLE TaskStatusHistory (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES Tasks(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL 
        CONSTRAINT valid_history_status CHECK (status IN ('backlog', 'todo', 'in_progress', 'in_review', 'blocked', 'completed')),
    changed_by BIGINT NOT NULL,
    changed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    comment TEXT
);