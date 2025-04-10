# SQL Engine - Multi-DB Query Processing Engine

## 🚀 Overview
SQL Engine is a scalable, consistent, and highly available multi-database query processing platform that supports:

- ✅ Dynamic SELECT query generation
- ✅ Metadata-aware parameter casting
- ✅ Multi-tenant DB support (MySQL, PostgreSQL, Oracle, MSSQL)
- ✅ Caffeine-based metadata & connection pooling
- ✅ Safe schema-aware validation (auto-refresh on schema drift)

---

## 📦 Supported Query Types (as of now)

### 🧾 Query Type Reference Table

| Query Type                             | Description                              | Example |
|----------------------------------------|------------------------------------------|---------|
| `SELECT * FROM table`                  | Fetch all columns                        | `SELECT * FROM users` |
| `SELECT col1, col2 FROM table`         | Fetch selected columns only              | `SELECT id, name FROM users` |
| `SELECT ... WHERE ...`                 | Conditional filtering                    | `SELECT * FROM users WHERE age > 25` |
| `SELECT ... WHERE col IN (...)`        | Multiple value filtering                 | `SELECT * FROM orders WHERE status IN ('PAID', 'PENDING')` |
| `SELECT ... WHERE col LIKE ...`        | Pattern matching                         | `SELECT * FROM customers WHERE name LIKE 'A%'` |
| `SELECT ... WHERE col > ?`             | Range filtering                          | `SELECT * FROM sales WHERE amount > 5000` |
| `SELECT ... LIMIT ... OFFSET ...`      | Pagination with max rows and offset      | `SELECT * FROM users ORDER BY created_at DESC LIMIT 10 OFFSET 20` |
| `SELECT ... GROUP BY ... HAVING ...`   | Aggregation + filtering on aggregates    | `SELECT region, SUM(amount) AS total_sales FROM orders GROUP BY region HAVING SUM(amount) > 50000` |

---

### ✅ Example JSON Template per Query Type

### 1. Fetch All Columns
```json
{
  "queryType": "SELECT",
  "tableName": "users"
}
```

### 2. Fetch Selected Columns
```json
{
  "queryType": "SELECT",
  "tableName": "users",
  "selectedColumns": [
    { "expression": "id" },
    { "expression": "name" }
  ]
}
```

### 3. Conditional Filtering (WHERE)
```json
{
  "queryType": "SELECT",
  "tableName": "users",
  "conditions": [
    { "fieldName": "age", "value": "25", "operator": "GREATER_THAN" }
  ]
}
```

### 4. Filtering with IN
```json
{
  "queryType": "SELECT",
  "tableName": "orders",
  "conditions": [
    { "fieldName": "status", "value": "[\"PAID\", \"PENDING\"]", "operator": "IN" }
  ]
}
```

### 5. LIKE Condition
```json
{
  "queryType": "SELECT",
  "tableName": "customers",
  "conditions": [
    { "fieldName": "name", "value": "A%", "operator": "LIKE" }
  ]
}
```

### 6. Range Filter + ORDER BY
```json
{
  "queryType": "SELECT",
  "tableName": "sales",
  "selectedColumns": [
    { "expression": "id" },
    { "expression": "amount" },
    { "expression": "created_at" }
  ],
  "conditions": [
    { "fieldName": "amount", "value": "5000", "operator": "GREATER_THAN" }
  ],
  "orderBy": {
    "created_at": "DESC"
  }
}
```

### 7. Pagination with LIMIT and OFFSET
```json
{
  "queryType": "SELECT",
  "tableName": "users",
  "selectedColumns": [
    { "expression": "id" },
    { "expression": "name" }
  ],
  "conditions": [
    { "fieldName": "status", "value": "active", "operator": "EQUALS" }
  ],
  "orderBy": {
    "created_at": "DESC"
  },
  "limit": 10,
  "offset": 20
}
```

### 8. GROUP BY + HAVING
```json
{
  "queryType": "SELECT",
  "tableName": "orders",
  "selectedColumns": [
    { "expression": "region" },
    { "expression": "SUM(amount)", "alias": "total_sales" }
  ],
  "conditions": [
    { "fieldName": "status", "value": "PAID", "operator": "EQUALS" }
  ],
  "groupBy": ["region"],
  "havingConditions": [
    { "fieldName": "total_sales", "value": "50000", "operator": "GREATER_THAN" }
  ],
  "orderBy": {
    "total_sales": "DESC"
  },
  "limit": 10
}
```

