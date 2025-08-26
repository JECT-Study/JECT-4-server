CREATE TABLE member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- 도메인 필드
    social_provider ENUM('KAKAO','GOOGLE') NOT NULL,
    social_id VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    nickname VARCHAR(50) NOT NULL,
    profile_image VARCHAR(512) NULL,
    category ENUM('STUDENT','WORKER','FREELANCER','JOBSEEKER') NOT NULL,
    role ENUM('ROLE_USER','ROLE_ADMIN','ROLE_OWNER') NOT NULL,

    -- Auditing
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6) NULL
);

CREATE TABLE trip (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- 도메인 필드
    name VARCHAR(255) NOT NULL,
    memo VARCHAR(255) NULL,
    start_date DATE NOT NULL,
    end_date DATE NULL,
    total_stamps INT NULL,
    completed_stamps INT NULL,
    completed BOOLEAN NOT NULL,
    category ENUM('COURSE', 'EXPLORE') NOT NULL,

    -- 외래키 필드
    member_id BIGINT NOT NULL,

    -- Auditing
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6) NULL,

    -- 외래키 제약조건: trip.member_id → member.id
    FOREIGN KEY (member_id) REFERENCES member (id)
    ON UPDATE RESTRICT
    ON DELETE RESTRICT
);

CREATE TABLE stamp (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- 도메인 필드
    name VARCHAR(255) NOT NULL,
    stamp_order INT NULL,
    completed BOOLEAN NOT NULL,

    -- 외래키 필드
    trip_id BIGINT NOT NULL,

    -- Auditing
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6) NULL,

    -- 외래키 제약조건: stamp.trip_id → trip.id
    FOREIGN KEY (trip_id) REFERENCES trip (id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT
);

CREATE TABLE mission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- 도메인 필드
    name VARCHAR(255) NOT NULL,
    completed BOOLEAN NOT NULL,

    -- 외래키 필드
    stamp_id BIGINT NOT NULL,

    -- Auditing
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6) NULL,

    -- 외래키 제약조건: mission.stamp_id → stamp.id
    FOREIGN KEY (stamp_id) REFERENCES stamp (id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT
);

CREATE TABLE daily_goal (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- 도메인 필드
    title VARCHAR(255) NOT NULL,
    completed BOOLEAN NOT NULL,

    -- 외래키 필드
    trip_id BIGINT NOT NULL,

    -- Auditing
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6) NULL,

    -- 외래키 제약조건: stamp.trip_id → trip.id
    FOREIGN KEY (trip_id) REFERENCES trip (id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT
);

CREATE TABLE study_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- 도메인 필드
    title VARCHAR(255) NOT NULL,
    content VARCHAR(255) NOT NULL,

    -- 외래키 필드
    member_id BIGINT NOT NULL,
    daily_goal_id BIGINT NOT NULL,

    -- Auditing
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6) NULL,

    -- 외래키 제약조건
    FOREIGN KEY (member_id) REFERENCES member (id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT,

    FOREIGN KEY (daily_goal_id) REFERENCES daily_goal (id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT
);

CREATE TABLE daily_mission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- 외래키 필드
    mission_id BIGINT NOT NULL,
    daily_goal_id BIGINT NOT NULL,

    -- Auditing
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6) NULL,

    -- 외래키 제약조건
    FOREIGN KEY (mission_id) REFERENCES mission (id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT,

    FOREIGN KEY (daily_goal_id) REFERENCES daily_goal (id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT
);

CREATE TABLE study_log_daily_mission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- 외래키 필드
    study_log_id BIGINT NOT NULL,
    daily_mission_id BIGINT NOT NULL,

    -- Auditing
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6) NULL,

    -- 외래키 제약조건
    FOREIGN KEY (study_log_id) REFERENCES study_log (id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT,

    FOREIGN KEY (daily_mission_id) REFERENCES daily_mission (id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT
);

CREATE TABLE pomodoro (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- 도메인 필드
    focus_duration_in_seconds INT NULL,
    focus_session_count INT NULL,
    break_duration_in_seconds INT NULL,
    total_focus_time_in_seconds INT NULL,

    -- 외래키 필드
    daily_goal_id BIGINT NOT NULL,

    -- Auditing
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6) NULL,

    -- 외래키 제약조건
    FOREIGN KEY (daily_goal_id) REFERENCES daily_goal (id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT
);
