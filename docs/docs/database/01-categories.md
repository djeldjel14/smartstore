# Categories Table Design

## Overview

The **Categories** table stores product categories used to organize products inside SmartStore.

Each product belongs to one category, while one category can contain many products.

Examples:

- Beverages
- Dairy
- Snacks
- Cleaning Products
- Pasta
- Rice
- Canned Food

---

# Table Information

| Property | Value |
|----------|-------|
| Table Name | categories |
| Purpose | Store product categories |
| Primary Key | id |

---

# Columns

| Column | Data Type | Nullable | Description |
|---------|-----------|----------|-------------|
| id | BIGINT | No | Primary Key (Auto Increment) |
| name | VARCHAR(100) | No | Category name |
| description | VARCHAR(255) | Yes | Optional description |
| created_at | TIMESTAMP | No | Creation date |
| updated_at | TIMESTAMP | No | Last modification date |

---

# Constraints

## Primary Key

- `id`

## Unique Constraints

- `name`

## Not Null

- `id`
- `name`
- `created_at`
- `updated_at`

---

# Indexes

| Index | Columns |
|--------|---------|
| Primary Key | `id` |
| Unique Index | `name` |

---

# Relationships

## One-to-Many

One category can contain many products.

```text
categories (1)
      │
      │
      ▼
products (N)
```

Foreign Key (Products Table):

```text
category_id → categories.id
```

---

# Business Rules

- Every category must have a unique name.
- Category names cannot be empty.
- Description is optional.
- Categories should not be duplicated.

---

# Future Considerations

The following features may be added in future versions:

- Category image
- Category icon
- Display order
- Parent category (Nested Categories)
- Soft Delete

These features are **not** part of the MVP.

---

# Example Data

| id | name | description |
|----|------|-------------|
| 1 | Beverages | Drinks and juices |
| 2 | Dairy | Milk, cheese and yogurt |
| 3 | Snacks | Chips and biscuits |

---

# Notes

- Table names use **snake_case**.
- Column names follow the **snake_case** naming convention.
- English is used for all database objects.
- This table is part of the SmartStore MVP database schema.