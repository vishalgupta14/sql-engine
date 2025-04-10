# 🚀 SQL Engine API Documentation

This document provides REST API details for managing:
- `DatabaseConfig` (for external SQL connections)
- `QueryTemplate` (for SQL query definitions)

All endpoints are `WebFlux`-based (non-blocking) and return JSON.

---

## 📁 DatabaseConfigController

### ✅ POST /api/database-config
Create a new database config.

```bash
curl -X POST http://localhost:8080/api/database-config \
  -H "Content-Type: application/json" \
  -d '{
    "databaseConnectionName": "client-db",
    "channel": "SQL",
    "provider": "MySQL",
    "isActive": true,
    "config": {
      "url": "jdbc:mysql://localhost:3306/test",
      "username": "root",
      "password": "password",
      "driverClassName": "com.mysql.cj.jdbc.Driver"
    }
  }'
```

---

### ✅ GET /api/database-config/{id}
Fetch config by ID.

```bash
curl http://localhost:8080/api/database-config/64f3eaa349f8a21ef5e0db9a
```

---

### ✅ GET /api/database-config/client/{clientName}
Fetch config by connection name.

```bash
curl http://localhost:8080/api/database-config/client/client-db
```

---

### ✅ GET /api/database-config
List all configs.

```bash
curl http://localhost:8080/api/database-config
```

---

### ✅ PUT /api/database-config/{id}
Update a config by ID.

```bash
curl -X PUT http://localhost:8080/api/database-config/64f3eaa349f8a21ef5e0db9a \
  -H "Content-Type: application/json" \
  -d '{
    "databaseConnectionName": "client-db",
    "channel": "SQL",
    "provider": "MySQL",
    "isActive": true,
    "config": {
      "url": "jdbc:mysql://localhost:3306/test",
      "username": "root",
      "password": "newpass",
      "driverClassName": "com.mysql.cj.jdbc.Driver"
    }
  }'
```

---

### ✅ DELETE /api/database-config/{id}
Delete config by ID.

```bash
curl -X DELETE http://localhost:8080/api/database-config/64f3eaa349f8a21ef5e0db9a
```

---

## 📁 QueryTemplateController

### ✅ POST /api/query-template
Create a new query template.

```bash
curl -X POST http://localhost:8080/api/query-template \
  -H "Content-Type: application/json" \
  -d '{
    "templateName": "select_users",
    "queryType": "SELECT",
    "tableName": "users",
    "selectedColumns": [
      { "expression": "id" },
      { "expression": "name" }
    ],
    "limit": 100,
    "offset": 0
  }'
```

---

### ✅ GET /api/query-template/{id}
Fetch template by ID.

```bash
curl http://localhost:8080/api/query-template/65f4a1e229e3d91be8abcf90
```

---

### ✅ GET /api/query-template/name/{templateName}
Fetch template by name.

```bash
curl http://localhost:8080/api/query-template/name/select_users
```

---

### ✅ GET /api/query-template
List all templates.

```bash
curl http://localhost:8080/api/query-template
```

---

### ✅ PUT /api/query-template/{id}
Update a query template.

```bash
curl -X PUT http://localhost:8080/api/query-template/65f4a1e229e3d91be8abcf90 \
  -H "Content-Type: application/json" \
  -d '{
    "templateName": "select_users",
    "queryType": "SELECT",
    "tableName": "users",
    "selectedColumns": [
      { "expression": "id" },
      { "expression": "email", "alias": "user_email" }
    ],
    "limit": 50,
    "offset": 10
  }'
```

---

### ✅ DELETE /api/query-template/{id}
Delete query template by ID.

```bash
curl -X DELETE http://localhost:8080/api/query-template/65f4a1e229e3d91be8abcf90
```

---

## 📁 QueryController

### ✅ POST /query/run
Execute a query template against a configured database.

**Request Body Example:**

```json
{
  "templateId": "65f4a1e229e3d91be8abcf90",
  "databaseConfigId": "64f3eaa349f8a21ef5e0db9a",
  "overrideConditions": [
    {
      "fieldName": "status",
      "operator": "=",
      "value": "ACTIVE"
    }
  ]
}
```

**Curl Example:**

```bash
curl -X POST http://localhost:8080/query/run   -H "Content-Type: application/json"   -d '{
    "templateId": "65f4a1e229e3d91be8abcf90",
    "databaseConfigId": "64f3eaa349f8a21ef5e0db9a",
    "overrideConditions": [
      {
        "fieldName": "status",
        "operator": "=",
        "value": "ACTIVE"
      }
    ]
  }'
```

---

### ✅ GET /query/table-schema

Returns column metadata for a table using a `DatabaseConfig`. Accepts either a `templateId` or a `tableName`.

**Query Parameters:**
- `configId` (required): ID of the database config
- `templateId` (optional): ID of the query template (preferred)
- `tableName` (optional): Direct table name if not using template

**Curl Example with Template ID:**

```bash
curl "http://localhost:8080/query/table-schema?configId=64f3eaa349f8a21ef5e0db9a&templateId=65f4a1e229e3d91be8abcf90"
```

**Curl Example with Table Name:**

```bash
curl "http://localhost:8080/query/table-schema?configId=64f3eaa349f8a21ef5e0db9a&tableName=users"
```

**Response Example:**

```json
{
  "table": "users",
  "columns": [
    { "name": "id", "type": "INT", "size": 11, "nullable": false },
    { "name": "email", "type": "VARCHAR", "size": 255, "nullable": true }
  ]
}
```


---

## 🛡️ Notes

- All requests and responses use JSON.
- `selectedColumns` may include optional `alias`.
- Query templates support CTEs, joins, group by, having, and unions.
- All requests and responses use JSON.
- Query execution supports `CTEs`, `joins`, `group by`, `having`, `limit`, `offset`, and `unions`.
- Metadata and schema extraction works for both R2DBC and JDBC fallback.

---

📘 Built using Spring WebFlux & Reactive MongoDB  
🧠 API auto-validates dangerous SQL expressions (e.g., DROP, DELETE, etc.)  
🚀 Reactive and scalable by design.
