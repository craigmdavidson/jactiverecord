package jactiverecord;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Dao handles interaction with the Database
 * 
 * @author Craig Davidson
 *
 */
public class Dao extends BasicObject {
  private static String databaseUrl;

  public boolean createDatabase(String databaseName) {
    return executeStatement("CREATE DATABASE " + databaseName);
  }
  
  public boolean dropDatabase(String databaseName) {
    return executeStatement("DROP DATABASE " + databaseName);
  }
  
  private boolean executeStatement(String sql) {
    p("Executing: " + sql);
    try (
        Connection connection = DriverManager.getConnection(getDatabaseUrl());
        PreparedStatement statement = connection.prepareStatement(sql);
    ) {
       return statement.execute();
    } catch (SQLException e) {
       throw new ActiveRecordException(e);
    }
  }

  private String getDatabaseUrl() {
    return databaseUrl;
  }

  public static void setDatabaseUrl(String databaseUrl) {
    Dao.databaseUrl = databaseUrl;
  }

  public void createTable(String tableName, Map<String, String> columns) {
    String createTableDDL = new Ddl().buildCreateTableDDL(tableName, columns);
    executeStatement(createTableDDL);
    
  }

  public int executeDml(String query, Object ...parameters){
    p("Executing: " + query + " with " + list(parameters));
    try (
        Connection connection = DriverManager.getConnection(getDatabaseUrl());
        PreparedStatement ps = connection.prepareStatement(query, array("id"));
    ) {
      int i = 1;
      
      for(Object object : parameters) {
        p("Setting parameter " + i + " with " + object);
          ps.setObject(i++, object);
      }
      ps.execute();
      int primaryKey = primaryKeyGeneratedBy(ps);
      if (primaryKey != -1) return primaryKey;
      return ps.getUpdateCount();
    } catch(SQLException e){
      throw new ActiveRecordException(e);
    }
  }

  private int primaryKeyGeneratedBy(PreparedStatement statement) throws SQLException {
    try (ResultSet generatedKeys = statement.getGeneratedKeys();) {
        return generatedKeys.next() ? generatedKeys.getInt(1) : -1;
    }
  }

  public List<Map<String, Object>> findBySql(String query, Object ...parameters) {
    p("Executing: " + query + " with " + list(parameters));
    ResultSet rs = null;
    try (
        Connection connection = DriverManager.getConnection(getDatabaseUrl());
        PreparedStatement ps = connection.prepareStatement(query);
    ) {
        bindQueryParameters(ps, parameters);
        rs = ps.executeQuery();
        List<Map<String, Object>> records = asRecords(rs);
        p(" " + records.size() + " rows returned");
        return records;
    } catch(SQLException | InstantiationException | IllegalAccessException e) {
        throw new ActiveRecordException(query + " -> " + list(parameters), e);
    }
    finally {
      if (rs != null)
    try { rs.close(); } catch (SQLException e) { throw new ActiveRecordException(e); }
    }    
  }
  
  private void bindQueryParameters(PreparedStatement ps, Object[] parameters) throws SQLException {
    for (int i=0; i < parameters.length; i++){
      Object param = parameters[i];
      ps.setObject(i+1, param);
    }
  }
  
  private List<Map<String, Object>> asRecords(ResultSet rs) throws SQLException, InstantiationException, IllegalAccessException {
    List<Map<String, Object>> records = new ArrayList<Map<String, Object>>();
    while(rs.next()) {
        records.add(asDecodedInstance(rs));
    }
    return records;
  }

  private Map<String, Object> asDecodedInstance(ResultSet rs) throws InstantiationException, IllegalAccessException, SQLException {
    Map<String, Object> record = new LinkedHashMap<String, Object>();  
    ResultSetMetaData metaData = rs.getMetaData();
    int columnCount = metaData.getColumnCount();
    for(int i=0; i<columnCount; i++){
      String key = metaData.getColumnName(i+1);
      Object value = rs.getObject(i+1);
      record.put(key, value);
    }
    return record;
  }
}
