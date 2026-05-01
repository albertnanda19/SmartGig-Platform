CREATE TABLE IF NOT EXISTS project_skill_requirements (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    skill_id BIGINT NOT NULL,
    skill_name VARCHAR(100) NOT NULL,
    is_required BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(project_id, skill_id)
);

CREATE INDEX idx_psr_project_id ON project_skill_requirements(project_id);
CREATE INDEX idx_psr_skill_id ON project_skill_requirements(skill_id);

