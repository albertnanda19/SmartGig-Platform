CREATE TABLE IF NOT EXISTS project_applications (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id),
    freelancer_id BIGINT NOT NULL,
    freelancer_username VARCHAR(50) NOT NULL,
    cover_letter TEXT NOT NULL,
    proposed_budget DECIMAL(15,2) NOT NULL,
    estimated_duration_days INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    matching_score DECIMAL(5,4),
    client_notes TEXT,
    applied_at TIMESTAMP NOT NULL DEFAULT NOW(),
    reviewed_at TIMESTAMP,
    UNIQUE(project_id, freelancer_id)
);

CREATE INDEX idx_pa_project_id ON project_applications(project_id);
CREATE INDEX idx_pa_freelancer_id ON project_applications(freelancer_id);
CREATE INDEX idx_pa_status ON project_applications(status);

