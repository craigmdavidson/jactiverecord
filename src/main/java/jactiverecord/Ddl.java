package jactiverecord;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Ddl extends BasicObject {

  public String buildCreateTableDDL(String tableName, Map<String, String> columns) {
    List<String> columnDefinitions = columns.entrySet().stream().map(entrySet -> entrySet.getKey() + " " + entrySet.getValue()).collect(Collectors.toList());
    return 
        "CREATE TABLE " + tableName + " (" + 
        " id int(11) NOT NULL auto_increment, " + join(", ", columnDefinitions) + "," + 
        " created_at datetime NOT NULL," + 
        " updated_at datetime NOT NULL," + 
        " PRIMARY KEY (`id`)" + 
        ")";
  }

}