## 📦 Supported JOIN Query Types

### 🧾 Query Type Reference Table for JOINS

| Query Type                             | Description                              | Example |
|----------------------------------------|------------------------------------------|---------|
| `SELECT ... JOIN ...`                  | Join between two tables                  | `SELECT o.id, c.name FROM orders o INNER JOIN customers c ON o.customer_id = c.id` |
| `SELECT ... LEFT JOIN ...`             | Fetch records even if no match exists in the second table | `SELECT o.id, c.name FROM orders o LEFT JOIN customers c ON o.customer_id = c.id` |
| `SELECT ... RIGHT JOIN ...`            | Fetch records even if no match exists in the first table | `SELECT o.id, c.name FROM orders o RIGHT JOIN customers c ON o.customer_id = c.id` |
| `SELECT ... JOIN ... ON ...`           | Join with condition (complex ON clause)  | `SELECT o.id, p.name FROM orders o INNER JOIN products p ON o.product_id = p.id WHERE p.category = 'electronics'` |
| `SELECT ... JOIN ... WITH alias`       | Add aliases for readability              | `SELECT o.id, c.name AS customer_name FROM orders o INNER JOIN customers c ON o.customer_id = c.id` |
| `SELECT ... INNER JOIN ...`            | Standard INNER JOIN                      | `SELECT o.id, p.name FROM orders o INNER JOIN products p ON o.product_id = p.id` |
| `SELECT ... FULL OUTER JOIN ...`       | Full OUTER JOIN between tables           | `SELECT o.id, c.name FROM orders o FULL OUTER JOIN customers c ON o.customer_id = c.id` |
| `SELECT ... LEFT JOIN ... WHERE ...`   | LEFT JOIN with filtering                 | `SELECT o.id, c.name FROM orders o LEFT JOIN customers c ON o.customer_id = c.id WHERE c.status = 'active'` |
| `SELECT ... RIGHT JOIN ... WHERE ...`  | RIGHT JOIN with filtering                | `SELECT o.id, c.name FROM orders o RIGHT JOIN customers c ON o.customer_id = c.id WHERE c.status = 'active'` |
| `SELECT ... LEFT JOIN ... GROUP BY ...`| LEFT JOIN with GROUP BY and aggregation   | `SELECT o.product_id, COUNT(o.id) FROM orders o LEFT JOIN products p ON o.product_id = p.id GROUP BY o.product_id` |
| `SELECT ... CROSS JOIN ...`            | Cartesian Product between two tables     | `SELECT * FROM products CROSS JOIN categories` |

---

### ✅ Example JSON Template per JOIN Query Type

### 1. Basic INNER JOIN between two tables (orders, customers)
```json
{
  "queryType": "SELECT",
  "fromTable": "orders",
  "joins": [
    {
      "joinType": "INNER",
      "table": "customers",
      "alias": "c",
      "onCondition": "orders.customer_id = c.id"
    }
  ],
  "selectedColumns": [
    { "expression": "orders.id" },
    { "expression": "c.name", "alias": "customer_name" }
  ]
}
```

### 2. LEFT JOIN between three tables (orders, customers, products)
```json
{
  "queryType": "SELECT",
  "fromTable": "orders",
  "joins": [
    {
      "joinType": "LEFT",
      "table": "customers",
      "alias": "c",
      "onCondition": "orders.customer_id = c.id"
    },
    {
      "joinType": "LEFT",
      "table": "products",
      "alias": "p",
      "onCondition": "orders.product_id = p.id"
    }
  ],
  "selectedColumns": [
    { "expression": "orders.id" },
    { "expression": "c.name", "alias": "customer_name" },
    { "expression": "p.name", "alias": "product_name" }
  ]
}
```

### 3. RIGHT JOIN with aliasing
```json
{
  "queryType": "SELECT",
  "fromTable": "orders",
  "joins": [
    {
      "joinType": "RIGHT",
      "table": "products",
      "alias": "p",
      "onCondition": "orders.product_id = p.id"
    }
  ],
  "selectedColumns": [
    { "expression": "orders.id" },
    { "expression": "p.name", "alias": "product_name" }
  ]
}
```

