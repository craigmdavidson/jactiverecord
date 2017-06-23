package jactiverecord;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class DaoTest extends DatabaseFixturing {
    
  @Test public void test_no_rows_in_table() {
    assertTrue(dao.findBySql("SELECT * FROM products").isEmpty());
  }
  
  @Test public void test_insert() {
    Timestamp now = now(); now.setNanos(0); // MySQL ignores nanoseconds    
    int id = dao.executeDml(
        "INSERT INTO products " + 
        " (name, sku, created_at, updated_at)"+ 
        " VALUES (?, ?, ?, ?)", "socks", 121, now, now);
    
    List<Map<String, Object>> products = dao.findBySql("SELECT * FROM products WHERE id = ?", id);
    assertEquals(1, products.size());
    assertEquals(
      map(
        "id", id, 
        "name", "socks",
        "sku", 121,
        "created_at", now,
        "updated_at", now), 
      last(products)    
    );
  }

  @Test public void test_update() throws InterruptedException{    
    int id = dao.executeDml(
        "INSERT INTO products " + 
        " (name, sku, created_at, updated_at)"+ 
        " VALUES (?, ?, ?, ?)", "socks", 121, now(), now());
    
    // wait a sec so updated_at has time to be different from created_at
    TimeUnit.MILLISECONDS.sleep(1000); 
    
    dao.executeDml(
        "UPDATE products" + 
        " SET sku = ?, " + 
        " updated_at = ? " + 
        " WHERE id = ?", 122, now(), id);
    
    List<Map<String, Object>> products = dao.findBySql("SELECT * FROM products where id = ?", id);
    assertEquals(1, products.size());
    Map<String, Object> record = last(products);
    assertEquals(122, record.get("sku"));
    assertNotEquals(record.get("updated_at"), record.get("created_at"));
  }
  
  @Test public void test_delete() {
    int id = dao.executeDml(
        "INSERT INTO products " + 
        " (name, sku, created_at, updated_at)"+ 
        " VALUES (?, ?, ?, ?)", "socks", 121, now(), now());

    dao.executeDml("DELETE FROM products WHERE id = ?", id);
    assertTrue(dao.findBySql("SELECT * FROM products WHERE id = ?", id).isEmpty());    
  }

}
