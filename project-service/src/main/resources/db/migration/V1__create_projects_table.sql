CREATE TABLE IF NOT EXISTS projects (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    slug VARCHAR(200) UNIQUE NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    budget_min DECIMAL(15,2) NOT NULL,
    budget_max DECIMAL(15,2) NOT NULL,
    budget_type VARCHAR(20) NOT NULL DEFAULT 'FIXED',
    deadline TIMESTAMP,
    estimated_duration_days INT,
    complexity_level VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    max_applicants INT NOT NULL DEFAULT 10,
    current_applicant_count INT NOT NULL DEFAULT 0,
    selected_freelancer_id BIGINT,
    views_count INT NOT NULL DEFAULT 0,
    is_featured BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    cancellation_reason TEXT
);

CREATE INDEX idx_projects_client_id ON projects(client_id);
CREATE INDEX idx_projects_status ON projects(status);
CREATE INDEX idx_projects_category ON projects(category);
CREATE INDEX idx_projects_created_at ON projects(created_at DESC);
CREATE INDEX idx_projects_budget_min ON projects(budget_min);