### 4. CROSS JOIN
```json
{
  "queryType": "SELECT",
  "fromTable": "products",
  "joins": [
    {
      "joinType": "CROSS",
      "table": "categories"
    }
  ],
  "selectedColumns": [
    { "expression": "products.id" },
    { "expression": "categories.name", "alias": "category_name" }
  ]
}
```

---

## 📦 Supported COUNT(*) Query Types

### 🧾 Query Type Reference Table for COUNT

| Query Type                            | Description                            | Example |
|---------------------------------------|----------------------------------------|---------|
| `SELECT COUNT(*) FROM table`          | Count all rows                         | `SELECT COUNT(*) FROM users` |
| `SELECT COUNT(*) FROM table WHERE ...`| Count with conditions                  | `SELECT COUNT(*) FROM orders WHERE status = 'PAID'` |
| `SELECT COUNT(*) FROM A JOIN B ...`   | Count rows with join                   | `SELECT COUNT(*) FROM orders o JOIN customers c ON o.customer_id = c.id` |
| `SELECT COUNT(*) GROUP BY ...`        | Count per group                        | `SELECT customer_id, COUNT(*) FROM orders GROUP BY customer_id` |
| `SELECT COUNT(col) FROM table`        | Count non-null values in a column      | `SELECT COUNT(email) FROM users` |
| `SELECT COUNT(*) AS total ...`        | Count with alias                       | `SELECT COUNT(*) AS total_orders FROM orders` |
| `SELECT COUNT(*) ... LIMIT ...`       | Count with pagination (rare)           | `SELECT COUNT(*) FROM users LIMIT 1` |

---

### ✅ Example JSON Templates for COUNT Queries

#### 1. Count All Rows
```json
{
  "queryType": "SELECT",
  "tableName": "users",
  "selectedColumns": [
    { "expression": "COUNT(*)", "alias": "total_users" }
  ]
}
```

#### 2. Count with Condition (WHERE)
```json
{
  "queryType": "SELECT",
  "tableName": "orders",
  "selectedColumns": [
    { "expression": "COUNT(*)", "alias": "paid_orders" }
  ],
  "conditions": [
    { "fieldName": "status", "value": "PAID", "operator": "EQUALS" }
  ]
}
```

#### 3. Count with INNER JOIN
```json
{
  "queryType": "SELECT",
  "fromTable": "orders",
  "joins": [
    {
      "joinType": "INNER",
      "table": "customers",
      "alias": "c",
      "onCondition": "orders.customer_id = c.id"
    }
  ],
  "selectedColumns": [
    { "expression": "COUNT(*)", "alias": "order_customer_count" }
  ]
}
```

#### 4. Count by Group
```json
{
  "queryType": "SELECT",
  "tableName": "orders",
  "selectedColumns": [
    { "expression": "customer_id" },
    { "expression": "COUNT(*)", "alias": "order_count" }
  ],
  "groupBy": ["customer_id"]
}
```

#### 5. Count Non-null Column Values
```json
{
  "queryType": "SELECT",
  "tableName": "users",
  "selectedColumns": [
    { "expression": "COUNT(email)", "alias": "valid_emails" }
  ]
}
```

#### 6. Count with Pagination (rare use-case)
```json
{
  "queryType": "SELECT",
  "tableName": "users",
  "selectedColumns": [
    { "expression": "COUNT(*)", "alias": "total" }
  ],
  "limit": 1
}
```

## 📦 Supported Advanced SELECT Query Types

---

### 🧾 Query Type Reference Table for Advanced SELECT

| Query Type                            | Description                          | Example |
|---------------------------------------|--------------------------------------|---------|
| `SELECT DISTINCT col`                 | Remove duplicates                    | `SELECT DISTINCT email FROM users` |
| `SELECT CASE WHEN ...`               | Conditional projections              | `SELECT name, CASE WHEN age > 18 THEN 'Adult' ELSE 'Minor' END AS status FROM users` |
| `SELECT JSON_OBJECT(...)`            | JSON formatting for APIs (MySQL/Postgres) | `SELECT JSON_OBJECT('id', id, 'name', name) FROM users` |
| `WITH cte_name AS (...) SELECT ...`  | Common Table Expressions (CTE)       | `WITH active AS (SELECT * FROM users WHERE active = 1) SELECT * FROM active` |

---

