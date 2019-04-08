package expression.parser;

import expression.TripleExpression;

public class CheckedLow extends AbstractUnaryOperator {

    public CheckedLow(TripleExpression x) {
        super(x);
    }

    @Override
    protected int apply(int x) {
        return Integer.lowestOneBit(x);
    }
}
