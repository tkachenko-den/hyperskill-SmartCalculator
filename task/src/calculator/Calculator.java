package calculator;

import java.math.BigInteger;
import java.util.*;
import java.util.regex.Pattern;


public class Calculator {
    public static final String VAR_STRING_REGEX = "\\s*([^=]*?)\\s*=\\s*(\\S*)\\s*";
    public static final String EXPRESSION_PATTERN = "^[\\d\\w.+\\-*\\/() ]+$";
    public static final String OPERATOR_PATTERN = "\\s*[+-\\/*]+";
    public static final String LITERAL_PATTERN = "[+-]*\\d+";
    public static final String VARIABLE_PATTERN = "[+-]*\\w+";
    public static final String PARENTHESES_PATTERN = "[()]";
    public static final String INFIX_PATTERN = "((?<=[)\\d\\w])" + OPERATOR_PATTERN + "(?=\\s*[-(\\d\\w]))"
            + "|(" + LITERAL_PATTERN + ")|(" + VARIABLE_PATTERN + ")|" + PARENTHESES_PATTERN;

    private final Map<String, BigInteger> variables;

    public Calculator() {
        variables = new HashMap<>();
    }


    public BigInteger calculateExpression(String input) {
        input=input.trim();
        // Проверка на корректность расстановки скобок
        if(!isBracketsBalanced(input)) throw new CalculatorError("Invalid expression");
        return calculatePostfix(infixToPostfix(parseString(input)));
    }

    public boolean isBracketsBalanced(String input) {
        String scopes = Pattern.compile("[^()\\[\\]{}]").matcher(input).replaceAll("");
        Deque<Character> stack = new ArrayDeque<>();
        try {
            for (char tt : scopes.toCharArray())
                if("({[".indexOf(tt)>-1)
                    stack.push(tt);
                else if ("({[".indexOf(stack.pop())!=")}]".indexOf(tt))
                    return false;
        } catch (NoSuchElementException e) {
            return false;
        }
        return stack.isEmpty();
    }

    void proceedVariable(String input) {
        var matcher = Pattern.compile(VAR_STRING_REGEX).matcher(input);
        matcher.matches();
        // Проверка левой части равенства
        if (!matcher.group(1).matches("[A-z]+"))
            throw new CalculatorError("Invalid identifier");
        // Проверка правой части равенства
        if (!matcher.group(2).matches("^[+-]*(\\d+|[A-z]+)$"))
            throw new CalculatorError("Invalid assignment");

        variables.put(matcher.group(1),calculateExpression(matcher.group(2)));
    }

    public static List<Element> parseString(String input) {
        return new Scanner(input)
                .findAll(INFIX_PATTERN)
                .map(mr -> new Element(input.substring(mr.start(), mr.end())))
                .toList();
    }

    public static List<Element> infixToPostfix(List<Element> infix) {
        List<Element> postfix = new ArrayList<>();
        Deque<Element> stack = new ArrayDeque<>();

        for (Element element : infix) {
            switch (element.type) {
                case LITERAL:
                case VARIABLE:
                    postfix.add(element);  // #1
                    break;
                case OPERATOR:
                    if (stack.size() == 0   // #2
                            || (stack.peek().type == Element.ElementType.PARENTHESE
                            && stack.peek().parentheseType == Element.ParentheseType.LEFT) // #2
                            || (stack.peek().type == Element.ElementType.OPERATOR
                            && element.operatorPriority > stack.peek().operatorPriority))  // #3
                        stack.push(element);
                    else if (stack.peek().type == Element.ElementType.OPERATOR
                            && element.operatorPriority <= stack.peek().operatorPriority) { // #4
                        while(!(stack.size()==0
                                || (stack.peek().type == Element.ElementType.OPERATOR
                                && element.operatorPriority > stack.peek().operatorPriority)
                                || (stack.peek().type == Element.ElementType.PARENTHESE
                                && stack.peek().parentheseType==Element.ParentheseType.LEFT)
                        ))
                            postfix.add(stack.pop());
                        stack.push(element);
                    }
                    break;
                case PARENTHESE:
                    if(element.parentheseType== Element.ParentheseType.LEFT)
                        stack.push(element);
                    else while(stack.size()>0) {
                        if (stack.peek().type!=Element.ElementType.PARENTHESE)
                            postfix.add(stack.pop());
                        else {
                            stack.pop();
                            break;
                        }
                    }
                    break;
            }
        }
        postfix.addAll(stack);
        return postfix;
    }

    public BigInteger calculatePostfix(List<Element> postfix) {
        Deque<BigInteger> stack = new ArrayDeque<>();
        for(Element element : postfix) {
            switch (element.type) {
                case VARIABLE:
                case LITERAL:
                    element.calculateElement(variables);
                    stack.push(element.value);
                    break;
                case OPERATOR:
                    BigInteger second=stack.pop();
                    BigInteger first=stack.pop();
                    stack.push(element.getOperatorFunction().apply(first,second));
                    break;
            }
        }
        return stack.pop();
    }


}
