package org.bitnine.importer;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.graph.api.TimeFormat;
import org.gephi.io.database.drivers.SQLUtils;
import org.gephi.io.importer.api.ColumnDraft;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.Database;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.ElementDraft;
import org.gephi.io.importer.api.Issue;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.io.importer.api.PropertiesAssociations;
import org.gephi.io.importer.api.PropertiesAssociations.EdgeProperties;
import org.gephi.io.importer.api.PropertiesAssociations.NodeProperties;
import org.gephi.io.importer.api.Report;
import org.gephi.io.importer.spi.DatabaseImporter;

/**
 *
 * @author dehowefeng
 */
public class ImporterAgensGraph implements DatabaseImporter {

    private Report report;
    private AgensGraphDatabaseImpl database;
    private ContainerLoader container;
    private Connection connection;
    //TempData
    private String timeIntervalStart;
    private String timeIntervalEnd;

    @Override
    public boolean execute(ContainerLoader container) {
        //Logger.getLogger(ImporterAgensGraph.class.getName()).log(Level.INFO, "execute() called");
        this.container = container;
        this.report = new Report();
        try {
            importData();
        } catch (Exception e) {
            close();
            throw new RuntimeException(e);
        }
        close();
        return true;
    }

    private void close() {
        //Close connection
        if (connection != null) {
            try {
                connection.close();
                report.log("Database connection terminated");
            } catch (Exception e) {
                /* ignore close errors */ }
        }
    }

    private void importData() throws Exception {
        //Connect database
        //Logger.getLogger(ImporterAgensGraph.class.getName()).log(Level.INFO, "importData() called");
        String url = SQLUtils.getUrl(database.getSQLDriver(), database.getHost(), database.getPort(), database.getDBName());
        try {
            report.log("Try to connect at " + url);
            connection = database.getSQLDriver().getConnection(url, database.getUsername(), database.getPasswd());
            report.log("Database connection established");
        } catch (SQLException ex) {
            if (connection != null) {
                try {
                    connection.close();
                    report.log("Database connection terminated");
                } catch (Exception e) {
                    /* ignore close errors */ }
            }
            report.logIssue(new Issue("Failed to connect at " + url, Issue.Level.CRITICAL, ex));
        }

        if (connection == null) {
            report.logIssue(new Issue("Failed to connect at " + url, Issue.Level.CRITICAL));
        }

        report.log(database.getPropertiesAssociations().getInfos());

        setGraphPath(connection);
        getNodes(connection);
        getEdges(connection);
        getNodesAttributes(connection);
        getEdgesAttributes(connection);
    }

    private void setGraphPath(Connection connection) throws SQLException {
        Logger.getLogger(ImporterAgensGraph.class.getName()).log(Level.INFO, "setGraphPath called");

        String graphPathQuery = "SET graph_path = " + database.getGraphPath() + ";";

        // Logger.getLogger(ImporterAgensGraph.class.getName()).log(Level.INFO, graphPathQuery);
        try (Statement s = connection.createStatement()) {
            s.execute(graphPathQuery);
            //Logger.getLogger(ImporterAgensGraph.class.getName()).log(Level.INFO, "Graph Path Set");

        } catch (SQLException ex) {
            report.logIssue(new Issue("Failed to set graph path with query", Issue.Level.SEVERE, ex));
        }
    }

    private void getNodes(Connection connection) throws SQLException {

        //Factory
        ElementDraft.Factory factory = container.factory();

        //Properties
        PropertiesAssociations properties = database.getPropertiesAssociations();

        try (Statement s = connection.createStatement(); ResultSet rs = s.executeQuery(database.getNodeQuery())) {
            findNodeAttributesColumns(rs);
            ResultSetMetaData metaData = rs.getMetaData();
            int columnsCount = metaData.getColumnCount();
            while (rs.next()) {
                String id = null;
                for (int i = 0; i < columnsCount; i++) {
                    String columnName = metaData.getColumnLabel(i + 1);
                    NodeProperties p = properties.getNodeProperty(columnName);
                    if (p != null && p.equals(NodeProperties.ID)) {
                        String ide = rs.getString(i + 1);
                        if (ide != null) {
                            id = ide;
                        }
                    }
                }
                NodeDraft node;
                if (id != null) {
                    node = factory.newNodeDraft(id);
                } else {
                    node = factory.newNodeDraft();
                }

                for (int i = 0; i < columnsCount; i++) {
                    String columnName = metaData.getColumnLabel(i + 1);
                    NodeProperties p = properties.getNodeProperty(columnName);
                    if (p != null) {
                        injectNodeProperty(p, rs, i + 1, node);
                    } else {
                        //Inject node attributes
                        ColumnDraft col = container.getNodeColumn(columnName);
                        injectElementAttribute(rs, i + 1, col, node);
                    }
                }
                injectTimeIntervalProperty(node);
                container.addNode(node);
            }
        } catch (SQLException ex) {
            report.logIssue(new Issue("Failed to execute Node query", Issue.Level.SEVERE, ex));
        }
    }

