ALTER TABLE app_settings RENAME TO app_settings_old;

CREATE TABLE app_settings (
    id BIGINT NOT NULL PRIMARY KEY,
    base_url VARCHAR(512),
    api_key VARCHAR(2048),
    model VARCHAR(256),
    max_retry_passes INTEGER,
    python_executable VARCHAR(256)
);

INSERT INTO app_settings (
    id,
    base_url,
    api_key,
    model,
    max_retry_passes,
    python_executable
)
SELECT
    id,
    base_url,
    api_key,
    model,
    max_retry_passes,
    python_executable
FROM app_settings_old;

DROP TABLE app_settings_old;
