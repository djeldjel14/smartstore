# Sale Items Table Design

## Overview

The **Sale Items** table stores the individual products included in each sales invoice.

Each record represents one product within a sale.

A single sale can contain multiple sale items, while each sale item belongs to exactly one sale.

---

# Table Information

| Property | Value |
|----------|-------|
| Table Name | sale_items |
| Purpose | Store products sold in each invoice |
| Primary Key | id |

---

# Columns

| Column | Data Type | Nullable | Description |
|---------|-----------|----------|-------------|
| id | BIGINT | No | Primary Key (Auto Increment) |
| sale_id | BIGINT | No | Reference to the sale |
| product_id | BIGINT | No | Reference to the product |
| quantity | INTEGER | No | Quantity sold |
| unit_price | DECIMAL(10,2) | No | Selling price per unit at the time of sale |
| line_total | DECIMAL(10,2) | No | Total amount for this line |
| created_at | TIMESTAMP | No | Creation date |
| updated_at | TIMESTAMP | No | Last modification date |

---

# Constraints

## Primary Key

- `id`

## Foreign Keys

- `sale_id` → `sales.id`
- `product_id` → `products.id`

## Not Null

- `id`
- `sale_id`
- `product_id`
- `quantity`
- `unit_price`
- `line_total`
- `created_at`
- `updated_at`

## Check Constraints

- `quantity > 0`
- `unit_price >= 0`
- `line_total >= 0`

---

# Indexes

| Index | Columns |
|--------|---------|
| Primary Key | `id` |
| Index | `sale_id` |
| Index | `product_id` |

---

# Relationships

## Many-to-One (Sales)

Many sale items belong to one sale.

```text
sale_items (N)
      │
      │
      ▼
sales (1)
```

Foreign Key:

```text
sale_id → sales.id
```

---

## Many-to-One (Products)

Many sale items can reference the same product.

```text
sale_items (N)
      │
      │
      ▼
products (1)
```

Foreign Key:

```text
product_id → products.id
```

---

# Business Rules

- Every sale item must belong to exactly one sale.
- Every sale item must reference one existing product.
- Quantity must always be greater than zero.
- The unit price is copied from the product at the time of the sale.
- Changing a product's selling price later must not affect previous invoices.
- The line total is calculated as:

```
quantity × unit_price
```

- The invoice subtotal is the sum of all line totals.

---

# Future Considerations

The following features may be added in future versions:

- Item discount
- Tax per item
- Batch/Lot number
- Serial number
- Returned quantity
- Profit per item
- Cost price snapshot

These features are **not** part of the MVP.

---

# Example Data

| id | sale_id | product_id | quantity | unit_price | line_total |
|----|---------|------------|----------|------------|------------|
| 1 | 1 | 3 | 2 | 120.00 | 240.00 |
| 2 | 1 | 8 | 1 | 180.00 | 180.00 |
| 3 | 1 | 15 | 4 | 70.00 | 280.00 |

---

# Notes

- Table names use **snake_case**.
- Column names follow the **snake_case** naming convention.
- English is used for all database objects.
- Product prices are stored as snapshots to preserve invoice history.
- This table is part of the SmartStore MVP database schema.