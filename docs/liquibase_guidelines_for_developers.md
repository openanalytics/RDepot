Before introducing any change in database - always create backup file! In the container with database run the following command:
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

Liquibase updates database on the base of changeLog.xml. Changelogs can be written in XML, SQL, YAML or JSON format. 
See https://docs.liquibase.com/concepts/basic/changelog.html.

Every changelog file contains a group of changesets (one or many changesets).
Changeset is a basic unit composed of:
- author
- id
- commands to be executed

Additionally, it can include rollback procedure, comments and other things. 
See https://docs.liquibase.com/concepts/basic/changeset.html for further information.

Example of changelog written in xml:

```xml
<databaseChangeLog 
  xmlns="http://www.liquibase.org/xml/ns/dbchangelog" 
    xmlns:ext="http://www.liquibase.org/xml/ns/dbchangelog-ext" 
    xmlns:pro="http://www.liquibase.org/xml/ns/pro" 
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog-ext 
    http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd 
    http://www.liquibase.org/xml/ns/pro 
    http://www.liquibase.org/xml/ns/pro/liquibase-pro-4.0.xsd 
    http://www.liquibase.org/xml/ns/dbchangelog 
    http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.0.xsd
    ">
    
    <changeSet  id="create-table-1"  author="example-admin">  
        <createTable  tableName="example">  
            <column  name="id"  type="integer"/>  
            <column  name="name"  type="text"/>  
        </createTable>
        <rollback/> (Many Change Types such as create table, rename column, and add column can automatically create rollback statements.)
        <comment>create table (here can be placed change description)</comment> 
    </changeSet>
    
</databaseChangeLog>
```
Both rollback and comment are optional. However, it's a good practice to create a rollback statement everytime.

See more: https://docs.liquibase.com/concepts/basic/xml-format.html
https://docs.liquibase.com/workflows/liquibase-community/using-rollback.html

And its distinct equivalent written in SQL - important: formatted SQL files use comments to provide Liquibase with metadata.
```sql
--liquibase formatted sql (each SQL file must begin with this comment)

--changeset example-admin:create-table-1 (example-admin is the author of this changeset and create-table-1 is the id)
CREATE TABLE example (
	id integer NOT NULL,
    name text NOT NULL
);    
--rollback DROP TABLE example; (rollback command)
--comment: create table (here can be placed a descriptive command)
```
See more: https://docs.liquibase.com/concepts/basic/sql-format.html

Most common liquibase commands are:
- [tag <tag>](https://docs.liquibase.com/commands/community/tag.html) - marks the current database state so you can roll back changes in the future
- [update](https://docs.liquibase.com/commands/community/update.html) - deploys any changes that are in the changelog file and that have not been deployed to your database yet
- [rollback <tag>](https://docs.liquibase.com/commands/community/rollbackbytag.html) - rolls back changes made to the database based on the specified tag
- [diff](https://docs.liquibase.com/commands/community/diff.html) - allows you to compare two databases of the same type, or different types, to one another

See other commands: https://docs.liquibase.com/commands/home.html

Liquibase has its own CLI - https://docs.liquibase.com/tools-integrations/cli/home.html

RDepot supports Liquibase too. 
Liquibase commands can be executed with Gradle from the root project directory. 
For example:
```groovy
gradle liquibase update 
```
```groovy
gradle liquibase tag -PliquibaseCommandValue=versionOne
```
```groovy
gradle liquibase rollback -PliquibaseCommandValue=versionOne 
```
Properties for **liquibase** Gradle Task are configured in **liquibase.properties** file in the root project directory.
This Task **does not use** all available [properties](https://docs.liquibase.com/workflows/liquibase-community/creating-config-properties.html). Therefore, when you need to use another property - firstly add it to *liquibase.properties*, then use this property in the Task.

It is also possible to deploy changes at the start of RDepot. There is a **liquibase-changeLog.xml** file in *app/src/main/resources* which actually points to files in the *app/src/main/resources/schema* directory. 

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
By default it is switched on (see above - *enabled* property).
