package JavaPatternMatcher;

import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;

import java.io.File;
import java.util.Scanner;


class Grammar {
  String terminals[] = {};

  private boolean accepted;
  private boolean valid;
  private int state;
  private List<Token> tokens;
  private State initialState;
 
  public Grammar(String file) {
    setTerminals();
    parseGrammar(file);
    constructSets();
    initialState = new State(tokens);
  }

  public String toString() {
    String ret = "Tokens:\n";
    for (Token t : tokens) {
      ret += t.toString() + "\n";
    }
    ret += "States:\n";
    ret += initialState.toString();
    return ret;
  }

  public State getRoot() {
    return initialState;
  }

  private void setTerminals() {
    List<String> ts = Arrays.asList(terminals);
    for (String s : ts) {
      tokens.add(new Terminal(s));
    }
  }

  private String rtrim(String s) {
    int i = s.length()-1;
    while (i >= 0 && Character.isWhitespace(s.charAt(i))) {
      i--;
    }
    return s.substring(0,i+1);
  }
  
  private void parseGrammar(String file) {
    Scanner sc;
    try {
      sc = new Scanner(new File(file));
    } catch (Exception e) {
      System.err.println("File not Found");
      return;
    }
    while (sc.hasNextLine()) {
      String line = rtrim(sc.nextLine().trim().replaceAll("\n","").replaceAll(" +"," "));
      List<String> toks = Arrays.asList(line.split(" "));
      if (toks.size() <= 2) {
        continue;
      }
      List<Token> ruleT = new LinkedList<Token>();
      for (int i = 0; i < toks.size(); ++i) {
        
        String primary = toks.get(i);
        String action = "";
        if (primary.indexOf(':') != -1) {
          action = primary.substring(primary.indexOf(':')+1);
          primary = primary.substring(0,primary.indexOf(':'));
        }
        if (!tokens.contains(primary)) {
          NonTerminal n = new NonTerminal(primary);
          tokens.add(n);
          ruleT.add(n);
        }
        if (!action.equals("")) {
          if (!tokens.contains(action)) {
            Action a = new Action(action);
            tokens.add(a);
            ruleT.add(a);
          }
        }
        boolean added = false;
        String lhs = toks.get(0);
        for (Token t : tokens) {
          if (t.isThisToken(lhs)) {
            added = true;
            if (t instanceof Terminal || t instanceof Action) {
              System.err.println("Error: tried to give rule to terminal or action\n");
              System.exit(1);
            } else {
              ((NonTerminal)t).addRule(ruleT);
            }
            
          }
        }
        if (!added) {
          NonTerminal x = new NonTerminal(lhs, ruleT);
          tokens.add(x);
        }
      }
    }
  }

  private List<Token> getBeginSet(Token t) {
    if (t.isBeginSetComplete()) {
      return t.getBeginSet();
    }
    NonTerminal n = (NonTerminal)t;
    for (List<Token> l : n.getRules()) {
      for (Token r : l) {
        if (r instanceof Action) {
          continue;
        }
        n.addToBeginSet(getBeginSet(r));
        if (r instanceof Terminal ||
            !((NonTerminal)r).canBeLambda())
          break;
      }
    }
    return new LinkedList<Token>();
  }

  private void getFollowSet(Token t) {
    if (t instanceof Terminal || t instanceof Action) {
      return;
    }
    NonTerminal n = (NonTerminal)t;
    for (List<Token> l : n.getRules()) {
      Token first = null, second = null;
      for (Token to : l) {
        second = to;
        if (first != null && ! (first instanceof Terminal) && !(first instanceof Action) &&
            !(second instanceof Action)) {
          ((NonTerminal)first).addToFollowSet(second.getBeginSet());
        }
        first = second;
      }
    }
  }

  /*  private Action determineAction(String s) {
    
      }*/

  private void constructSets() {
    for (Token t : tokens) {
      getBeginSet(t);
    }
    for (Token t : tokens) {
      t.constructUpdateList();
    }
    for (Token t : tokens) {
      getFollowSet(t);
    }
  }

 
}
