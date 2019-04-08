"use strict";

let expression = (function () {

    let variables = ['x', 'y', 'z'];
    let vars = {};
    let operations = {};

    let Primitive = function () {
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
        this.toString = function () {
            return this.operands.join(' ') + ' ' + symbol;
        };
        this.evaluate = function (...args) {
            return operation(...this.operands.map((operand) => {
                return operand.evaluate(...args);
            }));
        };
        this.diff = function (name) {
            return derivative.call(this, name, ...this.operands);
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

    let ArcTan = operatorFactory(
        'atan',
        function (a) {
            return Math.atan(a);
        },
        function (name, a) {
            return new Multiply(
                new Divide(
                    Const.ONE,
                    new Add(
                        new Multiply(a, a),
                        Const.ONE
                    )
                ),
                a.diff(name)
            );
        }
    );

    let ArcTan2 = operatorFactory(
        'atan2',
        function (a, b) {
            return Math.atan2(a, b);
        },
        function (name, a, b) {
            return new Divide(
                new Subtract(
                    new Multiply(b, a.diff(name)),
                    new Multiply(a, b.diff(name))
                ),
                new Add(
                    new Multiply(a, a),
                    new Multiply(b, b))
            );
        }
    );

    let parse = function (str) {
        let stack = [];
        str.trim().split(/\s+/).forEach(token => {
            if (token in operations) {
                let tmp = Object.create(operations[token].op.prototype);
                operations[token].op.apply(tmp, stack.splice(stack.length - operations[token].opLength));
                stack.push(tmp);
            } else if (variables.indexOf(token) !== -1) {
                stack.push(vars[token]);
            } else {
                stack.push(new Const(parseFloat(token)));
            }
        });
        return stack.pop();
    };
    return {
        Const: Const,
        Variable: Variable,
        Negate: Negate,
        Add: Add,
        Subtract: Subtract,
        Multiply: Multiply,
        Divide: Divide,
        ArcTan: ArcTan,
        ArcTan2: ArcTan2,
        parse: parse
    }
})();
let Const = expression.Const;
let Variable = expression.Variable;
let Negate = expression.Negate;
let Add = expression.Add;
let Subtract = expression.Subtract;
let Multiply = expression.Multiply;
let Divide = expression.Divide;
let ArcTan = expression.ArcTan;
let ArcTan2 = expression.ArcTan2;
let parse = expression.parse;