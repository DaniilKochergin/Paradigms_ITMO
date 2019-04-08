package expression.parser;


public class Main {
    public static void main(String args[]) {
        String s = new String("x*y(+(z-1   )/10");
        ExpressionParser parser = new ExpressionParser();
        try {
            System.out.println(parser.parse(s).evaluate(1, 1, 1));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
