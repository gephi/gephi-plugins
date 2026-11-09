# Community Clusters

Gephi layout plugin that gathers each community of a network into its own cluster.
See `MANUAL.md` for usage and a description of every setting.

## Build and run

```bat
cd /d C:\gephidev\gephi-plugins
mvn clean package
mvn org.gephi:gephi-maven-plugin:run
```

The module must be listed in the root `pom.xml`:

```xml
<module>modules/CommunityClusters</module>
```

## Source files

| File | Role |
|---|---|
| `CommunityClusters.java` | The Gephi layout: properties, reading the graph, writing positions |
| `CommunityClustersBuilder.java` | Registers the layout with Gephi |
| `ClusterForceSimulation.java` | The force simulation (pure Java) |
| `CirclePacker.java` | Packs one circle per cluster to give each a home position |
| `Leiden.java` | Community detection (pure Java) |
| `Bundle.properties` | All user-visible text (setting names, descriptions, layout name) |
| `src/test/java/...` | JUnit tests for Leiden, the circle packer and the force simulation |


## Tests

`mvn clean package` runs the unit tests automatically. To run only this module's tests:

```bat
mvn -pl modules/CommunityClusters -am test
```

## License

Apache License 2.0, see `LICENSE`.

## Credits

The layout is inspired by Fierăscu, S. I. (2026). *SoFab: Network analysis for research and practice* (Version 0.9.0) [Computer software].
