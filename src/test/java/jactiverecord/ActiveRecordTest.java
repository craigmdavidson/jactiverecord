package jactiverecord;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.concurrent.TimeUnit;

import org.junit.Test;


public class ActiveRecordTest extends DatabaseFixturing {

  @Test public void testCrud() throws InterruptedException {
    // given a new product
    Product product = new Product();
    product.setName("Hat");
    product.setSku(130);
    assertEquals(0, product.getId());
    assertFalse(product.isPersisted());
  
    // we can create it in the database
    assertTrue(product.save());
    assertTrue(product.isPersisted());
    assertNotSame(0, product.getId());
    assertEquals(product.getCreatedAt(), product.getUpdatedAt());
    
    // we can read it back
    Product created = ActiveRecord.firstWhere(Product.class, "id = ?", product.getId());
    assertEquals(product.getId(), created.getId());
    assertEquals("Hat", created.getName());
    assertEquals(130, created.getSku());
    
    // we can update it
    TimeUnit.SECONDS.sleep(1); // wait for created at and updated at not to be the same
    created.setName("Bobble Hat");
    created.setSku(131);
    assertTrue(created.save());
    assertTrue(created.getUpdatedAt().after(created.getCreatedAt()));
    
    // and read it back again
    Product updated = ActiveRecord.firstWhere(Product.class, "id = ?", product.getId());
    assertEquals(created.getId(), updated.getId());
    assertEquals("Bobble Hat", updated.getName());
    assertEquals(131, updated.getSku());
    assertTrue(updated.getUpdatedAt().after(updated.getCreatedAt()));
    
    // we can delete it
    assertTrue(updated.destroy());
    
    // and no longer read it back
    Product deleted = ActiveRecord.firstWhere(Product.class, "id = ?", product.getId());
    p("Record should be gone " + deleted);
    assertNull(deleted);
  }  
  
  @Test public void test_find_by_id_with_record_present() {
    Product product = new Product();
    product.setAttributes(map("name", "Hat", "sku", 4));
    assertTrue(product.save());
    long id = product.getId();
    
    Product created = ActiveRecord.find(id, Product.class);
    assertEquals("Hat", created.getName());
  }
  
  @Test public void test_find_by_id_with_no_record_present() {
    assertNull(ActiveRecord.find(-100, Product.class));
  }
  
  @Test public void test_set_attributes(){
    Product product = new Product();
    product.setAttributes(map("name", "Hat", "sku", 4));
    assertEquals("Hat", product.getName());
    assertEquals(4, product.getSku());
  }
  
  @Test public void test_get_attributes(){
    Product product = new Product();
    product.setName("Hat");
    product.setSku(4);
    assertEquals(map(
        "name", "Hat", 
        "sku", 4, 
        "id", 0, 
        "created_at", null, 
        "updated_at", null).toString(), product.getAttributes().toString());
  }
  
  @Test public void test_table_name_is_pluralized_simple_class_name_if_not_specified(){
    assertEquals("products", ActiveRecord.tableName(Product.class));
  }
  
  @Test public void test_table_name_declared_table_name_if_present(){
    assertEquals("products", ActiveRecord.tableName(SpecialProduct.class));
  }
  
}

class Product extends ActiveRecord {
  private String name;
  private int sku;

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  
  public int getSku() { return sku; }
  public void setSku(int sku) { this.sku = sku; }
}

class SpecialProduct extends Product {
  static final String tableName = "products";
}



