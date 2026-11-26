-- 既存のDBボリュームにも「その他」を追加するためのマイグレーション
ALTER TABLE todos DROP CHECK chk_todos_category;

ALTER TABLE todos
    ADD CONSTRAINT chk_todos_category
    CHECK (
        category COLLATE utf8mb4_unicode_ci IN (
            CONVERT(0xE38387E382B6E382A4E383B3 USING utf8mb4) COLLATE utf8mb4_unicode_ci,
            CONVERT(0xE3839EE383BCE382B1E38386E382A3E383B3E382B0 USING utf8mb4) COLLATE utf8mb4_unicode_ci,
            CONVERT(0xE38397E383ADE382B0E383A9E3839FE383B3E382B0 USING utf8mb4) COLLATE utf8mb4_unicode_ci,
            CONVERT(0xE8B387E6A0BC USING utf8mb4) COLLATE utf8mb4_unicode_ci,
            CONVERT(0xE5B0B1E881B7E6B4BBE58B95 USING utf8mb4) COLLATE utf8mb4_unicode_ci,
            CONVERT(0xE3819DE381AEE4BB96 USING utf8mb4) COLLATE utf8mb4_unicode_ci
        )
    );
