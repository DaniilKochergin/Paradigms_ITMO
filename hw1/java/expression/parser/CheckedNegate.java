package expression.parser;


import expression.TripleExpression;
import expression.exceptions.OverflowException;

public class CheckedNegate extends AbstractUnaryOperator {
    public CheckedNegate(TripleExpression x) {
        super(x);
    }

    protected int apply(int x) throws OverflowException {
        check(x);
        return -x;
    }

    private void check(int x) throws OverflowException {
        if (x == Integer.MIN_VALUE) {
            throw new OverflowException("Overflow: unary minus from Integer.MIN_VALUE");
        }
    }
}