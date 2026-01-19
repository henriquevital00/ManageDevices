-- sql
-- Flyway migration to create the DeviceHistory table
CREATE SCHEMA IF NOT EXISTS manage_devices;
SET search_path TO manage_devices;

CREATE TABLE IF NOT EXISTS device_history (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    device_id       UUID NOT NULL,
    name            VARCHAR(255) NOT NULL,
    brand           VARCHAR(255) NOT NULL,
    state           VARCHAR(64)  NOT NULL,
    creation_time       TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    operation_type  VARCHAR(64)  NOT NULL,
    CONSTRAINT device_history_name_not_blank       CHECK (btrim(name) <> ''),
    CONSTRAINT device_history_brand_not_blank      CHECK (btrim(brand) <> ''),
    CONSTRAINT device_history_state_not_blank      CHECK (btrim(state) <> ''),
    CONSTRAINT device_history_operation_not_blank  CHECK (btrim(operation_type) <> '')
);

CREATE INDEX IF NOT EXISTS idx_device_history_device_id ON device_history (device_id);
