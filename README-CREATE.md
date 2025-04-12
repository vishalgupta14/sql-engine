# 🏗️ Supported CREATE TABLE Query Types

## 🧾 Query Type Reference Table for CREATE TABLE

| Query Type                               | Description                             | Example |
|------------------------------------------|-----------------------------------------|---------|
| `CREATE TABLE ... (col1 TYPE, ...)`      | Basic table creation                    | `CREATE TABLE users (id INT, name VARCHAR(255))` |
| `CREATE TABLE IF NOT EXISTS ...`         | Conditional creation                    | `CREATE TABLE IF NOT EXISTS users (id INT, name VARCHAR(255))` |
| `CREATE TEMPORARY TABLE ...`             | Temporary table                         | `CREATE TEMPORARY TABLE temp_data (x INT)` |
| `CREATE TABLE ... AS SELECT ...`         | CTAS (create from select result)        | `CREATE TABLE active_users AS SELECT * FROM users WHERE active = true` |
| `CREATE TABLE ... LIKE ...`              | Clone table structure (MySQL)           | `CREATE TABLE new_users LIKE users` |
| `CREATE TABLE ... INHERITS (...)`        | Inheritance (PostgreSQL)                | `CREATE TABLE special_users (vip_level INT) INHERITS (users)` |

---

## ✅ Example JSON Templates for CREATE TABLE

### 1. Simple CREATE TABLE
```json
{
  "queryType": "CREATE",
  "sqlQuery": "CREATE TABLE users (id INT PRIMARY KEY, name VARCHAR(100), email VARCHAR(255))"
}
```

### 2. CREATE TABLE IF NOT EXISTS
```json
{
  "queryType": "CREATE",
  "sqlQuery": "CREATE TABLE IF NOT EXISTS products (id SERIAL, title TEXT, price DECIMAL(10,2))"
}
```

### 3. CREATE TEMPORARY TABLE
```json
{
  "queryType": "CREATE",
  "sqlQuery": "CREATE TEMPORARY TABLE temp_orders (order_id INT, created_at TIMESTAMP)"
}
```

### 4. CREATE TABLE AS SELECT
```json
{
  "queryType": "CREATE",
  "sqlQuery": "CREATE TABLE vip_users AS SELECT * FROM users WHERE vip = true"
}
```

### 5. CREATE TABLE LIKE (MySQL-style)
```json
{
  "queryType": "CREATE",
  "sqlQuery": "CREATE TABLE archived_orders LIKE orders"
}
```

### 6. CREATE TABLE INHERITS (PostgreSQL)
```json
{
  "queryType": "CREATE",
  "sqlQuery": "CREATE TABLE managers (department TEXT) INHERITS (employees)"
}
```

### 7. CREATE TABLE with constraints
```json
{
  "queryType": "CREATE",
  "sqlQuery": "CREATE TABLE invoices (id INT PRIMARY KEY, amount DECIMAL, created_at DATE, CONSTRAINT chk_amount CHECK (amount > 0))"
}
```

### 8. CREATE TABLE with foreign key
```json
{
  "queryType": "CREATE",
  "sqlQuery": "CREATE TABLE order_items (id INT, order_id INT, product_id INT, FOREIGN KEY (order_id) REFERENCES orders(id))"
}
```

### 9. CREATE TABLE with JSONB (PostgreSQL)
```json
{
  "queryType": "CREATE",
  "sqlQuery": "CREATE TABLE events (id UUID PRIMARY KEY, payload JSONB)"
}
```

### 10. CREATE TABLE with spatial data (PostGIS)
```json
{
  "queryType": "CREATE",
  "sqlQuery": "CREATE TABLE locations (id SERIAL PRIMARY KEY, name TEXT, geom GEOMETRY(Point, 4326))"
}
```

### 11. CREATE TABLE for SQLite
```json
{
  "queryType": "CREATE",
  "sqlQuery": "CREATE TABLE notes (id INTEGER PRIMARY KEY AUTOINCREMENT, content TEXT)"
}
```

### 12. CREATE TABLE using ENUM (PostgreSQL)
```json
{
  "queryType": "CREATE",
  "sqlQuery": "CREATE TYPE mood AS ENUM ('sad', 'ok', 'happy'); CREATE TABLE feelings (id INT, current_mood mood)"
}
```

### 13. CREATE TABLE with auto timestamp
```json
{
  "queryType": "CREATE",
  "sqlQuery": "CREATE TABLE audit_logs (id INT, log TEXT, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
}
```

### 14. CREATE TABLE with composite primary key
```json
{
  "queryType": "CREATE",
  "sqlQuery": "CREATE TABLE enrollments (student_id INT, course_id INT, PRIMARY KEY (student_id, course_id))"
}
```

### 15. CREATE TABLE with array column (PostgreSQL)
```json
{
  "queryType": "CREATE",
  "sqlQuery": "CREATE TABLE tags (id SERIAL PRIMARY KEY, labels TEXT[])"
}
```