package jstest.prefix;

import jstest.BaseJavascriptTest;
import jstest.Language;

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
public class PostfixSumexpSoftmaxTest extends PrefixSumexpSoftmaxTest {
    protected PostfixSumexpSoftmaxTest(final int mode) {
        super(mode, new Language(SUMEXP_SOFTMAX_OBJECT, PostfixMixin.DIALECT, new SumexpSoftmaxTests()), "postfix");
    }

    @Override
    protected void testParsing() {
        PostfixMixin.testErrors(this);
    }

    @Override
    protected String parse(final String expression) {
        return "parsePostfix('" + expression + "')";
    }

    public static void main(final String... args) {
        new PostfixSumexpSoftmaxTest(BaseJavascriptTest.mode(args, PostfixSumexpSoftmaxTest.class, "easy", "hard")).run();
    }
}
