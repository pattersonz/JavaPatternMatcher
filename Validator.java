package JavaPatternMatcher;

import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;

class Validator
{
  class StackFrame
  {
    public State state;
    public TokenData j;
    public StackFrame(State s, TokenData js)
    {
      state = s;
      j = js;
    }
    public StackFrame deepCopy() {
      StackFrame ret = new StackFrame(state,j);
      return ret;
    }
  }

  class ParseData
  {
    public List<StackFrame> stack;
    public List<TokenData> tokenOrder;
    public boolean valid;
    public ParseData() {
      valid = true;
      stack = new LinkedList<StackFrame>();
      tokenOrder = new LinkedList<TokenData>();
    }
    public ParseData deepCopy() {
      ParseData ret = new ParseData();
      ret.valid = valid;
      for (StackFrame s : stack) {
        stack.add(s.deepCopy());
      }
      ret.tokenOrder.addAll(tokenOrder);
      return ret;
    }
  }
  
  private Grammar grammar;
  private List<ParseData> parseQueue;
  private List<ParseData> validParses;
  private boolean parseInfo;
  
  public Validator(boolean p) {
    grammar = null;
    parseInfo = p;
    if (parseInfo) {
      System.err.println("Begin Validator");
    }
  }
  public Validator(Grammar g, boolean p) {
    grammar = g;
    parseInfo = p;
    parseQueue = new LinkedList<ParseData>();
    parseQueue.add(new ParseData());
    validParses = new LinkedList<ParseData>();
        
    if (parseInfo) {
      System.err.println("Begin Validator");
    }
  }
  public void processToken(String t, TokenData j, int pos) {
    if (parseInfo) {
      System.err.println("Processing: " + t);
    }
    int curSize = parseQueue.size();
    for (int i = 0; i < curSize; ++i) {
      processTokenRecursive(t,j,pos,parseQueue.get(i));
    }
    for (int i = 0; i < parseQueue.size(); ++i) {
      if (!parseQueue.get(i).valid) {
        parseQueue.remove(i);
        i--;
      }
    }
  }
  public void processOptionalToken(String t, TokenData j, int pos) {
    List<ParseData> newList = new LinkedList<ParseData>();
    for (ParseData p : parseQueue) {
      newList.add(p.deepCopy());
    }
    processToken(t,j,pos);
    parseQueue.addAll(newList);
  }
  public void processTokenRecursive(String t, TokenData j, int pos, ParseData pd) {
    if (!pd.valid) {
      return;
    }
    if (pos >= 0) {
      j.order = pos;
      pd.tokenOrder.add(j);
    }
    State curState;
    if (pd.stack.size() == 0) {
      curState = grammar.getRoot();
    } else {
      curState = pd.stack.get(pd.stack.size()-1).state;
    }
    boolean processed  = false;
    ParseData oldPd;
    if (curState.hasConflicts()) {
      oldPd = pd.deepCopy();
    } else {
      oldPd = pd;
    }

    if (curState.isReduction(t)) {
      processed = true;
      List<State.Reduction> reds = curState.getReductions(t);
      for ( State.Reduction r : reds) {
        if (parseInfo) {
          System.err.println("Reducing token: " + r.retTok);
        }
        String tokenPopped = "";
        ParseData currPd;
        if (curState.isShift(t) || r != reds.get(0)) {
          if (parseInfo) {
            System.err.println("Creating new branch");
          }
          currPd = oldPd.deepCopy();
          parseQueue.add(currPd);
        } else {
          currPd = pd;
        }

        if (currPd.stack.size() >= r.numReduce) {
          int popped = 0;
          for (int x = r.ruleBody.size() -1; x >= 0; --x) {
            if (r.ruleBody.get(x) instanceof Action) {
              //perform action
            } else {
              tokenPopped = currPd.stack.get(currPd.stack.size()-1).j.value + ((popped > 0) ? " " : "") + tokenPopped;
              currPd.stack.remove(currPd.stack.size()-1);
              popped++;
            }
          }
          
          tokenPopped = r.retTok + "( " + tokenPopped + " )";
          TokenData poppedT = new TokenData();
          poppedT.value = tokenPopped;
          processTokenRecursive(r.retTok,poppedT,-1,currPd);
          processTokenRecursive(t,j,-1,currPd);
        } else {
          System.err.println("Error tried to pop too much off the stack");
          System.exit(0);
        }
      }
    }
    if (curState.isShift(t)) {
      processed = true;
      if (parseInfo) {
        System.err.println("Shifting");
      }
      pd.stack.add(new StackFrame(curState.shift(t),j));
    }
    if (!processed) {
      if (parseInfo) {
        System.err.println("branch failed in state: " + curState.getId());
      }
      pd.valid = false;
    }
  }
  public String getVal(ParseData p, List<Token> v, int spot) {
    return getTokenData(p,v,spot).value;
  }
  public TokenData getTokenData(ParseData p, List<Token> v, int spot) {
    int sub = 1;
    for (int i = 0; i < spot; ++i) {
      if (v.get(i) instanceof Action) {
        sub++;
      }
    }
    return p.stack.get(spot-sub).j;
  }
  private void confirmParses() {
    for (int i = 0; i < parseQueue.size(); ++i) {
      ParseData p = parseQueue.get(i);
      List<Token> finalProd = p.stack.get(p.stack.size()-1).state.getFinalRules();
      if (finalProd.size() == 0) {
        continue;
      }
      for (int x = finalProd.size() -1; x >= 0; --x) {
        if (finalProd.get(x) instanceof Action) {
          //perform action
        }
      }
      if (p.valid) {
        validParses.add(p);
      }
      parseQueue.remove(i);
      i--;
    }
  }
}
