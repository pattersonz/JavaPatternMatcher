import javaPatternMatcher.Token;
import javaPatternMatcher.Grammar;
import javaPatternMatcher.Validator;


public class SampleTester {
  public static void main(String[] args) {
    Validator val = new Validator(new Grammar("testGrammar.grammar"),true);
    val.processOptionalToken(parent);
    val.processToken(object);
    val.processOptionalToken(parent2);
    val.processToken(field_access);
    val.processOptionalToken(parent);
    val.processToken(object);
  }
}
