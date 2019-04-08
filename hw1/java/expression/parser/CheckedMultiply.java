package expression.parser;


import expression.TripleExpression;
import expression.exceptions.OverflowException;

public class CheckedMultiply extends AbstractBinaryOperator {
    public CheckedMultiply(TripleExpression x, TripleExpression y) {
        super(x, y);
    }


    void check(int x, int y) throws OverflowException {
        if (x > 0 && y > 0 && Integer.MAX_VALUE / x < y) {
            throw new OverflowException("Overflow: Multiply by 2 positive arguments");
        }
        if (x > 0 && y < 0 && Integer.MIN_VALUE / x > y) {
            throw new OverflowException("Overflow: Multiply by first positive and second negative arguments");
        }
        if (x < 0 && y > 0 && Integer.MIN_VALUE / y > x) {
            throw new OverflowException("Overflow: Multiply by first negative and second positive argumts");
        }
        if (x < 0 && y < 0 && Integer.MAX_VALUE / x > y) {
            throw new OverflowException("Overflow: Multiply by 2 negative arguments");
        }
    }

    protected int apply(int x, int y) throws OverflowException {
        check(x, y);
        return x * y;
    }
}














































































































































































































