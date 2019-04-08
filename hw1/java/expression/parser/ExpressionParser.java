package expression.parser;

import expression.TripleExpression;
import expression.exceptions.*;

public class ExpressionParser implements Parser {

    public TripleExpression parse(String expression) throws ParsingException {
        s = expression;
        i = 0;
        depth = 0;
        cur = Token.END;
        return add();
    }

    private TripleExpression add() throws ParsingException {
        TripleExpression a = mul();
        while (true) {
            switch (cur) {
                case ADD:
                    a = new CheckedAdd(a, mul());
                    break;
                case SUBTRACT:
                    a = new CheckedSubtract(a, mul());
                    break;
                default:
                    return a;
            }
        }
    }

    private TripleExpression mul() throws ParsingException {
        TripleExpression a = unar();
        while (true) {
            switch (cur) {
                case MUL:
                    a = new CheckedMultiply(a, unar());
                    break;
                case DIVIDE:
                    a = new CheckedDivide(a, unar());
                    break;
                default:
                    return a;
            }
        }
    }

    private TripleExpression unar() throws ParsingException {
        next();
        TripleExpression a;
        switch (cur) {
            case OPEN:
                a = add();
                if (cur != Token.CLOSE) {
                    throw new MissingClosingBracketException(s.substring(Math.max(i - 10, 0), Math.min(i + 10, s.length())), i, i - Math.max(i - 10, 0));
                }
                next();
                break;
            case CONST:
                a = new Const(val);
                next();
                isNumber();
                break;
            case VARIABLE:
                a = new Variable(name);
                next();
                break;
            case NOT:
                return new CheckedNegate(unar());
            case HIGH:
                return new CheckedHigh(unar());
            case LOW:
                return new CheckedLow(unar());
            default:
                throw new ParsingException("Incorrect expression");
        }
        return a;
    }

    private enum Token {
        ADD,
        DIVIDE,
        CONST,
        MUL,
        SUBTRACT,
        NOT,
        OPEN,
        CLOSE,
        VARIABLE,
        HIGH,
        LOW,
        END;
    }

    Token cur = Token.END;
    int i = 0;
    String s;
    String name;
    int val;
    int depth;

    void skipSpace() {
        while (s.length() > i && Character.isWhitespace(s.charAt(i))) {
            i++;
        }
    }

    void next() throws ParsingException {
        skipSpace();
        if (s.length() <= i) {
            cur = Token.END;
            return;
        }

        switch (s.charAt(i)) {
            case '+':
                cur = Token.ADD;
                break;
            case '*':
                cur = Token.MUL;
                break;
            case '/':
                cur = Token.DIVIDE;
                break;
            case '-':
                if (cur == Token.VARIABLE || cur == Token.CONST || cur == Token.CLOSE) {
                    cur = Token.SUBTRACT;
                } else {
                    if (i + 1 == s.length()) {
                        throw new MissingOperandException(s.substring(Math.max(i - 10, 0), Math.min(i + 10, s.length())), i, i - Math.max(i - 10, 0));
                    }
                    if (Character.isDigit(s.charAt(i + 1))) {
                        getNumber();
                        cur = Token.CONST;
                    } else {
                        cur = Token.NOT;
                    }
                }
                break;
            case '(':
                cur = Token.OPEN;
                depth++;
                break;
            case ')':
                cur = Token.CLOSE;
                if (depth <= 0) {
                    throw new ExtraClosingBracketException(s.substring(Math.max(i - 10, 0), Math.min(i + 10, s.length())), i, i - Math.max(i - 10, 0));
                }
                depth--;
                break;
            case 'h':
                checkOperation("high");
                cur = Token.HIGH;
                break;
            case 'l':
                checkOperation("low");
                cur = Token.LOW;
                break;
            default: {
                if (Character.isDigit(s.charAt(i))) {
                    getNumber();
                    isNumber();
                    cur = Token.CONST;
                } else {
                    getName();
                    cur = Token.VARIABLE;
                }
            }
        }
        i++;
    }

    void checkOperation(String operation) throws UnknownOperationExceptions {
        int begin = i;
        if (i + operation.length() >= s.length()) {
            throw new UnknownOperationExceptions(s.substring(Math.max(i - 10, 0), Math.min(i + 10, s.length())), i, begin - Math.max(i - 10, 0), 0);
        }
        for (int j = 0; j < operation.length(); ++j) {
            if (s.charAt(i + j) != operation.charAt(j)) {
                throw new UnknownOperationExceptions(s.substring(Math.max(i - 10, 0), Math.min(i + 10, s.length())), i, begin - Math.max(i - 10, 0), j);
            }
        }
        i += operation.length() - 1;
        if (!Character.isWhitespace(s.charAt(i + 1)) && s.charAt(i + 1) != '-' && s.charAt(i + 1) != '(') {
            throw new UnknownOperationExceptions(s.substring(Math.max(i - 10, 0), Math.min(i + 10, s.length())), begin, begin - Math.max(i - 10, 0), operation.length());
        }
    }


    void isNumber() throws MissingOperationException {
        if (cur == Token.VARIABLE || cur == Token.CONST || cur == Token.CLOSE) {
            throw new MissingOperationException(s.substring(Math.max(i - 10, 0), Math.min(i + 10, s.length())), i, i - Math.max(i - 10, 0));
        }
    }

    void getNumber() throws IncorrectConstException {
        int l = i;
        if (s.charAt(i) == '-') {
            i++;
        }
        while (s.length() > i && Character.isDigit(s.charAt(i))) {
            i++;
        }
        try {
            val = Integer.parseInt(s.substring(l, i));
        } catch (NumberFormatException e) {
            throw new IncorrectConstException(i);
        }
        i--;
    }


    void getName() throws UnknownSymbolException {
        name = String.valueOf(s.charAt(i));
        if (!(name.contains("x") || name.contains("y") || name.contains("z"))) {
            throw new UnknownSymbolException(s.substring(Math.max(i - 10, 0), Math.min(i + 10, s.length())), i, i - Math.max(i - 10, 0));
        }
    }
}