    private void getEdges(Connection connection) throws SQLException {

        //Factory
        ElementDraft.Factory factory = container.factory();

        //Properties
        PropertiesAssociations properties = database.getPropertiesAssociations();

        try (Statement s = connection.createStatement(); ResultSet rs = s.executeQuery(database.getEdgeQuery())) {
            findEdgeAttributesColumns(rs);
            ResultSetMetaData metaData = rs.getMetaData();
            int columnsCount = metaData.getColumnCount();
            while (rs.next()) {
                String id = null;
                for (int i = 0; i < columnsCount; i++) {
                    String columnName = metaData.getColumnLabel(i + 1);
                    EdgeProperties p = properties.getEdgeProperty(columnName);
                    if (p != null && p.equals(EdgeProperties.ID)) {
                        String ide = rs.getString(i + 1);
                        if (ide != null) {
                            id = ide;
                        }
                    }
                }
                EdgeDraft edge;
                if (id != null) {
                    edge = factory.newEdgeDraft(id);
                } else {
                    edge = factory.newEdgeDraft();
                }
                for (int i = 0; i < columnsCount; i++) {
                    String columnName = metaData.getColumnLabel(i + 1);
                    EdgeProperties p = properties.getEdgeProperty(columnName);
                    if (p != null) {
                        injectEdgeProperty(p, rs, i + 1, edge);
                    } else {
                        //Inject edge attributes
                        ColumnDraft col = container.getEdgeColumn(columnName);
                        injectElementAttribute(rs, i + 1, col, edge);
                    }
                }
                injectTimeIntervalProperty(edge);
                container.addEdge(edge);
            }
        } catch (SQLException ex) {
            report.logIssue(new Issue("Failed to execute Edge query", Issue.Level.SEVERE, ex));
        }
    }

    private void getNodesAttributes(Connection connection) throws SQLException {
    }

    private void getEdgesAttributes(Connection connection) throws SQLException {
    }

    private void injectNodeProperty(NodeProperties p, ResultSet rs, int column, NodeDraft nodeDraft) throws SQLException {
        switch (p) {
            case LABEL:
                String label = rs.getString(column);
                if (label != null) {
                    nodeDraft.setLabel(label);
                }
                break;
            case X:
                float x = rs.getFloat(column);
                if (x != 0) {
                    nodeDraft.setX(x);
                }
                break;
            case Y:
                float y = rs.getFloat(column);
                if (y != 0) {
                    nodeDraft.setY(y);
                }
                break;
            case Z:
                float z = rs.getFloat(column);
                if (z != 0) {
                    nodeDraft.setZ(z);
                }
                break;
            case COLOR:
                String color = rs.getString(column);
                if (color != null) {
                    String[] rgb = color.replace(" ", "").split(",");
                    if (rgb.length == 3) {
                        nodeDraft.setColor(rgb[0], rgb[1], rgb[2]);
                    } else {
                        nodeDraft.setColor(color);
                    }
                }
                break;
            case SIZE:
                float size = rs.getFloat(column);
                if (size != 0) {
                    nodeDraft.setSize(size);
                }
                break;
            case START:
                container.setTimeFormat(getTimeFormat(rs, column));
                String start = getDateData(rs, column);
                if (start != null) {
                    timeIntervalStart = start;
                }
                break;
            case START_OPEN:
                container.setTimeFormat(getTimeFormat(rs, column));
                String startOpen = rs.getString(column);
                if (startOpen != null) {
                    timeIntervalStart = startOpen;
                }
                break;
            case END:
                container.setTimeFormat(getTimeFormat(rs, column));
                String end = rs.getString(column);
                if (end != null) {
                    timeIntervalEnd = end;
                }
                break;
            case END_OPEN:
                container.setTimeFormat(getTimeFormat(rs, column));
                String endOpen = rs.getString(column);
                if (endOpen != null) {
                    timeIntervalEnd = endOpen;
                }
                break;
        }
    }

