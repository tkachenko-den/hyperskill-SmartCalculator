package calculator;

import java.math.BigInteger;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static calculator.Calculator.*;

class Element {
        enum ElementType {
            OPERATOR, LITERAL, VARIABLE, PARENTHESE
        }

        enum ParentheseType {
            LEFT, RIGHT
        }

        ElementType type;
        String original;
        String element;
        int operatorPriority = 0;
        ParentheseType parentheseType;
        BigInteger value;


    public static String plusMinusReduce(String str) {
        Matcher matcher = Pattern.compile("^(.*?)([+-]+)(.*?)$").matcher(str);
        return !matcher.matches() ?
                str :
                matcher.group(1) +
                        (matcher.group(2).chars()
                                .map(ch -> ch == '-' ? -1 : 1)
                                .reduce(1, Math::multiplyExact) < 0 ? "-" : "+") +
                        matcher.group(3);
    }

        public static int getOperatorPriority(String operator) {
            return "+-".contains(operator) ? 1 : 2;
        }

        public Element(String str) {
            original = str;
            // Элемент - бинарный оператор
            if (str.matches("^" + OPERATOR_PATTERN + "$")) {
                type = ElementType.OPERATOR;
                element = str.trim();
                if (element.length() > 1) {
                    if (element.indexOf('*') > -1 || element.indexOf('/') > -1)
                        throw new CalculatorError("Invalid expression");
                    element = plusMinusReduce(element);
                }
                operatorPriority = getOperatorPriority(element);
            }
            // Элемент - круглая скобка
            else if (str.matches("^" + PARENTHESES_PATTERN + "$")) {
                type = ElementType.PARENTHESE;
                element = str;
                parentheseType = "(".contains(str) ? ParentheseType.LEFT : ParentheseType.RIGHT;
            }
            // Элемент - литерал
            else if (str.matches("^" + LITERAL_PATTERN + "$")) {
                type = ElementType.LITERAL;
                element = plusMinusReduce(str);
            }
            // Элемент - переменная
            else if (str.matches("^" + VARIABLE_PATTERN + "$")) {
                type = ElementType.VARIABLE;
                element = plusMinusReduce(str);
            }
        }

        public Element calculateElement(Map<String, BigInteger> variables) {
            if (type==ElementType.LITERAL || type==ElementType.VARIABLE) {
                if(type==ElementType.LITERAL)
                    value = new BigInteger(element.replaceAll("[+-]", ""));
                if(type==ElementType.VARIABLE) {
                    value = variables.get(element.replaceAll("[+-]", ""));
                    if(value==null) throw new CalculatorError("Unknown variable");
                }
                value = value.multiply(new BigInteger((element.charAt(0) == '-' ? "-1" : "1")));
            }
            return this;
        }

    @Override
    public String toString() {
        return "{" + type + " " + element + "}";
    }

    BiFunction<BigInteger,BigInteger,BigInteger> getOperatorFunction() {
        switch (element) {
            case "+": return BigInteger::add;
            case "-": return BigInteger::subtract;
            case "*": return BigInteger::multiply;
            case "/": return BigInteger::divide;
            default: return null;
        }
    }
}