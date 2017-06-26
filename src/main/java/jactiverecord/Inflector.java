package jactiverecord;

import java.util.ArrayList;
import java.util.HashMap;

public class Inflector {
  public static final Inflections inflections = new Inflections(); 
  static {
    uncountable("equipment", "information", "money", "species", "series", "fish", "sheep");
    irregular("octopus", "octupi");
    irregular("person", "people");
    irregular("child", "children");
    irregular("mouse", "mice");
  }
  private static InflectionMap plurals() {return inflections.plurals;}
  private static InflectionMap singulars() {return inflections.singulars;}
  
  
  public static String pluralize(String word) {
    if (shouldNotPluralize(word)) return word;
    if (plurals().containsKey(word)) return plurals().substitute(word);
    return word + "s";
  } 

  public static String singularize(String word) {
    if (shouldNotPluralize(word)) return word;
    if (singulars().containsKey(word)) return singulars().get(word);
    return word.substring(0, word.length() -1);
  }

  public static String tableize(String word) {
    return pluralize(underscore(word));
  }
  
  public static String underscore(String word) {
    return separate(word, "_");
  }
  
  public static String separate(String word, String separator){
    return new CamelCase(word).separateWith(separator).toLowerCase();
  }
  
  public static String tableize(Class<?> klass) {
    return tableize(klass.getSimpleName());
  }
  
  private static boolean shouldNotPluralize(String word) {
    return 
      word == null || word.length() == 0 || 
      inflections.uncountables.contains(word.toLowerCase());
  }
  
  public static void uncountable(String ... uncountables) {
    for (String uncountable : uncountables) 
      inflections.uncountables.add(uncountable.toLowerCase());
  }

  public static void irregular(String singular, String plural) {
    plurals().put(singular, plural);
    singulars().put(plural, singular);
  }
  
  public static String ordinalize(Integer number){
    String n = number.toString();
    if (n.endsWith("11") || 
      n.endsWith("12") || n.endsWith("13")) return number + "th";
    if (n.endsWith("1")) return number + "st";
    if (n.endsWith("2")) return number + "nd";
    if (n.endsWith("3")) return number + "rd";
    return number + "th";
  }
  
  public static String camelize(String s) {
    return new UnderscoreCase(s).camelize();
  }
}

class Inflections {
  public InflectionList uncountables = new InflectionList();
  public InflectionMap plurals = new InflectionMap();
  public InflectionMap singulars = new InflectionMap();
  public void clearAll(){
    uncountables.clear();
    plurals.clear();
    singulars.clear();
  }
}

class InflectionList extends ArrayList<String>{
  private static final long serialVersionUID = 655920811794462953L;
  public boolean add(String s){return super.add(s.toLowerCase());}
  public boolean contains(String s){
    String ss = s.toLowerCase();
    if (super.contains(ss)) return super.contains(ss);
    for (String rule : this) if (ss.endsWith(rule)) return true;
    return false;
  }
}

class InflectionMap extends HashMap<String, String>{
  private static final long serialVersionUID = -4163118581291898033L;
  public String put(String a, String b){
    return super.put(a.toLowerCase(), b.toLowerCase());
  }
  public String substitute(String word) {
    if (super.containsKey(word)) return get(word);
    for (String part : this.keySet())
      if (word.toLowerCase().endsWith(part)) 
        return sub(word, part, get(part)); 
    return word;
  }
  private String sub(String word, String part, String value) {
    int i = word.toLowerCase().lastIndexOf(part);
    String result = word.substring(0, i);
    return result + recase(word.substring(i), value);
  }
  private String recase(String pre, String post) {
    if (Character.isUpperCase(pre.charAt(0)))
      return Character.toUpperCase(post.charAt(0)) + post.substring(1);
    return post;
  }
  public boolean containsKey(String word){
    String downcased = word.toLowerCase();
    if (super.containsKey(downcased))
      return super.containsKey(downcased);
    for (String rule : this.keySet()) 
      if (downcased.endsWith(rule)) 
        return true;
    return false;
  }
  public String get(String s){
    return super.get(s.toLowerCase());
  }
}