### ✅ Example JSON Templates for Advanced SELECT Queries

#### 1. SELECT DISTINCT Column
```json
{
  "queryType": "SELECT",
  "tableName": "users",
  "distinct": true,
  "selectedColumns": [
    { "expression": "email" }
  ]
}
```

#### 2. SELECT with CASE WHEN
```json
{
  "queryType": "SELECT",
  "tableName": "users",
  "selectedColumns": [
    { "expression": "name" },
    {
      "expression": "CASE WHEN age < 18 THEN 'Minor' ELSE 'Adult' END",
      "alias": "age_category"
    }
  ]
}
```

#### 3. SELECT JSON_OBJECT (MySQL)
```json
{
  "queryType": "SELECT",
  "tableName": "users",
  "selectedColumns": [
    {
      "expression": "JSON_OBJECT('id', id, 'name', name)",
      "alias": "user_json"
    }
  ]
}
```

#### 4. SELECT with Common Table Expression (CTE)
```json
{
  "queryType": "SELECT",
  "ctes": [
    {
      "name": "active_users",
      "query": "SELECT id, name FROM users WHERE status = 'active'"
    }
  ],
  "tableName": "active_users",
  "selectedColumns": [
    { "expression": "id" },
    { "expression": "name" }
  ]
}
```

---

## 📦 Supported UNION and UNION ALL Queries

---

### 🧾 Query Type Reference Table for UNION Support

| Query Type                   | Description                            | Example |
|------------------------------|----------------------------------------|---------|
| `SELECT ... UNION SELECT ...`       | Combine distinct results              | `SELECT id FROM users WHERE active = 1 UNION SELECT id FROM admins` |
| `SELECT ... UNION ALL SELECT ...`   | Combine all results (with duplicates) | `SELECT id FROM users UNION ALL SELECT id FROM deleted_users` |

---

### ✅ Example JSON Templates for UNION Queries

#### 1. Basic UNION (Remove Duplicates)
```json
{
  "queryType": "UNION",
  "unionType": "UNION",
  "queries": [
    {
      "queryType": "SELECT",
      "tableName": "users",
      "selectedColumns": [
        { "expression": "id" }
      ],
      "conditions": [
        { "fieldName": "active", "value": "true", "operator": "EQUALS" }
      ]
    },
    {
      "queryType": "SELECT",
      "tableName": "admins",
      "selectedColumns": [
        { "expression": "id" }
      ]
    }
  ]
}
```

#### 2. UNION ALL (Keep Duplicates)
```json
{
  "queryType": "UNION",
  "unionType": "UNION_ALL",
  "queries": [
    {
      "queryType": "SELECT",
      "tableName": "users",
      "selectedColumns": [
        { "expression": "email" }
      ]
    },
    {
      "queryType": "SELECT",
      "tableName": "deleted_users",
      "selectedColumns": [
        { "expression": "email" }
      ]
    }
  ]
}
```

---

## 📦 Supported Window Function Queries

---

### 🧾 Query Type Reference Table for Window/Analytical Queries

| Query Type                                     | Description                             | Example |
|------------------------------------------------|-----------------------------------------|---------|
| `RANK() OVER (...)`                            | Ranks rows within a partition           | `SELECT name, RANK() OVER (PARTITION BY dept ORDER BY salary DESC) AS rank FROM employees` |
| `ROW_NUMBER() OVER (...)`                      | Assigns a unique row number             | `SELECT name, ROW_NUMBER() OVER (ORDER BY created_at) FROM users` |
| `DENSE_RANK() OVER (...)`                      | Like RANK but without gaps              | `SELECT name, DENSE_RANK() OVER (PARTITION BY region ORDER BY sales DESC) FROM reps` |
| `AVG(col) OVER (...)`, `SUM(...) OVER (...)`   | Rolling aggregates                      | `SELECT id, AVG(score) OVER (PARTITION BY class_id) FROM results` |

---

### ✅ Example JSON Templates for Window Functions

#### 1. RANK within Departments
```json
{
  "queryType": "SELECT",
  "tableName": "employees",
  "selectedColumns": [
    { "expression": "name" },
    {
      "expression": "RANK() OVER (PARTITION BY department_id ORDER BY salary DESC)",
      "alias": "rank_in_dept"
    }
  ]
}
```

