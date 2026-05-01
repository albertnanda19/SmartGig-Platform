CREATE TABLE IF NOT EXISTS skill_relations (
    id BIGSERIAL PRIMARY KEY,
    source_skill_id BIGINT NOT NULL REFERENCES skills(id),
    target_skill_id BIGINT NOT NULL REFERENCES skills(id),
    relation_type VARCHAR(30) NOT NULL,
    weight DECIMAL(3,2) NOT NULL DEFAULT 0.5,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(source_skill_id, target_skill_id),
    CHECK (source_skill_id != target_skill_id),
    CHECK (weight >= 0 AND weight <= 1)
);

CREATE INDEX idx_skill_relations_source ON skill_relations(source_skill_id);
CREATE INDEX idx_skill_relations_target ON skill_relations(target_skill_id);

INSERT INTO skill_relations (source_skill_id, target_skill_id, relation_type, weight)
SELECT s1.id, s2.id, 'REQUIRES', 0.90 FROM skills s1, skills s2 WHERE s1.slug='spring-boot' AND s2.slug='java'
ON CONFLICT (source_skill_id, target_skill_id) DO NOTHING;

INSERT INTO skill_relations (source_skill_id, target_skill_id, relation_type, weight)
SELECT s1.id, s2.id, 'ENABLES', 0.80 FROM skills s1, skills s2 WHERE s1.slug='spring-boot' AND s2.slug='microservices'
ON CONFLICT (source_skill_id, target_skill_id) DO NOTHING;

INSERT INTO skill_relations (source_skill_id, target_skill_id, relation_type, weight)
SELECT s1.id, s2.id, 'ENABLES', 0.90 FROM skills s1, skills s2 WHERE s1.slug='spring-boot' AND s2.slug='rest-api'
ON CONFLICT (source_skill_id, target_skill_id) DO NOTHING;

INSERT INTO skill_relations (source_skill_id, target_skill_id, relation_type, weight)
SELECT s1.id, s2.id, 'REQUIRES', 0.85 FROM skills s1, skills s2 WHERE s1.slug='microservices' AND s2.slug='docker'
ON CONFLICT (source_skill_id, target_skill_id) DO NOTHING;

INSERT INTO skill_relations (source_skill_id, target_skill_id, relation_type, weight)
SELECT s1.id, s2.id, 'COMPLEMENTS', 0.75 FROM skills s1, skills s2 WHERE s1.slug='microservices' AND s2.slug='apache-kafka'
ON CONFLICT (source_skill_id, target_skill_id) DO NOTHING;

INSERT INTO skill_relations (source_skill_id, target_skill_id, relation_type, weight)
SELECT s1.id, s2.id, 'LEADS_TO', 0.70 FROM skills s1, skills s2 WHERE s1.slug='docker' AND s2.slug='kubernetes'
ON CONFLICT (source_skill_id, target_skill_id) DO NOTHING;

INSERT INTO skill_relations (source_skill_id, target_skill_id, relation_type, weight)
SELECT s1.id, s2.id, 'COMPLEMENTS', 0.70 FROM skills s1, skills s2 WHERE s1.slug='postgresql' AND s2.slug='redis'
ON CONFLICT (source_skill_id, target_skill_id) DO NOTHING;

INSERT INTO skill_relations (source_skill_id, target_skill_id, relation_type, weight)
SELECT s1.id, s2.id, 'COMPLEMENTS', 0.80 FROM skills s1, skills s2 WHERE s1.slug='microservices' AND s2.slug='postgresql'
ON CONFLICT (source_skill_id, target_skill_id) DO NOTHING;

INSERT INTO skill_relations (source_skill_id, target_skill_id, relation_type, weight)
SELECT s1.id, s2.id, 'REQUIRES', 0.85 FROM skills s1, skills s2 WHERE s1.slug='react' AND s2.slug='typescript'
ON CONFLICT (source_skill_id, target_skill_id) DO NOTHING;

