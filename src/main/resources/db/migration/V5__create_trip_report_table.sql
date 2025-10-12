CREATE TABLE trip_report (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- 도메인 필드
    title VARCHAR(255) NOT NULL,
    content VARCHAR(255) NOT NULL,
    start_date VARCHAR(255) NOT NULL,
    end_date VARCHAR(255) NULL,
    completed_mission_count BIGINT NOT NULL,
    total_focus_hours BIGINT NOT NULL,
    study_days BIGINT NOT NULL,
    image_title VARCHAR(255) NOT NULL,
    image_url VARCHAR(255) NULL,

    -- 외래키 필드
    member_id BIGINT NOT NULL,

    -- Auditing
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6) NULL,

    -- 외래키 제약조건
    FOREIGN KEY (member_id) REFERENCES member (id)
    ON UPDATE RESTRICT
    ON DELETE RESTRICT
);
