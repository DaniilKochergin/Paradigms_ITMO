package expression.parser;

import expression.TripleExpression;
import expression.exceptions.OverflowException;

public class CheckedSubtract extends AbstractBinaryOperator {
    public CheckedSubtract(TripleExpression x, TripleExpression y) {
        super(x, y);
    }

    void check(final int x, final int y) throws OverflowException {
        if (x >= 0 && y < 0 && x - Integer.MAX_VALUE > y) {
            throw new OverflowException("Overflow: substracting from fist positive and second negative arguments");
        }
        if (x <= 0 && y > 0 && Integer.MIN_VALUE - x > -y) {
            throw new OverflowException("Overflow: substracting from fist negative and second positive arguments");
        }
    }

    protected int apply(int x, int y) throws OverflowException {
        check(x, y);
        return x - y;
    }
}