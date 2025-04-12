
# ✅ Native SQL Query Support in SQL Engine

This document explains how native (raw) SQL query execution is supported via the `sqlQuery` field in `QueryTemplate`. If a native query is present, it bypasses the dynamic query strategy engine.

---

## 🧠 Why Native SQL Support?

While the engine supports dynamic templates (SELECT/INSERT/UPDATE/DELETE/UPSERT), advanced use cases may require custom SQL.

✅ Use native SQL when:
- You want full control over the SQL logic.
- You need vendor-specific optimizations.
- You are doing advanced analytics or aggregations.

---

## ⚙️ How It Works

If `QueryTemplate.sqlQuery` is **non-null and not blank**, the engine executes it directly:

```java
if (template.getSqlQuery() != null && !template.getSqlQuery().isBlank()) {
    dbClient.sql(template.getSqlQuery())
            .fetch()
            .all()
            .collectList()
            .map(result -> QueryRunResponse.newBuilder().setJsonResult(result.toString()).build())
            .subscribe(...);
}
```

If `sqlQuery` is not set, it falls back to dynamic strategy (SELECT, UPDATE, etc.).

---

## ✅ Sample QueryTemplate with Native SQL

### 1. Raw SELECT Query
```json
{
  "queryType": "SELECT",
  "sqlQuery": "SELECT name, email FROM users WHERE status = 'ACTIVE'"
}
```

### 2. Native JOIN + Aggregation
```json
{
  "queryType": "SELECT",
  "sqlQuery": "SELECT d.name, COUNT(e.id) FROM departments d JOIN employees e ON d.id = e.dept_id GROUP BY d.name"
}
```

### 3. Complex Window Function
```json
{
  "queryType": "SELECT",
  "sqlQuery": "SELECT name, salary, RANK() OVER (ORDER BY salary DESC) as rank FROM employees"
}
```

### 4. Vendor-Specific Hint (e.g., Oracle)
```json
{
  "queryType": "SELECT",
  "sqlQuery": "SELECT /*+ PARALLEL(employees 4) */ * FROM employees"
}
```

### 5. Native DML with RETURNING (PostgreSQL)
```json
{
  "queryType": "INSERT",
  "sqlQuery": "INSERT INTO users (name, email) VALUES ('bob', 'bob@example.com') RETURNING id"
}
```

---

## 🛡️ Limitations

- ✅ `sqlQuery` is expected to be fully formed SQL. No condition merging or dynamic parameterization.
- ⚠️ You are responsible for:
    - Correct SQL syntax.
    - Safety against SQL injection (only trusted users should use this).

---

## 🔒 Best Practices

- Use native SQL only for power users / admin users.
- Validate and audit usage.
- If dynamic values are used, sanitize inputs or use parameterized templates.

---

## 🔄 Engine Fallback Logic

| Condition                              | Behavior                       |
|----------------------------------------|--------------------------------|
| `sqlQuery != null && not blank`        | Native SQL executed directly   |
| `sqlQuery == null || sqlQuery.isBlank` | Dynamic strategy-based engine  |

---

## 🧪 Testing Native SQL

Test using Postman, curl, or SDK with:
```json
{
  "templateName": "native-sales",
  "queryType": "SELECT",
  "sqlQuery": "SELECT * FROM sales ORDER BY created_at DESC LIMIT 10"
}
```

---

## 📌 Summary

✅ Native SQL support gives you flexibility to:
- Run complex queries
- Use vendor features
- Override template engine safely

💡 Use it wisely. It is powerful but needs careful usage.
