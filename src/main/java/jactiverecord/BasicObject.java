package jactiverecord;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import com.google.common.base.Joiner;

import minimalisp.Lisp;

public class BasicObject extends Lisp {
  
  public static void p(Object o) { System.out.println(o); }
  public static String join(String joinString, Object ...objects) { return join(joinString, list(objects)); }  
  public static String join(String joinString, List<? extends Object> list) { return Joiner.on(joinString).join(compact(list)); }
  public Timestamp now() { return Timestamp.from(Instant.now()); }

}
