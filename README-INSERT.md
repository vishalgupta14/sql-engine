# ✅ Insert Query Support in SQL Engine

This document outlines how the SQL Engine supports **cross-database INSERT queries**, including native and emulated RETURNING logic, and SELECT-based inserts.

---

## 📥 INSERT Compatibility Matrix

| Feature                         | MySQL | PostgreSQL | Oracle | SQL Server | MariaDB | SQLite |
|---------------------------------|:-----:|:----------:|:------:|:----------:|:-------:|:------:|
| INSERT INTO ... VALUES (...)    | ✅    | ✅         | ✅     | ✅         | ✅      | ✅     |
| INSERT INTO ... SELECT ...      | ✅    | ✅         | ✅     | ✅         | ✅      | ✅     |
| INSERT ... RETURNING            | ❌    | ✅         | ✅     | ✅ OUTPUT  | ❌      | ❌     |
| Emulated RETURNING Support      | ✅    | ✅         | ✅     | ✅         | ✅      | ✅     |

---

## ⚙️ Emulation Strategy for Unsupported RETURNING

For databases that do **not support RETURNING**, the engine performs:

### 1. Phase 1: Insert Record (Basic Insert)
```sql
INSERT INTO users (name, email) VALUES ('john', 'john@example.com');
```

### 2. Phase 2: SELECT using Inserted Values
```sql
SELECT id, email FROM users WHERE name = 'john' AND email = 'john@example.com';
```

---

## 🧠 Benefits of RETURNING Emulation

- ✅ Works on **all major databases**
- ✅ Supports consistent **returningFields** query templates
- ✅ Enables downstream **data retrieval** after insert

---

## 📦 Supported Insert Query Types

### 🧾 Query Type Reference Table

| Query Type                                   | Description                               | Example |
|----------------------------------------------|-------------------------------------------|---------|
| `INSERT INTO ... VALUES ...`                 | Basic insert with literal values          | `INSERT INTO users (name, email) VALUES ('John', 'john@example.com')` |
| `INSERT INTO ... SELECT ...`                 | Insert from query result                  | `INSERT INTO archive_users SELECT * FROM users WHERE active = false` |
| `INSERT INTO ... VALUES ... RETURNING ...`   | Insert and return specified columns       | `INSERT INTO users (name) VALUES ('X') RETURNING id` |
| `INSERT ... SELECT ... RETURNING ...`        | Select-based insert with RETURNING        | `INSERT INTO audit_logs SELECT * FROM logs RETURNING id` |
| `INSERT with Emulated RETURNING`             | Uses SELECT after insert if unsupported   | (see strategy above) |

---

## ✅ Example JSON Templates for INSERT


### 1. Basic INSERT with Literal Values
```json
{"queryType": "INSERT", "tableName": "users", "insertValues": {"id": "101", "name": "Alice", "email": "alice@example.com"}}
```

### 2. INSERT with NOW() Timestamp
```json
{"queryType": "INSERT", "tableName": "logs", "insertValues": {"action": "LOGIN", "timestamp": "NOW()"}}
```

### 3. INSERT into Orders Table
```json
{"queryType": "INSERT", "tableName": "orders", "insertValues": {"order_id": "5001", "user_id": "101", "status": "PENDING"}}
```

### 4. INSERT with Returning Fields (PostgreSQL)
```json
{"queryType": "INSERT", "tableName": "users", "insertValues": {"name": "Bob", "email": "bob@example.com"}, "returningFields": ["id", "email"]}
```

### 5. INSERT with Default Primary Key
```json
{"queryType": "INSERT", "tableName": "categories", "insertValues": {"name": "Books"}}
```

### 6. Batch INSERT Emulated (Single Row)
```json
{"queryType": "INSERT", "tableName": "metrics", "insertValues": {"key": "cpu", "value": "85.5"}}
```

### 7. INSERT with Subquery
```json
{"queryType": "INSERT", "tableName": "archive_orders", "insertSelect": "SELECT * FROM orders WHERE created_at < NOW() - INTERVAL 30 DAY"}
```

### 8. INSERT with Boolean Value
```json
{"queryType": "INSERT", "tableName": "flags", "insertValues": {"flag": "true"}}
```

### 9. INSERT into Composite Key Table
```json
{"queryType": "INSERT", "tableName": "user_roles", "insertValues": {"user_id": "101", "role_id": "admin"}}
```

### 10. INSERT with JSON Data
```json
{"queryType": "INSERT", "tableName": "documents", "insertValues": {"title": "Policy", "content": "{"section": 1, "text": "All users must comply."}"}}
```

### 11. INSERT with Numeric Casting
```json
{"queryType": "INSERT", "tableName": "payments", "insertValues": {"amount": "1234.56", "currency": "USD"}}
```

### 12. INSERT Current Date
```json
{"queryType": "INSERT", "tableName": "schedule", "insertValues": {"event": "Meeting", "date": "CURRENT_DATE"}}
```

### 13. INSERT with CTE (not all DBs)
```json
{"queryType": "INSERT", "ctes": [{"name": "recent", "query": "SELECT id FROM orders WHERE created_at > NOW() - INTERVAL 1 DAY"}], "tableName": "summary", "insertSelect": "SELECT id FROM recent"}
```

### 14. INSERT NULL Values
```json
{"queryType": "INSERT", "tableName": "optional_fields", "insertValues": {"field1": null, "field2": "Non-null"}}
```

### 15. INSERT and Return ID
```json
{"queryType": "INSERT", "tableName": "emails", "insertValues": {"recipient": "test@example.com", "subject": "Welcome"}, "returningFields": ["id"]}
```



---

## 🔐 Future Enhancements

- ✅ Batch inserts (multi-row)
- ✅ Upsert / MERGE support (`ON CONFLICT`, `ON DUPLICATE KEY`)
- ✅ INSERT + RETURNING batching
- ✅ Fallback ID-based resolution when no primary key is defined

---