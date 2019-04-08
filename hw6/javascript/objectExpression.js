"use strict";

let expression = (function () {

    let variables = ['x', 'y', 'z'];
    let vars = {};
    let operations = {};


    let Primitive = function () {
        this.prefix = function () {
            return this.value.toString();
        };
        this.toString = function () {
            return this.value.toString();
        };
    };
    let Const = function (value) {
        this.value = value;
    };
    Const.prototype = new Primitive();
    Const.prototype.evaluate = function () {
        return this.value;
    };
    Const.prototype.diff = function () {
        return Const.ZERO;
    };

    Const.ZERO = new Const(0);
    Const.ONE = new Const(1);
    Const.TWO = new Const(2);

    let Variable = function (name) {
        this.value = name;
        this.index = variables.indexOf(name);
    };
    Variable.prototype = new Primitive();
    Variable.prototype.evaluate = function (...args) {
        return args[this.index];
    };
    Variable.prototype.diff = function (name) {
        return name === this.value ? Const.ONE : Const.ZERO;
    };
    variables.forEach((cur) => {
        vars[cur] = new Variable(cur);
    });
    let AbstractOperator = function (symbol, operation, derivative) {
        this.op = function (index) {
            return this.operands[index];
        };

        this.prefix = function () {
            return '(' + symbol + ' ' + this.operands.map(function (elem) {
                return elem.prefix();
            }).join(' ') + ')';
        };
        this.postfix = function () {
            return '(' + this.operands.map(function (elem) {
                return elem.postfix()
            }).join(' ') + ' ' + symbol + ')';
        };
        this.toString = function () {
            return this.operands.join(' ') + ' ' + symbol;
        };
        this.evaluate = function (...args) {
            return operation(...this.operands.map((operand) => {
                return operand.evaluate(...args);
            }));
        };
        this.diff = function (name) {
            return derivative(name, ...this.operands);
        };
    };

    let operatorFactory = function (symbol, operation, derivative) {
        let Operator = function (...args) {
            this.operands = Array.from(args);
        };
        Operator.prototype = new AbstractOperator(symbol, operation, derivative);
        operations[symbol] = {
            op: Operator,
            opLength: operation.length,
        };
        return Operator;
    };

    let Negate = operatorFactory(
        'negate',
        function (a) {
            return -a;
        },
        function (name, a) {
            return new Negate(a.diff(name));
        }
    );

    let Add = operatorFactory(
        '+',
        function (a, b) {
            return a + b;
        },
        function (name, a, b) {
            return new Add(a.diff(name), b.diff(name));
        }
    );

    let Subtract = operatorFactory(
        '-',
        function (a, b) {
            return a - b;
        },
        function (name, a, b) {
            return new Subtract(a.diff(name), b.diff(name));
        }
    );

    let Multiply = operatorFactory(
        '*',
        function (a, b) {
            return a * b;
        },
        function (name, a, b) {
            return new Add(
                new Multiply(a, b.diff(name)),
                new Multiply(a.diff(name), b)
            );
        }
    );

    let Divide = operatorFactory(
        '/',
        function (a, b) {
            return a / b;
        },
        function (name, a, b) {
            return new Divide(
                new Subtract(
                    new Multiply(a.diff(name), b),
                    new Multiply(a, b.diff(name))
                ),
                new Multiply(b, b)
            );
        }
    );
    let Exp = operatorFactory(
        null,
        function (a) {
            return Math.exp(a);
        },
        function (name, a) {
            return new Multiply(
                a.diff(name),
                new Exp(a),
            )
        }
    );
    let Sumexp = operatorFactory(
        'sumexp',
        function (...args) {
            return Array.from(args).reduce((sum, cur) => sum + Math.exp(cur), 0);
        },
        function (name, ...args) {
            return Array.from(args).reduce((sum, operation) => new Add(sum, (new Exp(operation)).diff(name)), Const.ZERO);
        }
    );
    let Softmax = operatorFactory(
        'softmax',
        function (...args) {
            return Math.exp(args[0]) / Array.from(args).reduce((sum, cur) => sum + Math.exp(cur), 0);
        },
        function (name, ...args) {
            return (new Divide(
                new Exp(args[0]),
                new Sumexp(...args)
            )).diff(name);
        }
    );

    function myNew(constructor, args) {
        let tmp = Object.create(constructor.prototype);
        constructor.apply(tmp, args);
        return tmp;
    }

    let parse = function (str) {
        let stack = [];
        str.trim().split(/\s+/).forEach(token => {
            if (token in operations) {
                stack.push(myNew(operations[token].op,
                    stack.splice(stack.length - operations[token].opLength, operations[token].opLength)));
            } else if (variables.indexOf(token) !== -1) {
                stack.push(vars[token]);
            } else {
                stack.push(new Const(parseInt(token)));
            }
        });
        return stack.pop();
    };
    let exceptions = function () {
        let exceptionFactory = function (msg) {
            let Exception = function (index, token) {
                this.name = msg + " on position " + index + ", where '" + token + "' is";
            };
            Exception.prototype = new Error();
            return Exception;
        };

        let ClosingParenthesisMissingException = exceptionFactory(
            'Closing parenthesis expected'
        );
        let ExpressionEndExpectedException = exceptionFactory(
            'End of expression expected'
        );
        let OperationExpectedException = exceptionFactory(
            'Operation symbol expected'
        );
        let OperandExpectedException = exceptionFactory(
            'Operand expected'
        );
        let InvalidOperandsAmountException = exceptionFactory(
            'Invalid operands amount found'
        );
        return {
            ClosingParenthesisMissingException: ClosingParenthesisMissingException,
            ExpressionEndExpectedException: ExpressionEndExpectedException,
            OperationExpectedException: OperationExpectedException,
            OperandExpectedException: OperandExpectedException,
            InvalidOperandsAmountException: InvalidOperandsAmountException
        }
    }();

    let Tokenizer = function (str) {
        this.index = 0;
        this.token = '';
        let isWhitespace = function (c) {
            return /\s/.test(c);
        };

        this.nextToken = function () {
            while (this.index < str.length && isWhitespace(str[this.index])) {
                this.index++;
            }
            this.token = '';
            if (str[this.index] === '(' || str[this.index] === ')') {
                this.token = str[this.index++];
            } else {
                while (this.index < str.length &&
                !isWhitespace(str[this.index]) && str[this.index] !== '(' && str[this.index] !== ')') {
                    this.token += str[this.index++];
                }
            }
        };
    };

    let parseOperand = function (tokenizer, parseExpression) {
        if (tokenizer.token === '(') {
            return parseExpression();
        } else if (variables.indexOf(tokenizer.token) !== -1) {
            let res = vars[tokenizer.token];
            tokenizer.nextToken();
            return res;
        } else if (!isNaN(tokenizer.token) && tokenizer.token !== '') {
            let res = new Const(parseFloat(tokenizer.token));
            tokenizer.nextToken();
            return res;
        } else {
            throw new exceptions.OperandExpectedException(tokenizer.index, tokenizer.token);
        }
    };
    let parsePostfix = function (str) {
        let tokenizer = new Tokenizer(str);

        let parseExpression = function () {
            if (tokenizer.token === '(') {
                tokenizer.nextToken();
                let args = [];
                while (!(tokenizer.token in operations)) {
                    args.push(parseOperand(tokenizer, parseExpression));
                }
                let operation = operations[tokenizer.token];
                if (args.length !== operation.opLength) {
                    throw new exceptions.InvalidOperandsAmountException(tokenizer.index, tokenizer.token);
                }
                tokenizer.nextToken();
                if (tokenizer.token !== ')') {
                    throw new exceptions.ClosingParenthesisMissingException(tokenizer.index, tokenizer.token);
                }
                tokenizer.nextToken();
                return myNew(operation.op, args);
            } else {
                return parseOperand(tokenizer, parseExpression);
            }
        };

        tokenizer.nextToken();
        let res = parseExpression();
        if (tokenizer.token !== '') {
            throw new exceptions.ExpressionEndExpectedException(tokenizer.index, tokenizer.token);
        }
        return res;
    };
    let parsePrefix = function (str) {
        let tokenizer = new Tokenizer(str);

        let parseExpression = function () {
            if (tokenizer.token === '(') {
                tokenizer.nextToken();
                if (!(tokenizer.token in operations)) {
                    throw new exceptions.OperationExpectedException(tokenizer.index, tokenizer.token);
                }
                let operation = operations[tokenizer.token];
                tokenizer.nextToken();
                let args = [];
                if (operation.opLength === 0) {
                    while (tokenizer.token !== ')' && tokenizer.token !== '') {
                        args.push(parseOperand(tokenizer, parseExpression));
                    }
                } else {
                    for (let i = 0; i < operation.opLength; i++) {
                        args.push(parseOperand(tokenizer, parseExpression));
                    }
                }
                if (tokenizer.token !== ')') {
                    throw new exceptions.ClosingParenthesisMissingException(tokenizer.index, tokenizer.token);
                }
                tokenizer.nextToken();
                return myNew(operation.op, args);
            } else {
                return parseOperand(tokenizer, parseExpression);
            }
        };
        tokenizer.nextToken();
        let res = parseExpression();
        if (tokenizer.token !== '') {
            throw new exceptions.ExpressionEndExpectedException(tokenizer.index, tokenizer.token);
        }
        return res;
    };

    return {
        Const: Const,
        Variable: Variable,
        Negate: Negate,
        Add: Add,
        Subtract: Subtract,
        Multiply: Multiply,
        Divide: Divide,
        Sumexp: Sumexp,
        Softmax: Softmax,
        parse: parse,
        parsePrefix: parsePrefix,
        parsePostfix: parsePostfix,
        Exp: Exp
    }

})();
let Const = expression.Const;
let Variable = expression.Variable;
let Negate = expression.Negate;
let Add = expression.Add;
let Subtract = expression.Subtract;
let Multiply = expression.Multiply;
let Divide = expression.Divide;
let Sumexp = expression.Sumexp;
let Softmax = expression.Softmax;
let parse = expression.parse;
let parsePrefix = expression.parsePrefix;
let parsePostfix = expression.parsePostfix;