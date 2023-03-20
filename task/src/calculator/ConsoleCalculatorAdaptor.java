package calculator;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class ConsoleCalculatorAdaptor {
    Scanner scanner;
    InputStream inputStream;
    PrintStream outputStream;
    Calculator calc;

    public ConsoleCalculatorAdaptor() {
        inputStream = System.in;
        outputStream = System.out;
        scanner = new Scanner(inputStream);
        calc = new Calculator();
    }

    public void runCalculator() {
        while (true)
            try {
                String input = scanner.nextLine();
                if ("".equals(input)) {
                } else if (input.matches("\\/.*"))
                    proceedCommand(input);
                else if (input.matches(Calculator.VAR_STRING_REGEX))
                    calc.proceedVariable(input);
                else if (input.matches(Calculator.EXPRESSION_PATTERN))
                    outputStream.println(calc.calculateExpression(input));
                else throw new CalculatorError("Invalid expression");
            } catch (CalculatorError e) {
                outputStream.println(e.getMessage());
            } catch (CalculatorExit e) {
                break;
            }
    }

    void proceedCommand(String input) throws CalculatorExit {
        if ("/exit".equals(input)) {
            outputStream.println("Bye!");
            throw new CalculatorExit();
        } else if ("/help".equals(input))
            outputStream.println("The program calculates the sum of numbers");
        else
            outputStream.println("Unknown command");
    }

}
