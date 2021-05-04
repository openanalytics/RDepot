Before doing any version upgrade of RDepot - always create backup file! In the container with database run the following command:
**pg_dump rdepot > rdepot_backup.sql**
To restore data:
**psql rdepot < rdepot_backup.sql**
It's especially important to create backup when changes may lead to data loss.
Examples of such changes:
```sql
DROP TABLE my table;
ALTER TABLE my_table DROP COLUMN my_col;
```
Without backup file it's impossible to restore data.


RDepot uses [Liquibase](https://docs.liquibase.com) for database change management.
It is configured with a property in *application.yaml* file:
```yaml
spring:
  liquibase:
    change-log: classpath:liquibase-changeLog.xml
    url: jdbc:postgresql://oa-rdepot-db:5432/rdepot
    user: rdepot
    password: mysecretpassword
    enabled: true
```
If you want to disable automatic database change management in RDepot (through Liquibase), change *enabled* property to *false*.
*liquibase-changeLog.xml* contains the set of changes in the database schema and it is included in every RDepot release, starting from version 1.5.0
