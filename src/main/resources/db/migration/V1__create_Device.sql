-- sql
-- Flyway migration to create the Device table
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE SCHEMA IF NOT EXISTS manage_devices;
SET search_path TO manage_devices;

CREATE TABLE IF NOT EXISTS device (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name            VARCHAR(255) NOT NULL,
    brand           VARCHAR(255) NOT NULL,
    state           VARCHAR(64)  NOT NULL,
    creation_time   TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT device_name_not_blank  CHECK (btrim(name)  <> ''),
    CONSTRAINT device_brand_not_blank CHECK (btrim(brand) <> ''),
    CONSTRAINT device_state_not_blank CHECK (btrim(state) <> '')
);

CREATE INDEX IF NOT EXISTS idx_device_state          ON device (state);
CREATE INDEX IF NOT EXISTS idx_device_creation_time  ON device (creation_time);
