package expression.parser;

import expression.TripleExpression;
import expression.exceptions.OverflowException;

public abstract class AbstractBinaryOperator implements TripleExpression {
    private final TripleExpression firstOperand;
    private final TripleExpression secondOperand;

    public AbstractBinaryOperator(TripleExpression x, TripleExpression y) {
        firstOperand = x;
        secondOperand = y;
    }


    protected abstract int apply(int x, int y) throws OverflowException;

    public int evaluate(int x, int y, int z) throws OverflowException {
        return apply(firstOperand.evaluate(x, y, z), secondOperand.evaluate(x, y, z));
    }
}