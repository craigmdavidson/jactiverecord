package jactiverecord;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DdlTest extends BasicObject {
  @Test public void testBuildDDL(){
    String ddl = new Ddl().buildCreateTableDDL("products", map("name", "VARCHAR(255)", "sku", "INT"));
    assertEquals(
      "CREATE TABLE products (" + 
      " id int(11) NOT NULL auto_increment," + 
      " name VARCHAR(255)," + 
      " sku INT," + 
      " created_at datetime NOT NULL," + 
      " updated_at datetime NOT NULL," + 
      " PRIMARY KEY (`id`)" + 
      ")", 
      ddl);
  }
}
