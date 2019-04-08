let variables = ['x', 'y', 'z'];
let constants = {
    pi: Math.PI,
    e: Math.E
};

const cnst = value => () => value;
function variable(name) {
    let index = variables.indexOf(name);
    return (...values) => values[index];
}

const abstractOperation = operation => (...operands) => (...values) => operation(...operands.map((currentOperand) => currentOperand(...values)));

const add = abstractOperation((a, b) => a + b);
const subtract = abstractOperation((a, b) => a - b);
const multiply = abstractOperation((a, b) => a * b);
const divide = abstractOperation((a, b) => a / b);

const negate = abstractOperation(a => -a);

const avg5 = abstractOperation((...operands) => {
    let sum = operands.reduce((sum, current) => sum + current, 0);
    return sum / operands.length;
});

const med3 = abstractOperation((...operands) => {
    operands.sort((a, b) => a - b);
    return operands[1];
});
let operations = {
    '+': [add, 2],
    '-': [subtract, 2],
    '*': [multiply, 2],
    '/': [divide, 2],
    'negate': [negate, 1],
    'med3': [med3, 3],
    'avg5': [avg5, 5]
};
const pi = cnst(Math.PI);
const e = cnst(Math.E);

function isDigit(symbol) {
    return symbol >= '0' && symbol <= '9';
}

const parse = expression => {
    let stack = [];
    expression.split(/\s+/).forEach((token) => {
            if (variables.indexOf(token) !== -1) {
                stack.push(variable(token));
            } else if (token in operations) {
                let len = stack.length;
                let args = stack.slice(len - operations[token][1], len);
                args.map(() => {
                    return stack.pop();
                }).reverse();
                stack.push(operations[token][0](...args));
            } else if (token in constants) {
                stack.push(cnst(constants[token]));
            } else {
                stack.push(cnst(parseFloat(token)));
            }
        }
    );
    return stack.pop();
};