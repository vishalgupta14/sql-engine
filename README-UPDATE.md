
# ✅ Update Query Support in SQL Engine

This document outlines how the SQL Engine supports **cross-database UPDATE queries**, including those with JOINs, by leveraging emulation strategies for databases that don't support `UPDATE ... JOIN` natively.

---

## 🔁 UPDATE Compatibility Matrix

| Feature                    | MySQL | PostgreSQL | Oracle | SQL Server | MariaDB | SQLite |
|----------------------------|:-----:|:----------:|:------:|:----------:|:-------:|:------:|
| UPDATE with WHERE          | ✅    | ✅         | ✅     | ✅         | ✅      | ✅     |
| UPDATE with INNER JOIN     | ✅    | ❌ (emulate) | ❌ (emulate) | ✅     | ✅      | ❌ (emulate) |
| UPDATE with LEFT JOIN      | ✅    | ❌ (emulate) | ❌ (emulate) | ✅     | ✅      | ❌ (emulate) |
| UPDATE with RIGHT JOIN     | ❌ (emulate) | ❌ (emulate) | ❌ (emulate) | ✅ | ❌ (emulate) | ❌ (emulate) |
| UPDATE with FULL JOIN      | ❌ (emulate) | ❌ (emulate) | ❌ (emulate) | ❌ (emulate) | ❌ (emulate) | ❌ (emulate) |
| UPDATE with CROSS JOIN     | ✅    | ✅         | ✅     | ✅         | ✅      | ✅     |

---

## ⚙️ Emulation Strategy for Unsupported JOINs

For databases that **do not support UPDATE with JOINs**, the engine performs a two-step strategy:

### 1. Phase 1: Select Matching Primary Keys
```sql
SELECT a.id
FROM a
FULL OUTER JOIN b ON a.key = b.key
WHERE b.status = 'inactive';
```

### 2. Phase 2: Update Based on IDs
```sql
UPDATE a
SET status = 'ARCHIVED'
WHERE id IN (:ids);
```

---

## 🧠 Benefits of Emulated Strategy

- ✅ Works on **all major databases**
- ✅ Supports **ALL JOIN types**:
  - INNER, LEFT, RIGHT, FULL, CROSS
- ✅ Clean separation of **query planning** and **execution**
- ✅ Enables batching, retry, fallback

---


## 📦 Supported Update Query Types (as of now)

### 🧾 Query Type Reference Table

| Query Type                             | Description                                   | Example |
|----------------------------------------|-----------------------------------------------|---------|
| `UPDATE table SET col = val`           | Update all rows without filter                | `UPDATE users SET status = 'inactive'` |
| `UPDATE ... WHERE ...`                 | Update filtered rows                          | `UPDATE users SET status = 'active' WHERE age > 25` |
| `UPDATE ... SET col = col + 1`         | Increment a numeric column                    | `UPDATE counters SET count = count + 1 WHERE id = 1` |
| `UPDATE ... SET multiple cols`         | Update multiple columns                       | `UPDATE orders SET status = 'shipped', shipped_at = NOW() WHERE id = 1001` |
| `UPDATE ... WHERE col IN (...)`        | Conditional update on a set of IDs            | `UPDATE users SET verified = true WHERE id IN (1, 2, 3)` |

---

### ✅ Example JSON Templates for Basic UPDATE Queries

### 1. Update All Rows (No WHERE)
```json
{
  "queryType": "UPDATE",
  "tableName": "users",
  "updatedValues": {
    "status": "inactive"
  }
}
```

### 2. Update with Filter (WHERE clause)
```json
{
  "queryType": "UPDATE",
  "tableName": "users",
  "updatedValues": {
    "status": "active"
  },
  "conditions": [
    { "fieldName": "age", "value": "25", "operator": "GREATER_THAN" }
  ]
}
```

### 3. Update with Arithmetic Operation
```json
{
  "queryType": "UPDATE",
  "tableName": "counters",
  "updatedValues": {
    "count": "count + 1"
  },
  "conditions": [
    { "fieldName": "id", "value": "1", "operator": "EQUALS" }
  ]
}
```

### 4. Update Multiple Columns
```json
{
  "queryType": "UPDATE",
  "tableName": "orders",
  "updatedValues": {
    "status": "shipped",
    "shipped_at": "NOW()"
  },
  "conditions": [
    { "fieldName": "id", "value": "1001", "operator": "EQUALS" }
  ]
}
```

