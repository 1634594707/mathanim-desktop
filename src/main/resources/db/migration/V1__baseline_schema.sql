CREATE TABLE IF NOT EXISTS app_settings (
    id BIGINT NOT NULL PRIMARY KEY,
    base_url VARCHAR(512),
    api_key VARCHAR(2048),
    model VARCHAR(256),
    max_retry_passes INTEGER,
    python_executable VARCHAR(256)
);

CREATE TABLE IF NOT EXISTS render_jobs (
    id TEXT NOT NULL PRIMARY KEY,
    concept VARCHAR(8192) NOT NULL,
    status VARCHAR(32) NOT NULL,
    output_media_path VARCHAR(2048),
    error_message VARCHAR(4096),
    failure_summary VARCHAR(512),
    failure_repair_hint VARCHAR(2048),
    fallback_mode_active BOOLEAN,
    force_fallback_mode BOOLEAN,
    processing_stage TEXT,
    output_mode VARCHAR(16),
    video_quality VARCHAR(16),
    job_kind VARCHAR(24),
    source_code TEXT,
    edit_instructions VARCHAR(8192),
    script_path VARCHAR(2048),
    use_problem_framing BOOLEAN,
    use_two_stage_ai BOOLEAN,
    reliable_generation BOOLEAN,
    allow_fallback_mode BOOLEAN,
    favorited BOOLEAN,
    problem_plan_json TEXT,
    reference_images_json TEXT,
    prompt_locale VARCHAR(16),
    prompt_overrides_json TEXT,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_render_jobs_created_at ON render_jobs(created_at);
CREATE INDEX IF NOT EXISTS idx_render_jobs_status ON render_jobs(status);
CREATE INDEX IF NOT EXISTS idx_render_jobs_favorited ON render_jobs(favorited);
