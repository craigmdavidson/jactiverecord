package jactiverecord;

import org.junit.After;
import org.junit.Before;

public class DatabaseFixturing extends BasicObject {
  protected Dao dao = new Dao();  

  // Fixturing...
  @Before public void setUp(){
    createDatabase();
    setUpTable();
  }
  
  @After public void dropDatabase(){
    dao.dropDatabase("jactiverecord_test");
  }  

  public void createDatabase() {
    Dao.setDatabaseUrl("jdbc:mysql://127.0.0.1:3306/?user=root&password=");
    dao.createDatabase("jactiverecord_test");
  }

  public void setUpTable() {
    Dao.setDatabaseUrl("jdbc:mysql://127.0.0.1:3306/jactiverecord_test?user=root&password=");
    new Dao().createTable("products", map("name", "VARCHAR(255)", "sku", "INT"));
  }
}
