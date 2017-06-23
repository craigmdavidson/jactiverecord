package jactiverecord;

public class UnderscoreCase {
  private String s;

  public UnderscoreCase(String s) { this.s = s; }

  public String camelize() {
    char [] chars = s.toCharArray();
    char [] change = s.toCharArray(); 
    
    for (int i=0; i<chars.length; i++)      
      if (chars[i] == '_' && (i+1)<chars.length)
        change[i+1] = Character.toUpperCase(change[i+1]);
    
    // System.out.println("---> " + String.valueOf(change));
//    for (char c: change){
//      System.out.println(c);
//    }
    
    // System.out.println(s + "--> " + Arrays.asList(chars) + "-->" + Arrays.asList(change));
    
    return String.valueOf(change).replaceAll("_", "");
    //return join("", Arrays.asList(change)).replaceAll("_", ""); 
    
    //return Joiner.on("").join(change)
    //return new Joiner<Character>().join(Joiner.box(change), "").replaceAll("_", "");
  }

}
