
## 🗑️ Supported DELETE Query Types

### 🧾 Query Type Reference Table for DELETE + JOIN

| Query Type                                         | Description                                 | Example |
|----------------------------------------------------|---------------------------------------------|---------|
| `DELETE FROM A WHERE ...`                         | Simple delete with conditions               | `DELETE FROM orders WHERE status = 'cancelled'` |
| `DELETE A FROM A JOIN B ON ... WHERE ...`         | Delete with JOIN (MySQL-style)              | `DELETE o FROM orders o JOIN users u ON o.user_id = u.id WHERE u.vip = true` |
| `DELETE FROM A USING B WHERE ...`                 | PostgreSQL-style delete with USING          | `DELETE FROM orders USING users WHERE orders.user_id = users.id AND users.vip = true` |
| `DELETE FROM A WHERE id IN (SELECT ...)`          | Emulated join-based delete (for all DBs)    | `DELETE FROM orders WHERE id IN (SELECT o.id FROM orders o JOIN users u ON o.user_id = u.id WHERE u.vip = true)` |
| `WITH ids AS (SELECT ...) DELETE FROM A WHERE ...`| CTE-based delete (Postgres/Oracle)          | `WITH stale_orders AS (SELECT id FROM orders WHERE updated_at < NOW() - INTERVAL 30 DAY) DELETE FROM orders WHERE id IN (SELECT id FROM stale_orders)` |

---

### ✅ Example JSON Templates for DELETE with JOIN

### 1. Simple DELETE by status
```json
{
  "queryType": "DELETE",
  "tableName": "orders",
  "conditions": [
    { "fieldName": "status", "value": "cancelled", "operator": "EQUALS" }
  ]
}
```

### 2. DELETE with JOIN condition (emulated)
```json
{
  "queryType": "DELETE",
  "tableName": "orders",
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

### 3. DELETE with RIGHT JOIN (emulated)
```json
{
  "queryType": "DELETE",
  "tableName": "logs",
  "joins": [
    {
      "joinType": "RIGHT",
      "table": "users",
      "alias": "u",
      "onCondition": "logs.user_id = u.id"
    }
  ]
}
```

### 4. DELETE using a subquery fallback
```json
{
  "queryType": "DELETE",
  "tableName": "orders",
  "conditions": [
    {
      "fieldName": "id",
      "value": "(SELECT o.id FROM orders o JOIN payments p ON o.payment_id = p.id WHERE p.failed = true)",
      "operator": "IN"
    }
  ]
}
```

### 5. DELETE using a CTE
```json
{
  "queryType": "DELETE",
  "ctes": [
    {
      "name": "stale_orders",
      "query": "SELECT id FROM orders WHERE updated_at < NOW() - INTERVAL 30 DAY"
    }
  ],
  "tableName": "orders",
  "conditions": [
    {
      "fieldName": "id",
      "value": "(SELECT id FROM stale_orders)",
      "operator": "IN"
    }
  ]
}
```

### 6. DELETE with LEFT JOIN and fallback
```json
{
  "queryType": "DELETE",
  "tableName": "sessions",
  "joins": [
    {
      "joinType": "LEFT",
      "table": "users",
      "alias": "u",
      "onCondition": "sessions.user_id = u.id"
    }
  ],
  "conditions": [
    { "fieldName": "u.id", "value": "NULL", "operator": "IS" }
  ]
}
```

### 7. DELETE based on time range
```json
{
  "queryType": "DELETE",
  "tableName": "logs",
  "conditions": [
    { "fieldName": "timestamp", "value": "2023-01-01", "operator": "LESS_THAN" }
  ]
}
```

### 8. DELETE with multiple conditions
```json
{
  "queryType": "DELETE",
  "tableName": "orders",
  "conditions": [
    { "fieldName": "status", "value": "failed", "operator": "EQUALS" },
    { "fieldName": "retry_count", "value": "3", "operator": "GREATER_THAN" }
  ]
}
```

### 9. DELETE with returning fields (PostgreSQL)
```json
{
  "queryType": "DELETE",
  "tableName": "orders",
  "conditions": [
    { "fieldName": "status", "value": "cancelled", "operator": "EQUALS" }
  ],
  "returningFields": ["id", "status"]
}
```

### 10. DELETE using sqlQuery directly
```json
{
  "queryType": "DELETE",
  "sqlQuery": "DELETE FROM logs WHERE created_at < NOW() - INTERVAL 90 DAY"
}
```
