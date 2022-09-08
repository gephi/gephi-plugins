## Neo4j plugin

This plugin allows you to import a network from a Neo4j database 4.X.

### Import 

#### Step 1: Neo4j connection 

To import data from Neo4J, you must define how to connect to the Neo4j server.
The plugin uses the official Neo4j driver with neo4j/bolt protocol.

So to create a connection to the database you must fill : 

- Neo4j URL (ex: `neo4j://localhost` )
- Database  : If empty we will use the default database 
- Authentication : Select the authentication mechanism. We support "username/password" and "No authentication" modes.
- Username : Neo4j user with whom you want to connect to the database (ex: `neo4j`)
- Password: Neo4j user's password

You can click on the button *Verify* to test the connection to the database with the provided values. 
If an error occurred, its message will be displayed. 

#### Step 2: Import configuration

There are two import modes available :

- By selecting labels and relationship types 
- By specifying two Cypher queries, one for nodes and the other for edges.

##### By labels and relationship types

On this screen you have two list one for nodes, and the other for edges.
If you select nothing, it's the same as selecting everything. 

So for example, if it's the first time you see this screen, you can click on *Finish*, and the plugin will import the whole Neo4j otherwise.

If you want to import a subset of your Neo4j database, you must provide a selection.
In order to do a multi-selection, just use *ctrl + click*.

NOTE: We only import relationships where node extremities are part from the labels selections. 

##### By queries

On this screen you can define two Cypher queries : 

- One for retrieving nodes  
- The other for edges

With this configuration, you can define precisely what you want to import. 
It's really useful when you want to project your graph (ex: create the colleagues graph  when in your database you have `(:Person)-[:WORK_AT]->(:Company)`  )

The node query must return a field named *id* that is the unique identifier of your node.
Ex: `MATCH (n) RETURN id(n) AS id, labels(n) AS labels`

The edge query must return a field named *id* that is the unique identifier of your edge, but also a *sourceId* & *targetId* that must match an id from the node query
Ex: `MATCH (n)-[r]->(m) RETURN id(r) AS id, type(r) AS type, id(n) AS sourceId, id(m) AS targetId`

