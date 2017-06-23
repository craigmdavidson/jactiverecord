package jactiverecord;

public class ActiveRecordException extends RuntimeException {
  private static final long serialVersionUID = 4036212923570345572L;
  
  public ActiveRecordException(Exception e) { super(e); }
  public ActiveRecordException(String message, Exception e) { super(message, e); }
}
