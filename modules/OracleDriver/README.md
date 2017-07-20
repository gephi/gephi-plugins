## Oracle Driver

This SQLDriver implementation allows Gephi to use Oracle data.

 **No Oracle code is packaged in this module**: it locates the Oracle JDBC drivers using 2 environment variables:-

1. $JDBC_PATH  - a path of directories containing JDBC JAR files: any JAR files matching the file pattern "ojdbc\*.jar" is considered a  valid Oracle JDBC JAR;
2. $ORACLE_HOME - the usual Oracle environment variable specifying the location of the Oracle client or Instant Client. 


This plugin has been tested under Windows (Windows 7) and OSX (El Capitan)

## MacOS Problems
Exporting environment variables which are visible to GUI applications under OSX seems to be a non-trivial task; this approach was attempted and failed: running the application from the command line works    

### Failed 

```
stumacbook:Downloads sturton$ defaults write   ~/.MacOSX/environment.plist  ORACLE_HOME -string "$ORACLE_HOME" 
stumacbook:Downloads sturton$ defaults write   ~/.MacOSX/environment.plist  JDBC_PATH -string "$ORACLE_HOME" 
stumacbook:Downloads sturton$ defaults read   ~/.MacOSX/environment.plist  
{
    "JDBC_PATH" = "/Users/sturton/Developer/instantclient_11_2";
    "ORACLE_HOME" = "/Users/sturton/Developer/instantclient_11_2";
}
stumacbook:Downloads sturton$ export TWO_TASK=192.168.0.23:1521/orcl
stumacbook:Downloads sturton$ sqlplus system/oracle

SQL*Plus: Release 11.2.0.4.0 Production on Thu Mar 3 19:16:50 2016

Copyright (c) 1982, 2013, Oracle.  All rights reserved.


Connected to:
Oracle Database 11g Enterprise Edition Release 11.2.0.2.0 - Production
With the Partitioning, OLAP, Data Mining and Real Application Testing options

SQL> 
```

### Succeeded  

```
stumacbook:OracleDriver sturton$  /Applications/Gephi.app/Contents/MacOS/gephi 
```

## Importing From Oracle 

The queries below may be used to import the dependencies between database objects in the Oracle Developer Day VM example schemas 

### Node Query

```
SELECT  	 
DISTINCT 	 
dbms_utility.get_hash_value(owner||'~'||object_type||'~'||object_name, 1000000, 4194304  ) id 	
,CASE WHEN object_type LIKE  'JAVA%' 
  THEN dbms_java.longname (object_name) 
  ELSE object_name 
  END label 	
,owner 	
, object_type   	
,COUNT(*) OVER (PARTITION BY dbms_utility.get_hash_value(owner||'~'||object_type||'~'||object_name, 1000000, 4194304  )) duplication 	
,CASE 	
  WHEN  REGEXP_INSTR(object_type, 'PACKAGE.*|TYPE.*|TABLE|VIEW|TRIGGER|PROCEDURE|FUNCTION') > 0   	
    THEN 'Y' 	
  ELSE 'N'  	
  END IS_BASE 	
,CASE 	
  WHEN REGEXP_INSTR(object_type, 'PROCEDURE|FUNCTION|TRIGGER|TYPE.*|PACKAGE.*') > 0 
  THEN 'PLSQL' 	
  ELSE 'DDL' 	
  END NAMESPACE   
,CASE 	
  WHEN OBJECT_TYPE IN ('TABLE','VIEW','MATERIALIZED VIEW' )  	
    THEN 'http://localhost:9090/DeveloperDay/schemaspy/'||OWNER||'/tables/'||OBJECT_NAME||'.html' 	
  WHEN  REGEXP_INSTR(OBJECT_TYPE, 'PROCEDURE|FUNCTION|TRIGGER') > 0   
    THEN 'http://localhost:9090/DeveloperDay/pldoc/' || '_' || OWNER ||'.html#' || LOWER(OBJECT_NAME) 	
  WHEN  REGEXP_INSTR(OBJECT_TYPE, 'PACKAGE.*|TYPE.*') > 0   	
    THEN 'http://localhost:9090/DeveloperDay/pldoc/' || OBJECT_NAME ||'.html' 	
  WHEN  REGEXP_INSTR(OBJECT_TYPE, 'PACKAGE BODY|TYPE BODY') > 0   
    THEN 'http://localhost:9090/DeveloperDay/pldoc/_' || OBJECT_NAME ||'.html' 	
  ELSE ' '  	END DOCUMENTATION_URL   
FROM DBA_OBJECTS_AE 	
WHERE REGEXP_LIKE(owner , '^PLS$|^HR$|^OE$|^DEMO$') 	
AND INSTR(object_name, '$') = 0  
ORDER BY id
```

### Edge Query

```
SELECT 
dbms_utility.get_hash_value(owner||'~'||type||'~'||name, 1000000, 4194304  ) source
,dbms_utility.get_hash_value(referenced_owner||'~'||referenced_type||'~'||referenced_name, 1000000, 4194304  ) target
,dependency_type 
from DBA_DEPENDENCIES 
WHERE REGEXP_LIKE(owner , '^PLS$|^HR$|^OE$|^DEMO$' ) 
AND REGEXP_LIKE(referenced_owner , '^PLS$|^HR$|^OE$|^DEMO$' ) 
```