#### 2. ROW_NUMBER with Ordering
```json
{
  "queryType": "SELECT",
  "tableName": "users",
  "selectedColumns": [
    { "expression": "id" },
    { "expression": "ROW_NUMBER() OVER (ORDER BY created_at)", "alias": "row_num" }
  ]
}
```

#### 3. Aggregates Over Window
```json
{
  "queryType": "SELECT",
  "tableName": "results",
  "selectedColumns": [
    { "expression": "student_id" },
    {
      "expression": "AVG(score) OVER (PARTITION BY subject_id)",
      "alias": "avg_score_per_subject"
    }
  ]
}
```

---

# 📦 Supported SUBQUERY Query Types

---

## 🧾 Query Type Reference Table for SUBQUERIES

| Query Type                                  | Description                                      | Example |
|---------------------------------------------|--------------------------------------------------|---------|
| `SELECT ... FROM (SELECT ...) AS alias`     | Subquery used as a table in FROM clause         | `SELECT * FROM (SELECT * FROM users WHERE active = 1) AS active_users` |
| `SELECT (SELECT ...) AS alias`              | Subquery in SELECT column                       | `SELECT name, (SELECT COUNT(*) FROM orders o WHERE o.user_id = u.id) AS order_count FROM users u` |
| `SELECT ... WHERE col IN (SELECT ...)`      | Subquery in WHERE clause                        | `SELECT * FROM users WHERE id IN (SELECT user_id FROM orders WHERE status = 'PAID')` |

---

## 🧱 Key Components

### 1. `DatabaseConfig`
Defines connection info for external DBs. Used for pooling and client mapping.

### 2. `QueryTemplate`
Stores SQL query templates in MongoDB with:
- Table name
- Selected columns
- Conditions
- Group by / Having
- Order by
- Limit & Offset

### 3. `SelectedColumn`
Defines a selected expression and optional alias.
- Expression: `SUM(amount)`
- Alias: `total_sales`

### 4. `QueryCondition`
Defines dynamic WHERE or HAVING conditions using:
- Field name
- Operator (EQUALS, GREATER_THAN, IN, LIKE...)
- Value
- Filter operator (AND, OR)

### 5. `SortDirection`
Enum to enforce `ASC` or `DESC` direction in `orderBy`.

### 6. `QueryParamCaster`
Casts all condition values based on SQL column types (using table metadata).

### 7. `TableMetadataManager`
Caches column types for each table using hashed DB config key.
Automatically refreshes schema metadata on failures.

### 8. `DatabaseConnectionPoolManager`
Manages lazy-loading and caching of `DataSource` using HikariCP + Caffeine.

### 9. `SelectQueryExecutionStrategy`
Dynamically generates SQL, builds param map, retries on schema mismatch.


### 10. `JoinConfig`
Defines the relationship between two tables:

- `joinType`: INNER, LEFT, RIGHT, CROSS
- `table`: Table to be joined
- `alias`: (Optional) Alias for the table
- `onCondition`: Condition for the join

```java
@Getter
@Setter
public class JoinConfig {
    private String joinType; // INNER, LEFT, RIGHT, CROSS
    private String table;    // e.g., "customers"
    private String alias;    // optional alias for readability
    private String onCondition; // e.g., "orders.customer_id = c.id"
}
```

### 11. `CountConfig`
- **SelectedColumn**: Uses `expression: "COUNT(*)"` with optional `alias`.
- **QueryCondition**: Allows filtering rows before counting.
- **Group By**: Used for grouped counts (`COUNT(*) GROUP BY col`).
- **JoinConfig**: Allows `COUNT(*)` across relationships.
- **Validation**: Ensures count queries don’t use meaningless LIMIT/OFFSET unless explicitly intended.

## 12. Key Components for Advanced SELECT

- **SelectedColumn**: Supports `DISTINCT`, `CASE WHEN`, `JSON_OBJECT`, and raw SQL expressions.
- **distinct**: Boolean flag to prepend `SELECT DISTINCT` automatically.
- **CteBlock**: Holds name + raw query for CTEs (`WITH ...` clause).
- **Expression Flexibility**: You can use SQL functions, subqueries, and case logic in column expressions.

## 14. Key Components for UNION Support

