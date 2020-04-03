# Setting up local run environment

This is useful for development and debugging purposes.

All commands below are provided for IntelliJ IDEA running on Ubuntu. Prior to running them, clone the repository and open in IntelliJ IDEA.

To learn more about some variables referenced here, see [instructions to run the pipeline on the cluster](import/running-the-import.md)

## 1. Install and configure Java 8
By default, IDEA will use Java 11 on most modern systems. However, this project can only run on Java 8. If you don't have it, install it:
 ```bash
 sudo apt install openjdk-8-jdk openjdk-8-jre
```

In IDEA, open File → Project Structure. Change the “Project SDK” to Java 8.

## 2. Configure Maven
* Open File → Settings → Build, Execution, Deployment → Build Tools → Maven and change the location of the “User settings file” to `settings.xml` in the root of the repository. To change the setting, you will need to enable the “Override” checkbox on the right.
* In the `settings.xml` file, fill in `ORACLE_SSO_USERNAME` and `ORACLE_SSO_PASSWORD` (see above).
* In IDEA, right click on the project → Maven → Reimport. Wait for the process to complete.
* Right click on the project → Maven → Generate sources and update folders.

## 3. Create and configure local Postgres database

### Method 1: system installation
```bash
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
sudo systemctl enable postgresql
sudo -u postgres psql postgres
\password postgres
```

### Method 2: run with Docker
```bash
docker run --name postgres -p 5432:5432 -e POSTGRES_PASSWORD=00000000 -d postgres
```

Type `exit`.

## 4. Configure test profiles

In `metadata-load/src/test/resources/application.properties`, set the following variables:
* `ena.datasource.url`
* `ena.datasource.username`
* `ena.datasource.password`

To run the tests, open View → Tool Windows → Maven. Then open a project and click Lifecycle → test. To run tests for metadata-load, you need to be either on EBI premises or connected to its network via VPN or SSH.

## 5. Configure run profiles

The run profiles are useful for running import pipeline or the metadata webservice and debugging them.

### `metadata-load`

In `metadata-load/src/main/resources/application.properties`, set the following variables:
* `ena.datasource.url`, `ena.datasource.username`, `ena.datasource.password` — same as for testing profiles
* `metadata.datasource.url=jdbc:postgresql://localhost:5432/postgres`
* `metadata.datasource.username=postgres`
* `metadata.datasource.password` — your local Postgres password
* `entrez.api.key`

Add lines to this file:
```
spring.jpa.hibernate.ddl-auto=create
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.generate-ddl=true
```
They will ensure that the local database schema and contents are erased with each import run.

### `metadata-ws`

In `metadata-ws/src/main/resources/application.properties`, set the following variables:
* `spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password`
* `spring.jpa.hibernate.ddl-auto=create`

Finally, add run configurations in IDEA for metadata-load and metadata-ws modules.

Once the environment has been set up, it makes sense to save all of the modifications as a Git stash to avoid applying them again:
```bash
git stash save 'Local run configuration'
```

The stash can be applied without losing it with:
```bash
git stash apply stash@{0}
```