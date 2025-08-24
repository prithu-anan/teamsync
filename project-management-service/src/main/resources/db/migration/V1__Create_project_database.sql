-- ============== SIMPLIFIED SQL SCHEMA ==============

-- Projects table
CREATE TABLE Projects (
    id BIGSERIAL PRIMARY KEY,       -- auto-incrementing PK
    title VARCHAR(255) NOT NULL,
    description TEXT,
    created_by BIGINT NOT NULL,     -- external user ID
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ProjectMembers table (junction table for Users-Projects many-to-many)
CREATE TABLE ProjectMembers (
    project_id BIGINT NOT NULL REFERENCES Projects(id) ON DELETE CASCADE,  -- FK to Projects.id
    user_id BIGINT NOT NULL,       -- external user ID
    role VARCHAR(50) NOT NULL CHECK (role IN ('owner', 'admin', 'member', 'guest', 'viewer')),
    joined_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (project_id, user_id)
);

-- Add constraint to ensure valid role values
ALTER TABLE ProjectMembers 
ADD CONSTRAINT check_project_role 
CHECK (role IN ('owner', 'admin', 'member', 'guest', 'viewer'));