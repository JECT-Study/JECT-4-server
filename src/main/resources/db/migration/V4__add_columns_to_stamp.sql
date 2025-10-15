-- stamp 테이블 : end_date, total_missions, completed_missions 필드 추가
ALTER TABLE stamp
    ADD COLUMN end_date DATETIME(6) NULL,
    ADD COLUMN total_missions INT NOT NULL DEFAULT 0,
    ADD COLUMN completed_missions INT NOT NULL DEFAULT 0;