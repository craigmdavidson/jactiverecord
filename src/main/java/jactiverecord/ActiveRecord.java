package jactiverecord;

import static java.lang.reflect.Modifier.isStatic;
import static java.lang.reflect.Modifier.isTransient;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ActiveRecord extends BaseRecord {
  private long id;
  private Timestamp createdAt, updatedAt;
  public long getId() { return id; }
  public Timestamp getCreatedAt() { return createdAt; }
  public Timestamp getUpdatedAt() { return updatedAt; }
  public boolean isPersisted() { return getId() > 0; }
  
  public static <T extends ActiveRecord> List<T> where(Class<? extends BaseRecord> klass, String whereClause, Object... parameters) {
      return map(
              attributes -> create(klass, attributes), 
              new Dao().findBySql(queryFor(klass, whereClause), parameters));
  }

  public static <T extends ActiveRecord> T firstWhere(Class<? extends BaseRecord> recordClass, String whereClause, Object ...parameters) {
    return first(where(recordClass, whereClause, parameters));
  }
  
  public static <T extends ActiveRecord> T find(long id, Class<? extends ActiveRecord> klass) {
    return firstWhere(klass, "id = ?", id);
  }
  
  public static String queryFor(Class<? extends BaseRecord> recordClass, String whereClause) {
    return "SELECT * FROM " + tableName(recordClass) + " WHERE " + whereClause;
  }

  public static String tableName(Class<? extends BaseRecord> recordClass) {
    try {
      Field tableName = recordClass.getDeclaredField("tableName");
      if (tableName != null && isStatic(tableName.getModifiers())) {
        tableName.setAccessible(true);
        return (String)tableName.get(recordClass);
      }
      else
        return Inflector.pluralize(recordClass.getSimpleName().toLowerCase());
    } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
      return Inflector.pluralize(recordClass.getSimpleName().toLowerCase());
    }
  }
  
  private static <T extends ActiveRecord> T create(Class<? extends BaseRecord> recordClass, Map<String, Object> attributes) {
    try {
      T record = (T)recordClass.newInstance();
      record.setAttributes(attributes);
      return (T)record;
    } catch (InstantiationException | IllegalAccessException e) {
      throw new ActiveRecordException(e);
    }
  }
  
  public Map<String, Object> getAttributes() {
    Map<String, Object> attributes = new LinkedHashMap<String, Object>();
    clientFields().forEach(field -> attributes.put(asColumnName(field.getName()), valueOf(field)));
    return attributes;
  }

  public <T extends ActiveRecord> T setAttributes(Map<String, Object> attributes) {
    clientFields().forEach(field -> assign(field, attributes.get(asColumnName(field.getName()))) );
    return (T)this;
  }
  
  protected List<Field> clientFields() {
    List<Field> declaredFields = new ArrayList<Field>();
    Class<?> klass = getClass();
    while(!BaseRecord.class.equals(klass)) {
      declaredFields.addAll(list(klass.getDeclaredFields()));
      klass = klass.getSuperclass();
    }
    return filter(
             field -> !isTransient(field.getModifiers()) && !isStatic(field.getModifiers()),
             declaredFields );
  }

  protected String asColumnName(String fieldName) { return Inflector.underscore(fieldName); }
  
  private Object valueOf(Field field) {
    try {
      field.setAccessible(true);
      return field.get(this);
    } catch (Exception e) {
      throw new ActiveRecordException(e);
    }    
  }
  
  private void assign(Field field, Object value) {
    try {
      if (value == null) return;
      field.setAccessible(true);

      // fields are naturally stored as BigDecimal but may be used in class as doubles
      // coerce any bigdecimals into doubles
      if (field.getType().equals(double.class) && value instanceof BigDecimal) {
        field.set(this, ((BigDecimal)value).doubleValue());
      } else {
        field.set(this, value);
      }
      field.setAccessible(false);
    } catch (Exception e) {
      throw new ActiveRecordException(e);
    }
  }
  
  public boolean save() {
    return isPersisted() ? update() : insert();
  }
  
  public boolean destroy() {
    long deleteCount = new Dao().executeDml(deleteDml(), getId());
    p(" " +  deleteCount + " row deleted");
    return deleteCount == 1;
  }

  private boolean insert() {
    Timestamp now = now();
    this.createdAt = now;
    this.updatedAt = now;
    Object[] params = array(list(except(getAttributes(), "id").values()));
    long id = new Dao().executeDml(insertDml(), params);
    if (id > 0) this.id = id;
    p(" " + tableName(getClass()) + " record inserted with id: " + id);
    return isPersisted();
  }
  
  private boolean update() {
    this.updatedAt = now();
    List<Object> params = list(except(getAttributes(), "id").values());
    params.add(getId()); // we need the id parameter to be last
    long count = new Dao().executeDml(updateDml(), array(params));
    p(" " + count + " row updated");
    return count == 1;
  }
  
  public Timestamp now() {
    return Timestamp.from(Instant.now());
  }
  
  public String insertDml() {
    List<String> keys = list(except(getAttributes(), "id").keySet());
    String params = join(", ", map(key -> "?", keys));
    return join(" ", 
        "INSERT INTO", tableName(getClass()),
        "(", join(", ", keys), ")", 
        "VALUES", "(", params, ")");
  }

  public String deleteDml() {
    return "DELETE FROM " + tableName(getClass()) + " WHERE id = ?";
  }
  
  public String updateDml() {
    List<String> keys = list(except(getAttributes(), "id").keySet());
    String params = join(", ", map(key -> key + " = ?", keys));
    return join(" ",
        "UPDATE", tableName(getClass()),
        "SET ", params,
        "WHERE id = ?");
  }
  
}
