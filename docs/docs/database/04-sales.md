# Sales Table Design

## Overview

The **Sales** table stores sales transactions (invoice headers) in SmartStore.

Each record represents one completed sale and contains general information about the invoice.

The products sold are stored separately in the **Sale Items** table.

---

# Table Information

| Property | Value |
|----------|-------|
| Table Name | sales |
| Purpose | Store sales invoices |
| Primary Key | id |

---

# Columns

| Column | Data Type | Nullable | Description |
|---------|-----------|----------|-------------|
| id | BIGINT | No | Primary Key (Auto Increment) |
| invoice_number | VARCHAR(30) | No | Unique invoice number |
| sale_date | TIMESTAMP | No | Date and time of sale |
| subtotal | DECIMAL(10,2) | No | Total amount before discount |
| discount | DECIMAL(10,2) | No | Discount amount |
| total | DECIMAL(10,2) | No | Final invoice amount |
| payment_method | VARCHAR(20) | No | Payment method (Cash for MVP) |
| created_at | TIMESTAMP | No | Creation date |
| updated_at | TIMESTAMP | No | Last modification date |

---

# Constraints

## Primary Key

- `id`

## Unique Constraints

- `invoice_number`

## Not Null

- `id`
- `invoice_number`
- `sale_date`
- `subtotal`
- `discount`
- `total`
- `payment_method`
- `created_at`
- `updated_at`

## Check Constraints

- `subtotal >= 0`
- `discount >= 0`
- `total >= 0`

---

# Indexes

| Index | Columns |
|--------|---------|
| Primary Key | `id` |
| Unique Index | `invoice_number` |
| Index | `sale_date` |

---

# Relationships

## One-to-Many

One sale can contain one or more sale items.

```text
sales (1)
      │
      │
      ▼
sale_items (N)
```

Foreign Key:

```text
sale_items.sale_id → sales.id
```

---

# Business Rules

- Every sale must have a unique invoice number.
- Every sale must contain at least one sale item.
- Invoice totals are calculated automatically from the associated sale items.
- Discount cannot be greater than the subtotal.
- Sales are immutable after completion unless an authorized correction process is performed.
- In the MVP version, the only supported payment method is **Cash**.

---

# Future Considerations

The following features may be added in future versions:

- Customer reference
- Employee (Cashier)
- Tax amount
- Payment status
- Invoice status
- Notes
- Refund reference
- Receipt printing status
- Multiple payment methods (Cash, BaridiMob, CIB, CCP, Bank Transfer)

These features are **not** part of the MVP.

---

# Example Data

| id | invoice_number | sale_date | subtotal | discount | total | payment_method |
|----|----------------|-----------|----------|----------|-------|----------------|
| 1 | INV-202600001 | 2026-07-29 14:25:12 | 850.00 | 50.00 | 800.00 | Cash |
| 2 | INV-202600002 | 2026-07-29 15:11:45 | 420.00 | 0.00 | 420.00 | Cash |

---

# Notes

- Table names use **snake_case**.
- Column names follow the **snake_case** naming convention.
- English is used for all database objects.
- This table stores only invoice header information.
- Invoice items are stored in the **sale_items** table.
- The payment system is designed to support additional payment methods in future versions without changing the database structure.
- This table is part of the SmartStore MVP database schema.