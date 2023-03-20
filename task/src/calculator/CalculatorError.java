package calculator;

class CalculatorError extends RuntimeException {
    public CalculatorError(String message) {
        super(message);
    }
}