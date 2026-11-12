CREATE TABLE todos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    detail VARCHAR(255) NULL,
    category VARCHAR(255) NOT NULL,
    priority INT NOT NULL DEFAULT 2,
    due_date DATE NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT chk_todos_category
        CHECK (
            category COLLATE utf8mb4_unicode_ci IN (
                CONVERT(0xE38387E382B6E382A4E383B3 USING utf8mb4) COLLATE utf8mb4_unicode_ci,
                CONVERT(0xE3839EE383BCE382B1E38386E382A3E383B3E382B0 USING utf8mb4) COLLATE utf8mb4_unicode_ci,
                CONVERT(0xE38397E383ADE382B0E383A9E3839FE383B3E382B0 USING utf8mb4) COLLATE utf8mb4_unicode_ci,
                CONVERT(0xE8B387E6A0BC USING utf8mb4) COLLATE utf8mb4_unicode_ci,
                CONVERT(0xE5B0B1E881B7E6B4BBE58B95 USING utf8mb4) COLLATE utf8mb4_unicode_ci
            )
        ),
    CONSTRAINT chk_todos_priority
        CHECK (priority IN (1, 2, 3))
) ENGINE=InnoDB
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
