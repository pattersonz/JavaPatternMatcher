package JavaPatternMatcher;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;

class State {
  class Production{
    public NonTerminal head;
    public List<Token> body;
    public int pos;
    public Production(NonTerminal h, List<Token> b, int p) {
      head = h;
      body = b;
      pos = p;
    }
    public boolean eq(Production p) {
      return p.head == head && p.body == body && p.pos == pos;
    }
    public String toString() {
      String ret = head.getId() + " -> ";
      for (int i = 0; i < body.size(); ++i) {
        if (pos == i) {
          ret += ". ";
        }
        ret += body.get(i).getId() + " ";
      }
      if (pos == body.size()) {
        ret += ".";
      }
      return ret;
    }
  }
  class Reduction{
    public String retTok;
    public int numReduce;
    public List<Token> reduceSet;
    public List<Token> ruleBody;
    public Reduction(String s, int i, List<Token> v, List<Token> b) {
      retTok = s;
      numReduce = i;
      reduceSet = v;
      ruleBody = b;
    }
    public String toString() {
      String ret = retTok + " <- " + numReduce + " on:";
      for (Token t : reduceSet) {
        ret += t.getId() + " ";
      }
      return ret;
    }
  }
  public static int count=0;
  private int id;
  private boolean conflict;
  private State root;
  List<Production> prods;
  List<Reduction> reds;
  Map<String,State> paths;
  public State(List<Token> toks) {
    for (Token t : toks) {
      if (t instanceof NonTerminal) {
        for (List<Token> l : ((NonTerminal)t).getRules())
          prods.add(new Production((NonTerminal)t,l,0));
      }
    }
    root = this;
    id = count;
    count++;
    createPaths();
  }
  private State (State r, List<Production> ps) {
    prods = ps;
    root = r;
    id = count;
    count++;
  }
  public String toString() {
    List<Integer> l = new LinkedList<Integer>();
    return toString(l);
  }
  public boolean isShift(String s) {
    for (Map.Entry<String,State> e : paths.entrySet()) {
      if (s.equals(e.getKey())) {
        return true;
      }
    }
    return false;
  }
  public boolean isReduction(String s) {
    for (Reduction r : reds) {
      for (Token rs : r.reduceSet) {
        if (rs.isThisToken(s)) {
          return true;
        }
      }
    }
    return false;
  }
  public List<Reduction> getReductions(String s) {
    List<Reduction> ret = new LinkedList<Reduction>();
    for (Reduction r : reds) {
      for (Token rs : r.reduceSet) {
        if (rs.isThisToken(s)) {
          ret.add(r);
        }
      }
    }
    return ret;
  }
  public State shift(String s) {
    for (Map.Entry<String,State> e : paths.entrySet()) {
      if (s.equals(e.getKey())) {
        return e.getValue();
      }
    }
    return null;
  }
  public Reduction reduce(String s) {
    for (Reduction r : reds) {
      for (Token rs : r.reduceSet) {
        if (rs.isThisToken(s)) {
          return r;
        }
      }
    }
    return new Reduction("",0,new LinkedList<Token>(),new LinkedList<Token>());
  }
  public boolean isEndState() {
    boolean isEnd = false;
    for (Reduction r: reds) {
      isEnd = isEnd || (r.reduceSet.size() == 0);
    }
    return (paths.size()==0) && isEnd && (reds.size() > 0);
  }
  public String getPattern() {
    for (Reduction r : reds) {
      if (r.reduceSet.size() == 0) {
        return r.retTok;
      }
    }
    return "";
  }
  public int getId() {
    return id;
  }
  public boolean getConflicts() {
    List<String> all = new LinkedList<String>();
    for (Reduction r : reds) {
      for (Token t: r.reduceSet) {
        String thisTok = t.getId();
        if (all.contains(thisTok)) {
          return false;
        }
        all.add(thisTok);
      }
    }
    for (Map.Entry<String,State> e : paths.entrySet()) {
      String thisTok = e.getKey();
      if (all.contains(thisTok)) {
        return false;
      }
    }
    return true;
  }
  public boolean hasConflicts() {
    return conflict;
  }
  
