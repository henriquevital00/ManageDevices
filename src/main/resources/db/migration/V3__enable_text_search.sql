-- 1. Habilita a extensão de trigramas (nativa do Postgres)
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- 2. Cria um índice GIN (Generalized Inverted Index) na coluna 'name'
-- O operador 'gin_trgm_ops' otimiza o LIKE '%...%'
CREATE INDEX idx_device_name_trgm ON device USING GIN (name gin_trgm_ops);

