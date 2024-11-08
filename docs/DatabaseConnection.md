# Connecting to the Database

## Before getting anything from the database
A DatabaseConnection object is used to open a connection to the database. Each DatabaseConnection object should be declared and used in a try-with block, or closed manually after use.

```Java
try (DatabaseConnection db = new DatabaseConnection()) {
    /* do stuff with db */
}
```
or
```Java
DatabaseConnection db = new DatabaseConnection();
/* do stuff with db */
db.close();
```

The DatabaseConnection object can send queries to the database and return values in the format ``List<List<T>>``.\
Below is an example of how values are represented in the database compared to how they will be retrieved by the DatabaseConnection object.

#### roleTypes
| roleId | role |
| :--- | :---: |
|1|2|
|3|4|
|5|6|
|7|8|

#### values in Java
```Java
// both column types can be casted as bytes, so we can get both columns without worrying about errors
List<List<byte[]>> results = db.queryBytes("SELECT * FROM roleTypes");
[
    [1, 2],
    [3, 4],
    [5, 6],
    [7, 8]
]
```

## Column Types
Different columns may contain different types- while all can be returned as bytes, some may be better returned as a string, integer, or boolean. Attempting to retrieve columns of a different type than the type requested will raise an Exception, or return malformed data.
```Java
// the role column is of type String, while the roleId column is type byte[32].
List<List<byte[]>> results = db.queryBytes("SELECT roleId FROM roleTypes");
[
    [1],
    [3],
    [5],
    [7]
]
List<List<byte[]>> results = db.queryStrings("SELECT role FROM roleTypes");
[
    [2],
    [4],
    [6],
    [8]
]
```

## Queries
Queries retrieve specific data from the database. Most of these queries should be pre-written and handled in methods, but if any custom ones need to be made and nobody that knows SQL is available, the general format of queries for this project goes as follows:
```SQL
SELECT [column name] FROM [table name]
```