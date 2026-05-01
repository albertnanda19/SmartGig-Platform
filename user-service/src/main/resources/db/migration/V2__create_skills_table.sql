CREATE TABLE IF NOT EXISTS skills (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    category VARCHAR(50) NOT NULL,
    description TEXT,
    icon_url VARCHAR(500),
    usage_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_skills_category ON skills(category);
CREATE INDEX idx_skills_slug ON skills(slug);

INSERT INTO skills (name, slug, category, description, usage_count) VALUES
('Java', 'java', 'BACKEND', 'Java programming language', 0),
('Spring Boot', 'spring-boot', 'BACKEND', 'Java Spring Boot framework', 0),
('Microservices', 'microservices', 'ARCHITECTURE', 'Microservices architecture pattern', 0),
('Apache Kafka', 'apache-kafka', 'MESSAGING', 'Distributed event streaming', 0),
('PostgreSQL', 'postgresql', 'DATABASE', 'Relational database', 0),
('Redis', 'redis', 'DATABASE', 'In-memory data store', 0),
('Docker', 'docker', 'DEVOPS', 'Container platform', 0),
('Kubernetes', 'kubernetes', 'DEVOPS', 'Container orchestration', 0),
('React', 'react', 'FRONTEND', 'JavaScript UI library', 0),
('TypeScript', 'typescript', 'FRONTEND', 'Typed JavaScript', 0),
('Python', 'python', 'BACKEND', 'Python programming language', 0),
('Machine Learning', 'machine-learning', 'AI_ML', 'Machine learning concepts', 0),
('REST API', 'rest-api', 'BACKEND', 'RESTful API design', 0),
('GraphQL', 'graphql', 'BACKEND', 'Query language for APIs', 0),
('CI/CD', 'ci-cd', 'DEVOPS', 'Continuous Integration/Deployment', 0)
ON CONFLICT (slug) DO NOTHING;

