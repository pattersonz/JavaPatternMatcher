package JavaPatternMatcher;

import java.util.List;
import java.util.LinkedList;

abstract class Token {
  public Token() {
    id = "";
  }
  public Token(String name) {
    id = name;
  }
  protected String id;
  protected List<Token> beginSet;
  protected boolean beginSetComplete;
  public abstract String toString();
  public String getId() {
    return id;
  }
  public boolean isThisToken(String s) {
    return s.equals(id);
  }
  public void addToBeginSet(List<Token> s) {
    for (Token t : s) {
      if (!beginSet.contains(t)) {
        beginSet.add(t);
      }
    }
  }
  public boolean isBeginSetComplete() {
    return beginSetComplete;
  }
  public List<Token> getBeginSet() {
    return beginSet;
  }
  public void finishedBeginSet() {
    beginSetComplete = true;
  }
  public abstract void constructUpdateList();
  
}
