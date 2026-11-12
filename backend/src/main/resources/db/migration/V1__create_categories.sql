CREATE TABLE categories (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100)
        CHARACTER SET utf8mb4
        COLLATE utf8mb4_0900_ai_ci
        NOT NULL,
    description VARCHAR(255)
        CHARACTER SET utf8mb4
        COLLATE utf8mb4_0900_ai_ci
        NULL,
    parent_id BIGINT NULL,

    -- العمود المشتق لحل مشكلة Uniqueness مع القيمة NULL
    parent_id_unique BIGINT
        GENERATED ALWAYS AS (COALESCE(parent_id, 0)) VIRTUAL,

    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,

    CONSTRAINT pk_categories PRIMARY KEY (id),
    CONSTRAINT uk_categories_parent_name
        UNIQUE (parent_id_unique, name),

    INDEX idx_categories_parent_id (parent_id),

    CONSTRAINT chk_categories_parent_positive
        CHECK (parent_id IS NULL OR parent_id > 0),

    CONSTRAINT fk_categories_parent
        FOREIGN KEY (parent_id)
        REFERENCES categories (id)
        ON DELETE CASCADE
        ON UPDATE RESTRICT
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;