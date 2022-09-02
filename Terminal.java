package JavaPatternMatcher;

import java.util.List;
import java.util.LinkedList;

class Terminal extends Token {
  public Terminal(String s) {
    super(s);
  }
  public String toString() {
    return "Terminal: " + id;
  }
  @Override
  public void constructUpdateList() {
    return;
  }
  
}
