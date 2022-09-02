package JavaPatternMatcher;

import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;
import java.util.HashMap;

class TokenData {
  public String name;
  public int order;
  public HashMap<String,String> properties;
  public TokenData() {
    properties = new HashMap<String,String>();
  }
}