  public List<Token> getFinalRules() {
    for (Production p : prods) {
      if (p.pos == p.body.size()) {
        return p.body;
      }
    }
    return new LinkedList<Token>();
  }
  private void createPaths() {
    List<Production> tempProds = prods;
    for (int z = 0; z < tempProds.size(); z++) {
      Production p = tempProds.get(z);
      if (p.pos == p.body.size()) {
        int x = 0;
        for ( Token tok : p.body) {
          if (tok instanceof Action) {
            ++x;
          }
        }
        reds.add(new Reduction(p.head.getId(),p.body.size()-x,p.head.getFollowSet(),p.body));
      } else {
        Token next = p.body.get(p.pos);
        List<Production> nextSet = new LinkedList<Production>();
        int growth = 1;
        if (p.body.size() > p.pos+1 && p.body.get(p.pos+1) instanceof Action ) {
          growth = 2;
        }
        nextSet.add(new Production(p.head,p.body,p.pos+growth));
        for (int y = z+1; y < tempProds.size(); ++y) {
          Production pro = tempProds.get(y);
          nextSet.add(new Production(pro.head, pro.body,pro.pos + (
                                     (pro.body.size() > pro.pos+1 &&
                                      pro.body.get(pro.pos+1) instanceof Action)?2:1)));
          tempProds.remove(y);
          y--;
        }
        for (Production pro : nextSet) {
          if (pro.pos < pro.body.size() && pro.body.get(pro.pos) instanceof NonTerminal) {
            NonTerminal n = (NonTerminal)pro.body.get(pro.pos);
            for (List<Token> lp : n.getRules()) {
              Production newP = new Production(n,lp,0);
              boolean found = false;
              for (int pp = 0; pp < nextSet.size() && !found; ++pp) {
                found = found || nextSet.get(pp).eq(newP);
              }
              if (!found) {
                nextSet.add(newP);
              }
            }
          }
        }
        List<Integer> visited = new LinkedList<Integer>();
        State prodPath = root.searchStates(visited,nextSet);
        if (prodPath != null) {
          paths.put(next.getId(),prodPath);
        } else {
          State newS = new State(root,nextSet);
          paths.put(next.getId(),newS);
          newS.createPaths();
        }
      }
    }
    conflict = !getConflicts();
  }
  private State searchStates(List<Integer> v, List<Production> p) {
    if (v.contains(id)) {
      return null;
    }
    v.add(id);
    int visited = 0;
    for (Production pro : p) {
      boolean found = false;
      for (Production j : prods) {
        if (pro.eq(j)) {
          found = true;
          break;
        }
        if (found) {
          break;
        }
      }
      visited++;
    }
    if (visited == p.size() && p.size() == prods.size()) {
      return this;
    }
    for (Map.Entry<String,State> e : paths.entrySet()) {
      State ss = e.getValue().searchStates(v,p);
      if (ss != null) {
        return ss;
      }
    }
    return null;
  }
  private String toString(List<Integer> v) {
    if (v.contains(id)) {
      return "";
    }
    v.add(id);
    String ret = "";
    ret += "State " + id + "\n";
    ret += "\tProductions\n";
    for (Production p : prods) {
      ret += "\t\t" + p + "\n";
    }
    ret += "\tReductions\n";
    for (Reduction r : reds) {
      ret += "\t\t" + r + "\n";
    }
    ret += "\tNexts\n";
    for (Map.Entry<String,State> e : paths.entrySet()) {
      ret += "\t\t<" + e.getValue() + "," + e.getValue().getId() + ">\n";
    }
    ret += "\n";
    if (!getConflicts()) {
      ret += "has conflict\n";
    }
    if (isEndState()) {
      ret += "is end state\n";
    }
    for (Map.Entry<String,State> e : paths.entrySet()) {
      ret += e.getValue().toString(v);
    }
    return ret;
  }
}
