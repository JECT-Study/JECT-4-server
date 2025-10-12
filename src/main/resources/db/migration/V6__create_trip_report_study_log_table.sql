CREATE TABLE trip_report_study_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- 외래키 필드
    trip_report_id BIGINT NOT NULL,
    study_log_id BIGINT NOT NULL,

    -- Auditing
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6) NULL,

    -- 외래키 제약조건
    FOREIGN KEY (trip_report_id) REFERENCES trip_report (id)
    ON UPDATE RESTRICT
    ON DELETE RESTRICT,

    FOREIGN KEY (study_log_id) REFERENCES study_log (id)
    ON UPDATE RESTRICT
    ON DELETE RESTRICT
);