- **queryType: "UNION"**: Marks a union-based query.
- **unionType**: Either `"UNION"` (distinct results) or `"UNION_ALL"` (includes duplicates).
- **queries**: List of individual SELECT queries to union.
- Each query must have the **same number and type of selected columns** to be compatible.

## 15. Key Components for Window Support

- **SelectedColumn**: SQL expression like `RANK() OVER (...)`, optional `alias`.
- **Partitioning / Ordering**: Handled inside the expression manually by the user.
- **No change to SQL builder**: It passes the expression as-is to the final SQL.

## 16 Key Components for SUBQUERY Support

- **SubqueryBlock**: Contains `name` and `query` to define subqueries.
- **tableName**: Can reference a subquery name defined in `subqueries`.
- **SelectedColumn**: Supports expressions like `(SELECT COUNT(...))`.
- **QueryCondition**: Allows raw SQL value such as `(SELECT ...)` when paired with `IN`, `EQUALS`, etc.


---

## ⚙️ How it Works
1. Fetch `QueryTemplate` + `DatabaseConfig`
2. Use `TableMetadataManager` to get column types
3. Validate and cast all values using `QueryParamCaster`
4. Build SQL query (SELECT ... WHERE ... GROUP BY ... HAVING ... ORDER BY ... LIMIT ... OFFSET)
5. Execute using `NamedParameterJdbcTemplate`
6. Retry if schema fails, after cache invalidation


## ⚙️ How Joins Work in the Engine

1. **Define the primary table** using `fromTable`.
2. **Specify joins** with `joins: List<JoinConfig>`.
3. **Select columns** with optional aliasing.
4. **Ensure all join conditions are provided**, including the type (`INNER`, `LEFT`, `RIGHT`, `CROSS`) and `onCondition`.


## ⚙️ How COUNT Works in the Engine

1. **SelectedColumn** must include a valid `COUNT(*)` or `COUNT(col)` expression.
2. Optional `alias` improves clarity and allows referencing in result.
3. `conditions` are applied **before** counting rows.
4. `JOIN` clauses are resolved first if present.
5. Optional `groupBy` causes the engine to return grouped counts.


## ⚙️ How Advanced SELECT Works in the Engine

1. `distinct` flag adds `SELECT DISTINCT`.
2. `expression` in `SelectedColumn` supports:
    - raw columns
    - aggregate functions (e.g., COUNT, SUM)
    - conditional logic (`CASE WHEN`)
    - JSON serialization (`JSON_OBJECT`)
3. CTEs (via `ctes`) are prepended using `WITH name AS (...)`.
4. SQL is safely built using metadata, typed casting, and clean validations.


## ⚙️ How UNION Works in the Engine

1. Union queries are wrapped in a parent structure with `queryType: "UNION"`.
2. The engine ensures that all child queries select compatible column types.
3. SQL is built by concatenating each SELECT with `UNION` or `UNION ALL`.
4. You can apply conditions, joins, etc., within each child query individually.


## ⚙️ How Window Functions Work in the Engine

1. User writes full window expression in `SelectedColumn.expression`.
2. Alias is optional but recommended for result readability.
3. Engine treats window expressions like any other expression.
4. No special validation is enforced — DB engine handles it.


## ⚙️ How SUBQUERY Works in the Engine

1. **FROM Subqueries**: Rendered before main query using alias name.
2. **SELECT Subqueries**: Inline within selected column expressions.
3. **WHERE Subqueries**: Treated as raw values inside conditions (`value = '(SELECT ...)'`).
4. **Validation** ensures subquery alias uniqueness and query completeness.

---

## ✅ DB Compatibility

- MySQL 8+
- PostgreSQL
- Oracle
- SQL Server
- MariaDB
- SQLite 3.25+

---

## 🛠 Tech Stack
- Java 17+
- Spring Boot
- Spring Data MongoDB
- JDBC + NamedParameterJdbcTemplate
- HikariCP
- Caffeine Cache
- Apache Commons Codec (for hashing)

---

## 📌 Next Features
- [ ] JOIN query support
- [ ] Subquery support (controlled)
- [ ] Auditing & query logging
- [ ] Admin dashboard for cache control
- [ ] Query-level rate limiting & ACL
- [ ] Export result to CSV/Excel

---

## 📬 Contributing
PRs welcome! Please fork and submit with good commit messages.

---

## 🧠 Author
Made with ❤️ by Vishal Gupta — building clean, scalable data engines.

