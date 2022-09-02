package JavaPatternMatcher;

import java.util.List;
import java.util.LinkedList;

class Action extends Token {
  public Action(String s) {
    super(s);
    beginSetComplete = true;
  }
  public String toString() {
    return getId();
  }
  @Override
  public void constructUpdateList() {
    return;
  }
  
}
