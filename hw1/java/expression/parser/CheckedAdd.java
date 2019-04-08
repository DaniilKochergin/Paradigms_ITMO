package expression.parser;

import expression.TripleExpression;
import expression.exceptions.OverflowException;

public class CheckedAdd extends AbstractBinaryOperator {

    public CheckedAdd(TripleExpression x, TripleExpression y) {
        super(x, y);
    }

    void check(int x, int y) throws OverflowException {
        if (x > 0 && Integer.MAX_VALUE - x < y) {
            throw new OverflowException("Overflow: sum of 2 positive arguments");
        }
        if (x < 0 && Integer.MIN_VALUE - x > y) {
            throw new OverflowException("Overflow: sum of 2 negative arguments");
        }
    }

    protected int apply(int x, int y) throws OverflowException {
        check(x, y);
        return x + y;
    }
}