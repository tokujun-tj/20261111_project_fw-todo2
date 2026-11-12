# DB設計書

## 1. 文書の目的

やること（Todo）を保存するデータベース（データを保存しておく仕組み）の設計を定める。

この設計書は、作業フォルダ内で確認できた `requirements.md` をもとに作成した。
指定された `handout_app_spec.md` は作業フォルダ内に存在しなかったため、その内容は反映できていない。

## 2. DBの前提

| 項目 | 内容 |
|---|---|
| DB製品 | MySQL（データベース製品） |
| 使用テーブル | `todos` の1つだけ |
| 1行の意味 | やること1件 |
| 利用者 | 1人。ログイン情報や利用者番号は保存しない |
| 文字コード | `utf8mb4`（日本語など幅広い文字を保存できる文字コード） |
| 削除 | 削除した行を元に戻す機能は設けない |

## 3. テーブル一覧

| テーブル名 | 役割 |
|---|---|
| `todos` | やること、メモ、ジャンル、優先度、期限、完了状態、登録・更新日時を保存する |

## 4. `todos` テーブル定義

### 4.1 カラム一覧

| カラム | 型 | NULL（値なし） | 初期値 | 制約・役割 |
|---|---|---:|---|---|
| `id` | `BIGINT` | 不可 | 自動採番 | 主キー（行を重複なく識別する番号）。画面から入力しない |
| `title` | `VARCHAR(255)` | 不可 | なし | やること。255文字以内。空欄・半角スペースだけ・全角スペースだけは登録しない |
| `detail` | `VARCHAR(255)` | 可 | `NULL` | メモ。255文字以内 |
| `category` | `VARCHAR(255)` | 不可 | なし | ジャンル。`デザイン`、`マーケティング`、`プログラミング`、`資格`、`就職活動`のいずれか |
| `priority` | `INT` | 不可 | `2` | 優先度。`1`＝高、`2`＝中、`3`＝低 |
| `due_date` | `DATE` | 可 | `NULL` | 期限。未入力を許可する |
| `completed` | `BOOLEAN` | 不可 | `FALSE` | 完了状態。`FALSE`＝未完了、`TRUE`＝完了 |
| `created_at` | `DATETIME` | 不可 | 現在日時 | 登録日時。DB（データベース）が自動設定し、画面から入力しない |
| `updated_at` | `DATETIME` | 不可 | 現在日時 | 最終更新日時。登録時と更新時にDBが自動設定し、画面から入力しない |

### 4.2 主キーと入力値の制約

- `id` を主キーとする。主キーは、各行を一意（重複なし）に特定する。
- `title`、`category`、`priority`、`completed` は必須とする。
- `detail` と `due_date` は任意とし、未入力の場合は `NULL` とする。
- 画面での入力チェック（入力内容が条件を満たすか確認する処理）に加えて、DBの `CHECK` 制約（保存してよい値を限定するルール）でも値を制限する。
- `title` と `detail` の文字数、空白だけの入力の判定は画面側でも行う。DB側の `VARCHAR(255)` は255文字を超える値を保存しないための上限である。

## 5. CREATE TABLE 文（DDL）

DDL（テーブルを作成するSQL文）は次のとおりとする。MySQL 8.0.16以降では `CHECK` 制約が有効に機能する。

```sql
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
```

## 6. 検索・並び替えのための考慮

一覧画面では、次の条件で `todos` を検索・並び替えする。

| 画面の機能 | 使用カラム | SQLでの考え方 |
|---|---|---|
| やること名の部分一致検索 | `title` | `WHERE title LIKE '%検索語%'`。一部でも含む行を残す |
| ジャンルの絞り込み | `category` | `WHERE category = 選択したジャンル`。未指定なら条件を付けない |
| 期限が近い順 | `due_date` | `ORDER BY due_date IS NULL, due_date ASC`。期限ありが先、日付が近い順 |
| 期限が遠い順 | `due_date` | `ORDER BY due_date IS NULL, due_date DESC`。期限ありが先、日付が遠い順 |

検索語、ジャンル、並び順はURL（画面のアドレス）の後ろに付けて送信し、再読み込み後も条件を復元する。

例：

```text
/todos?keyword=課題&category=プログラミング&order=asc
```

### 6.1 インデックス（検索を速くするための目印）

大量のデータになった場合に備え、ジャンルと期限の絞り込み・並び替え用に次のインデックスを作成する。`title` の部分一致検索は、検索語が先頭から始まらない場合、通常のインデックスだけでは十分に速くならないため、まずは要件どおりの検索を優先する。

```sql
CREATE INDEX idx_todos_category_due_date
    ON todos (category, due_date);
```

このインデックスは `todos` 以外のテーブルを追加するものではない。

## 7. 要件とカラムの対応

| 要件 | 対応するカラム・設計 |
|---|---|
| やることを1件ずつ保存する | `todos` の1行を1件として保存 |
| やることの名前を保存する | `title` |
| メモを保存する | `detail` |
| 5種類のジャンルから選ぶ | `category` の `CHECK` 制約 |
| 優先度を高・中・低で保存する | `priority`。`1`＝高、`2`＝中、`3`＝低。初期値は `2` |
| 期限を保存する | `due_date`。未入力は `NULL` |
| 完了・未完了を保存する | `completed`。初期値は未完了 |
| 登録日時を保存する | `created_at`。DBが自動設定 |
| 最終更新日時を保存する | `updated_at`。DBが更新時に自動設定 |
| 一覧から編集・削除する | `id` で対象の行を指定 |
| 名前で部分一致検索する | `title` と `LIKE` |
| ジャンルで絞り込む | `category` と完全一致 |
| 期限が近い順・遠い順に並べる | `due_date` と `ORDER BY` |
| 完了日時を記録しない | 完了日時用のカラムを作成しない |
| ログイン・複数利用者共有を行わない | 利用者番号・ログイン情報用のカラムを作成しない |
| 写真・ファイルを添付しない | 添付ファイル用のカラムを作成しない |
| ページ分けを行わない | ページ番号・ページサイズ用のカラムを作成しない |

## 8. ログの扱い

要件では、登録・編集・削除が成功したときに、操作の種類と対象の `id` を1行ずつログ（動作の記録）に残すことが求められている。

アプリが使うDBテーブルは `todos` の1つだけという前提のため、操作ログ用のテーブルは作成しない。登録・編集・削除の成功時に、アプリケーションのログへ次のような情報を1行で出力する。

```text
操作=登録 id=123
操作=編集 id=123
操作=削除 id=123
```

`title` と `detail` はログに記録しない。

## 9. この設計の対象外

- 画面の配置、画面遷移、入力欄の見た目は画面設計書で定める。
- ログイン、利用者管理、複数利用者間の共有は行わない。
- 削除データの復元機能は行わない。
- 完了日時、添付ファイル、写真、ページ分け用のデータは保存しない。