INSERT INTO skill_relations (source_skill_id, target_skill_id, relation_type, weight)
SELECT s1.id, s2.id, 'ENABLES', 0.65 FROM skills s1, skills s2 WHERE s1.slug='rest-api' AND s2.slug='graphql'
ON CONFLICT (source_skill_id, target_skill_id) DO NOTHING;

INSERT INTO skill_relations (source_skill_id, target_skill_id, relation_type, weight)
SELECT s1.id, s2.id, 'ALTERNATIVE_TO', 0.40 FROM skills s1, skills s2 WHERE s1.slug='graphql' AND s2.slug='rest-api'
ON CONFLICT (source_skill_id, target_skill_id) DO NOTHING;

INSERT INTO skill_relations (source_skill_id, target_skill_id, relation_type, weight)
SELECT s1.id, s2.id, 'ENABLES', 0.70 FROM skills s1, skills s2 WHERE s1.slug='apache-kafka' AND s2.slug='microservices'
ON CONFLICT (source_skill_id, target_skill_id) DO NOTHING;

INSERT INTO skill_relations (source_skill_id, target_skill_id, relation_type, weight)
SELECT s1.id, s2.id, 'REQUIRES', 0.80 FROM skills s1, skills s2 WHERE s1.slug='ci-cd' AND s2.slug='docker'
ON CONFLICT (source_skill_id, target_skill_id) DO NOTHING;

INSERT INTO skill_relations (source_skill_id, target_skill_id, relation_type, weight)
SELECT s1.id, s2.id, 'ENABLES', 0.75 FROM skills s1, skills s2 WHERE s1.slug='ci-cd' AND s2.slug='kubernetes'
ON CONFLICT (source_skill_id, target_skill_id) DO NOTHING;

INSERT INTO skill_relations (source_skill_id, target_skill_id, relation_type, weight)
SELECT s1.id, s2.id, 'REQUIRES', 0.70 FROM skills s1, skills s2 WHERE s1.slug='machine-learning' AND s2.slug='python'
ON CONFLICT (source_skill_id, target_skill_id) DO NOTHING;

INSERT INTO skill_relations (source_skill_id, target_skill_id, relation_type, weight)
SELECT s1.id, s2.id, 'ENABLES', 0.60 FROM skills s1, skills s2 WHERE s1.slug='python' AND s2.slug='rest-api'
ON CONFLICT (source_skill_id, target_skill_id) DO NOTHING;

INSERT INTO skill_relations (source_skill_id, target_skill_id, relation_type, weight)
SELECT s1.id, s2.id, 'COMPLEMENTS', 0.65 FROM skills s1, skills s2 WHERE s1.slug='apache-kafka' AND s2.slug='postgresql'
ON CONFLICT (source_skill_id, target_skill_id) DO NOTHING;

INSERT INTO skill_relations (source_skill_id, target_skill_id, relation_type, weight)
SELECT s1.id, s2.id, 'COMPLEMENTS', 0.60 FROM skills s1, skills s2 WHERE s1.slug='spring-boot' AND s2.slug='postgresql'
ON CONFLICT (source_skill_id, target_skill_id) DO NOTHING;

INSERT INTO skill_relations (source_skill_id, target_skill_id, relation_type, weight)
SELECT s1.id, s2.id, 'COMPLEMENTS', 0.60 FROM skills s1, skills s2 WHERE s1.slug='spring-boot' AND s2.slug='redis'
ON CONFLICT (source_skill_id, target_skill_id) DO NOTHING;

INSERT INTO skill_relations (source_skill_id, target_skill_id, relation_type, weight)
SELECT s1.id, s2.id, 'ENABLES', 0.70 FROM skills s1, skills s2 WHERE s1.slug='java' AND s2.slug='rest-api'
ON CONFLICT (source_skill_id, target_skill_id) DO NOTHING;

INSERT INTO skill_relations (source_skill_id, target_skill_id, relation_type, weight)
SELECT s1.id, s2.id, 'ENABLES', 0.65 FROM skills s1, skills s2 WHERE s1.slug='kubernetes' AND s2.slug='microservices'
ON CONFLICT (source_skill_id, target_skill_id) DO NOTHING;

