package expression.parser;

import expression.TripleExpression;
import expression.exceptions.OverflowException;

public class CheckedDivide extends AbstractBinaryOperator {
    public CheckedDivide(TripleExpression x, TripleExpression y) {
        super(x, y);
    }

    void check(int x, int y) throws OverflowException {
        if (y == 0 || x == Integer.MIN_VALUE && y == -1) {
            throw new OverflowException("Overflow: divide by zero");
        }
    }

    protected int apply(int x, int y) throws OverflowException {
        check(x, y);
        return x / y;
    }
}