# Square Social

A client-server Java application for managing a social user database over TCP sockets, backed by MySQL.

## Overview

On startup, the application prompts you to launch as either a **Server** or a **Client**. The server connects to a MySQL database and listens for incoming client connections on port `1234`. Clients connect to the server and perform CRUD operations on the `Users` table through an interactive menu. Multiple clients can connect simultaneously — each is handled on its own thread.

## Prerequisites

- Java 25+
- MySQL (database must already be running)

## Setup

### 1. Database

> For full installation instructions, see `db_template/mysql-setup-and-import-guide.pdf`.

**Install MySQL Server** (version 8.1+) from the [official download page](https://dev.mysql.com/downloads/mysql/). During the Configurator, set the connection port to `3306` and set a root password — keep it somewhere safe.

Verify the server is running:

```bash
mysql -u root -p
```

If the `mysql` command is not found on Windows, add `C:\Program Files\MySQL\MySQL Server 9.x\bin` to your system PATH.

**Create a database and import the schema:**

```bash
# Inside the MySQL prompt:
CREATE DATABASE your_database_name;
EXIT;

# Back in your terminal:
mysql -u root -p your_database_name < db_template/square_social.sql
```

You can name the database anything — just make sure it matches the `DB_URL` you set in `.env` (see step 2).

Verify the import worked:

```sql
USE your_database_name;
SHOW TABLES;        -- should show Users
DESCRIBE Users;     -- should show all columns
```

Prefer a GUI? MySQL Workbench (available at the [Workbench download page](https://dev.mysql.com/downloads/workbench/)) can import the dump via **Server → Data Import → Import from Self-Contained File**.

### 2. Environment Variables

Create a `.env` file in the project root with the following keys:

```
DB_URL=jdbc:mysql://localhost:3306/square
DB_USERNAME=your_username
DB_PASSWORD=your_password
```

## Running

The easiest way is to run `Main.java` directly from your IDE (IntelliJ is already configured). Alternatively, compile and run from the terminal:

```bash
export JAVA_HOME=/usr/lib/jvm/java-26-openjdk  # — this is for linux/unix based operating systems, if you're on Windows, then it's different.
mvn compile
java -cp "target/classes:/home/<user>/.m2/repository/com/mysql/mysql-connector-j/9.5.0/mysql-connector-j-9.5.0.jar" org.square.Main
```

When prompted, enter `S` to start the server or `C` to connect as a client.

## Client Commands

| Option | Action                                | Input                                              |
|--------|---------------------------------------|----------------------------------------------------|
| 1      | Create a user                         | `UserId, FirstName, LastName, Email, Age, DisplayName` (leave UserId blank to auto-assign) |
| 2      | Read all users                        | —                                                  |
| 3      | Read one user                         | `UserId`                                           |
| 4      | Update a user field                   | `UserId, ColumnName, NewValue`                     |
| 5      | Delete a user                         | `UserId`                                           |
| 6      | Filter users by column                | `ColumnName, Value` (supports `%` wildcards, e.g. `FirstName, %john%`) |
| 7      | Exit                                  | —                                                  |

## Authors

- Mason Doti
- Ethan Dornian
- Peter Gane
- Ruby Hennig