    private TimeFormat getTimeFormat(ResultSet rs, int column) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int type = metaData.getColumnType(column);
        switch (type) {
            case Types.DATE:
                return TimeFormat.DATE;
            case Types.TIME:
                return TimeFormat.DATETIME;
            case Types.TIMESTAMP:
                return TimeFormat.DATETIME;
            case Types.VARCHAR:
                return TimeFormat.DATETIME;
            case Types.DOUBLE:
            case Types.FLOAT:
                return TimeFormat.DOUBLE;
            default:
                break;
        }
        return TimeFormat.DOUBLE;
    }

    private String getDateData(ResultSet rs, int column) throws SQLException {
        String res = null;
        ResultSetMetaData metaData = rs.getMetaData();
        int type = metaData.getColumnType(column);
        if (type == Types.DATE) {
            Date date = rs.getDate(column);
            res = date.toString();
        } else if (type == Types.TIME) {
            Time time = rs.getTime(column);
            res = time.toString();
        } else if (type == Types.TIMESTAMP) {
            Timestamp timeStamp = rs.getTimestamp(column);
            res = timeStamp.toString();
        } else if (type == Types.VARCHAR) {
            res = rs.getString(column);
        } else if (type == Types.DOUBLE || type == Types.FLOAT) {
            Double dbl = rs.getDouble(column);
            res = dbl.toString();
        }
        return res;
    }

    private void injectTimeIntervalProperty(NodeDraft nodeDraft) {
        if (timeIntervalStart != null || timeIntervalEnd != null) {
            nodeDraft.addInterval(timeIntervalStart, timeIntervalEnd);
        }

        //Reset temp data
        timeIntervalStart = null;
        timeIntervalEnd = null;
    }

    private void injectEdgeProperty(EdgeProperties p, ResultSet rs, int column, EdgeDraft edgeDraft) throws SQLException {
        switch (p) {
            case LABEL:
                String label = rs.getString(column);
                if (label != null) {
                    edgeDraft.setLabel(label);
                }
                break;
            case SOURCE:
                String source = rs.getString(column);
                if (source != null && !source.isEmpty()) {
                    NodeDraft sourceNode = container.getNode(source);
                    edgeDraft.setSource(sourceNode);
                }
                break;
            case TARGET:
                String target = rs.getString(column);
                if (target != null && !target.isEmpty()) {
                    NodeDraft targetNode = container.getNode(target);
                    edgeDraft.setTarget(targetNode);
                }
                break;
            case WEIGHT:
                float weight = rs.getFloat(column);
                if (weight != 0) {
                    edgeDraft.setWeight(weight);
                }
                break;
            case COLOR:
                String color = rs.getString(column);
                if (color != null) {
                    String[] rgb = color.split(",");
                    if (rgb.length == 3) {
                        edgeDraft.setColor(rgb[0], rgb[1], rgb[2]);
                    } else {
                        edgeDraft.setColor(color);
                    }
                }
                break;
            case START:
                container.setTimeFormat(getTimeFormat(rs, column));
                String start = getDateData(rs, column);
                if (start != null) {
                    timeIntervalStart = start;
                }
                break;
            case START_OPEN:
                container.setTimeFormat(getTimeFormat(rs, column));
                String startOpen = rs.getString(column);
                if (startOpen != null) {
                    timeIntervalStart = startOpen;
                }
                break;
            case END:
                container.setTimeFormat(getTimeFormat(rs, column));
                String end = rs.getString(column);
                if (end != null) {
                    timeIntervalEnd = end;
                }
                break;
            case END_OPEN:
                container.setTimeFormat(getTimeFormat(rs, column));
                String endOpen = rs.getString(column);
                if (endOpen != null) {
                    timeIntervalEnd = endOpen;
                }
                break;
        }
    }

    private void injectTimeIntervalProperty(EdgeDraft edgeDraft) {
        if (timeIntervalStart != null || timeIntervalEnd != null) {
            edgeDraft.addInterval(timeIntervalStart, timeIntervalEnd);
        }

        //Reset temp data
        timeIntervalStart = null;
        timeIntervalEnd = null;
    }

    private void injectElementAttribute(ResultSet rs, int columnIndex, ColumnDraft column, ElementDraft draft) {
        String elementName;
        if (draft instanceof NodeDraft) {
            elementName = "node";
        } else {
            elementName = "edge";
        }
        Class typeClass = column.getTypeClass();
        if (typeClass.equals(Boolean.class)) {
            try {
                boolean val = rs.getBoolean(columnIndex);
                draft.setValue(column.getId(), val);
            } catch (SQLException ex) {
                report.logIssue(new Issue("Failed to get a BOOLEAN value for " + elementName + " attribute '" + column.getId() + "'", Issue.Level.SEVERE, ex));
            }
        } else if (typeClass.equals(Double.class)) {
            try {
                double val = rs.getDouble(columnIndex);
                draft.setValue(column.getId(), val);
            } catch (SQLException ex) {
                report.logIssue(new Issue("Failed to get a DOUBLE value for " + elementName + " attribute '" + column.getId() + "'", Issue.Level.SEVERE, ex));
            }
        } else if (typeClass.equals(Float.class)) {
            try {
                float val = rs.getFloat(columnIndex);
                draft.setValue(column.getId(), val);
            } catch (SQLException ex) {
                report.logIssue(new Issue("Failed to get a FLOAT value for " + elementName + " attribute '" + column.getId() + "'", Issue.Level.SEVERE, ex));
            }
        } else if (typeClass.equals(Integer.class)) {
            try {
                int val = rs.getInt(columnIndex);
                draft.setValue(column.getId(), val);
            } catch (SQLException ex) {
                report.logIssue(new Issue("Failed to get a INT value for " + elementName + " attribute '" + column.getId() + "'", Issue.Level.SEVERE, ex));
            }
        } else if (typeClass.equals(Long.class)) {
            try {
                long val = rs.getLong(columnIndex);
                draft.setValue(column.getId(), val);
            } catch (SQLException ex) {
                report.logIssue(new Issue("Failed to get a LONG value for " + elementName + " attribute '" + column.getId() + "'", Issue.Level.SEVERE, ex));
            }
        } else if (typeClass.equals(Short.class)) {
            try {
                short val = rs.getShort(columnIndex);
                draft.setValue(column.getId(), val);
            } catch (SQLException ex) {
                report.logIssue(new Issue("Failed to get a SHORT value for " + elementName + " attribute '" + column.getId() + "'", Issue.Level.SEVERE, ex));
            }
        } else if (typeClass.equals(Byte.class)) {
            try {
                byte val = rs.getByte(columnIndex);
                draft.setValue(column.getId(), val);
            } catch (SQLException ex) {
                report.logIssue(new Issue("Failed to get a BYTE value for " + elementName + " attribute '" + column.getId() + "'", Issue.Level.SEVERE, ex));
            }
        } else {
            try {
                String val = rs.getString(columnIndex);
                if (val != null) {
                    draft.setValue(column.getId(), val);
                } else {
                    report.logIssue(new Issue("Failed to get a STRING value for " + elementName + " attribute '" + column.getId() + "'", Issue.Level.WARNING));
                }
            } catch (SQLException ex) {
                report.logIssue(new Issue("Failed to get a STRING value for " + elementName + " attribute '" + column.getId() + "'", Issue.Level.SEVERE, ex));
            }
        }
    }

    private void findNodeAttributesColumns(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnsCount = metaData.getColumnCount();
        for (int i = 0; i < columnsCount; i++) {
            String columnName = metaData.getColumnLabel(i + 1);
            NodeProperties p = database.getPropertiesAssociations().getNodeProperty(columnName);
            if (p == null) {
                //No property associated to this column is found, so we append it as an attribute
                Class typeClass = findTypeClass(metaData, i);
                container.addNodeColumn(columnName, typeClass);
            }
        }
    }

    private void findEdgeAttributesColumns(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnsCount = metaData.getColumnCount();
        for (int i = 0; i < columnsCount; i++) {
            String columnName = metaData.getColumnLabel(i + 1);
            EdgeProperties p = database.getPropertiesAssociations().getEdgeProperty(columnName);
            if (p == null) {
                //No property associated to this column is found, so we append it as an attribute
                Class typeClass = findTypeClass(metaData, i);
                container.addEdgeColumn(columnName, typeClass);
            }
        }
    }

    private Class findTypeClass(ResultSetMetaData metaData, int columnIndex) throws SQLException {
        Class type = String.class;
        switch (metaData.getColumnType(columnIndex + 1)) {
            case Types.BIGINT:
                type = Long.class;
                break;
            case Types.INTEGER:
                type = Integer.class;
                break;
            case Types.TINYINT:
                type = Byte.class;
                break;
            case Types.SMALLINT:
                type = Short.class;
                break;
            case Types.BOOLEAN:
                type = Boolean.class;
                break;
            case Types.FLOAT:
                type = Float.class;
                break;
            case Types.DOUBLE:
                type = Double.class;
                break;
            case Types.VARCHAR:
                type = String.class;
                break;
            case Types.BIT:
                type = Boolean.class;
                break;
            case Types.REAL:
                type = Float.class;
                break;
            default:
                report.logIssue(new Issue("Unknown SQL Type " + metaData.getColumnType(columnIndex + 1) + ", STRING used.", Issue.Level.WARNING));
                break;
        }
        return type;
    }

    @Override
    public void setDatabase(Database database) {
        this.database = (AgensGraphDatabaseImpl) database;
    }

    @Override
    public Database getDatabase() {
        return database;
    }

    @Override
    public ContainerLoader getContainer() {
        return container;
    }

    @Override
    public Report getReport() {
        return report;
    }
}
