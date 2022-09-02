package JavaPatternMatcher;

import java.util.List;
import java.util.LinkedList;

class NonTerminal extends Token {
  protected List<Token> followSet;
  protected List<List<Token>> rules;
  protected boolean lambda;
  protected List<Token> updateList;
  public NonTerminal(String s) {
    super(s);
    lambda = false;
  }
  public NonTerminal(String s, List<Token> r) {
    super(s);
    lambda = false;
    addRule(r);
  }
  public void addRule(List<Token> r) {
    rules.add(r);
    if (r.size() == 0) {
      lambda = true;
    }
  }
  public boolean canBeLambda() {
    return lambda;
  }
  public int numRules() {
    return rules.size();
  }
  public List<Token> getRule(int r) {
    return rules.get(r);
  }
  public List<List<Token>> getRules() {
    return rules;
  }
  public void makeLambda() {
    lambda = true;
  }
  public void addToFollowSet(List<Token> s) {
    List<Token> remaining = new LinkedList<Token>();
    for (Token t : s) {
      if (!followSet.contains(t)) {
        followSet.add(t);
        remaining.add(t);
      }
    }
    if (remaining.size() > 0) {
      for (Token t : updateList) {
        ((NonTerminal)t).addToFollowSet(remaining);
      }
    }
  }
  public List<Token> getFollowSet() {
    return followSet;
  }
  public void addToUpdateList(Token t) {
    updateList.add(t);
  }
  public String toString() {
    String ret = "NonTerminal:" + id + "\nRules:\n";
    for (List<Token> l : rules) {
      String rs = "\t";
      for (Token t : l) {
        rs += t.getId() + " ";
      }
      rs += "\n";
      ret += rs;
    }
    if (lambda) {
      ret += "\t \u03BB\n";
    }
    ret += "Firsts: ";
    for (Token t : beginSet) {
      ret += t.getId() + " ";
    }
    ret += "\nFollow Set: ";
    for (Token t : followSet) {
      ret += t + " ";
    }
    ret += "\nUpdate List: ";
    for (Token t : updateList) {
      ret += t.getId() + " ";
    }
    return ret;
  }
  @Override
  public void constructUpdateList() {
    for (List<Token> lt : rules) {
      for (int i = lt.size()-1; i >= 0; --i) {
        Token t = lt.get(i);
        if (t instanceof Terminal) {
          break;
        }
        if (t instanceof Action && i > 0) {
          if (!(lt.get(i-1) instanceof Terminal) && !(lt.get(i-1) instanceof Action)) {
            continue;
          } else {
            break;
          }
        }
        if (!updateList.contains(t)) {
          updateList.add(t);
        }
        if (!((NonTerminal)t).canBeLambda()) {
          break;
        }
      }
    }
  }
  
}
