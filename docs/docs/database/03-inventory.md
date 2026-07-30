# Inventory Table Design

## Overview

The **Inventory** table stores stock information for each product.

It is responsible for tracking the current stock quantity, minimum stock level, and inventory updates.

Each product has one inventory record.

---

# Table Information

| Property | Value |
|----------|-------|
| Table Name | inventory |
| Purpose | Store stock information for products |
| Primary Key | id |

---

# Columns

| Column | Data Type | Nullable | Description |
|---------|-----------|----------|-------------|
| id | BIGINT | No | Primary Key (Auto Increment) |
| product_id | BIGINT | No | Reference to the product |
| quantity | INTEGER | No | Current stock quantity |
| minimum_quantity | INTEGER | No | Minimum quantity before low stock alert |
| created_at | TIMESTAMP | No | Creation date |
| updated_at | TIMESTAMP | No | Last modification date |

---

# Constraints

## Primary Key

- `id`

## Foreign Keys

- `product_id` → `products.id`

## Unique Constraints

- `product_id`

Each product can have only one inventory record.

## Not Null

- `id`
- `product_id`
- `quantity`
- `minimum_quantity`
- `created_at`
- `updated_at`

## Check Constraints

- `quantity >= 0`
- `minimum_quantity >= 0`

---

# Indexes

| Index | Columns |
|--------|---------|
| Primary Key | `id` |
| Unique Index | `product_id` |

---

# Relationships

## One-to-One

Each product has exactly one inventory record.

```text
products (1)
      │
      │
      ▼
inventory (1)
```

Foreign Key:

```text
product_id → products.id
```

---

# Business Rules

- Every product must have exactly one inventory record.
- Stock quantity cannot be negative.
- Minimum quantity cannot be negative.
- A low stock alert is triggered when:

```text
quantity <= minimum_quantity
```

- Inventory quantity is automatically updated after:
    - Purchase
    - Sale
    - Stock Adjustment
    - Inventory Correction

---

# Future Considerations

The following features may be added in future versions:

- Multiple warehouses
- Reserved quantity
- Available quantity
- Damaged quantity
- Expired quantity
- Inventory valuation
- Batch/Lot tracking
- Serial numbers

These features are **not** part of the MVP.

---

# Example Data

| id | product_id | quantity | minimum_quantity |
|----|------------|----------|------------------|
| 1 | 1 | 120 | 15 |
| 2 | 2 | 45 | 10 |
| 3 | 3 | 8 | 5 |

---

# Notes

- Table names use **snake_case**.
- Column names follow the **snake_case** naming convention.
- English is used for all database objects.
- Inventory data is separated from product information.
- This table is part of the SmartStore MVP database schema.