### 5. Update Based on IN Clause
```json
{
  "queryType": "UPDATE",
  "tableName": "users",
  "updatedValues": {
    "verified": "true"
  },
  "conditions": [
    { "fieldName": "id", "value": "[1,2,3]", "operator": "IN" }
  ]
}
```

---

## 📦 Supported JOIN + UPDATE Query Types

### 🧾 Query Type Reference Table for UPDATE + JOIN

| Query Type                                   | Description                               | Example |
|----------------------------------------------|-------------------------------------------|---------|
| `UPDATE A JOIN B ON ... SET A.col = ...`     | Update from joined table using condition  | `UPDATE orders o JOIN users u ON o.user_id = u.id SET o.status = 'confirmed' WHERE u.vip = true` |
| `UPDATE A LEFT JOIN B ...`                   | Update left table with join filter        | `UPDATE orders o LEFT JOIN coupons c ON o.coupon_id = c.id SET o.discount = c.amount WHERE c.expired = false` |
| `UPDATE A INNER JOIN B ...`                  | Standard inner join update                | `UPDATE products p INNER JOIN stock s ON p.id = s.product_id SET p.in_stock = s.count > 0` |
| `UPDATE A SET ... WHERE A.id IN (SELECT...)` | Fallback for unsupported JOIN UPDATEs     | `UPDATE orders SET status = 'cancelled' WHERE id IN (SELECT o.id FROM orders o JOIN payments p ON o.payment_id = p.id WHERE p.failed = true)` |
| `UPDATE ... WITH CTE ...`                    | Emulated join using CTE and update        | `WITH recent_orders AS (SELECT id FROM orders WHERE created_at > NOW() - INTERVAL 7 DAY) UPDATE orders SET recent = true WHERE id IN (SELECT id FROM recent_orders)` |

---

### ✅ Example JSON Templates for UPDATE with JOIN

### 1. INNER JOIN Based Update
```json
{
  "queryType": "UPDATE",
  "tableName": "orders",
  "updatedValues": {
    "status": "confirmed"
  },
  "joins": [
    {
      "joinType": "INNER",
      "table": "users",
      "alias": "u",
      "onCondition": "orders.user_id = u.id"
    }
  ],
  "conditions": [
    { "fieldName": "u.vip", "value": "true", "operator": "EQUALS" }
  ]
}
```

### 2. LEFT JOIN Update with Discount
```json
{
  "queryType": "UPDATE",
  "tableName": "orders",
  "updatedValues": {
    "discount": "c.amount"
  },
  "joins": [
    {
      "joinType": "LEFT",
      "table": "coupons",
      "alias": "c",
      "onCondition": "orders.coupon_id = c.id"
    }
  ],
  "conditions": [
    { "fieldName": "c.expired", "value": "false", "operator": "EQUALS" }
  ]
}
```

### 3. Update using Emulated RIGHT JOIN
```json
{
  "queryType": "UPDATE",
  "tableName": "stock",
  "updatedValues": {
    "status": "orphaned"
  },
  "joins": [
    {
      "joinType": "RIGHT",
      "table": "products",
      "alias": "p",
      "onCondition": "stock.product_id = p.id"
    }
  ]
}
```

### 4. Update with Subquery Fallback
```json
{
  "queryType": "UPDATE",
  "tableName": "orders",
  "updatedValues": {
    "status": "cancelled"
  },
  "conditions": [
    {
      "fieldName": "id",
      "value": "(SELECT o.id FROM orders o JOIN payments p ON o.payment_id = p.id WHERE p.failed = true)",
      "operator": "IN"
    }
  ]
}
```

### 5. Update with CTE
```json
{
  "queryType": "UPDATE",
  "ctes": [
    {
      "name": "recent_orders",
      "query": "SELECT id FROM orders WHERE created_at > NOW() - INTERVAL 7 DAY"
    }
  ],
  "tableName": "orders",
  "updatedValues": {
    "recent": "true"
  },
  "conditions": [
    {
      "fieldName": "id",
      "value": "(SELECT id FROM recent_orders)",
      "operator": "IN"
    }
  ]
}
```

