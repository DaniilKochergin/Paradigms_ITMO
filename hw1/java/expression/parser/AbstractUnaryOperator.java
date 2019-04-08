package expression.parser;


import expression.TripleExpression;
import expression.exceptions.OverflowException;

public abstract class AbstractUnaryOperator implements TripleExpression {
    protected final TripleExpression operand;

    protected AbstractUnaryOperator(TripleExpression x) {
        operand = x;
    }

    protected abstract int apply(int x) throws OverflowException;

    public int evaluate(int x, int y, int z) throws OverflowException {
        return apply(operand.evaluate(x, y, z));
    }
}