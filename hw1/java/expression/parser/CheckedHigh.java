package expression.parser;


import expression.TripleExpression;

public class CheckedHigh extends AbstractUnaryOperator {
    public CheckedHigh(TripleExpression x) {
        super(x);
    }

    @Override
    protected int apply(int x) {
        return Integer.highestOneBit(x);
    }
}
