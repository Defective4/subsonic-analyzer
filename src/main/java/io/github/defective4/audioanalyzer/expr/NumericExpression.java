package io.github.defective4.audioanalyzer.expr;

public class NumericExpression extends Number {
    private final Number number;
    private final NumericExpressionType type;

    public NumericExpression(NumericExpressionType type, Number number) {
        this.type = type;
        this.number = number;
    }

    @Override
    public double doubleValue() {
        return number.doubleValue();
    }

    @Override
    public float floatValue() {
        return number.floatValue();
    }

    public Number getNumber() {
        return number;
    }

    public NumericExpressionType getType() {
        return type;
    }

    @Override
    public int intValue() {
        return number.intValue();
    }

    @Override
    public long longValue() {
        return number.longValue();
    }

    public boolean matches(Number compareTo) {
        int result = getDiff(compareTo);
        return switch (type) {
            case EQUAL_TO -> result == 0;
            case LESS_THAN -> result <= 0;
            case MORE_THAN -> result >= 0;
            default -> throw new IllegalStateException("Invalid expr type");
        };
    }

    @Override
    public String toString() {
        return "NumericExpression [number=" + number + ", type=" + type + "]";
    }

    private int getDiff(Number compareTo) {
        double compare = compareTo.doubleValue();
        double self = number.doubleValue();
        return compare == self ? 0 : compare >= self ? 1 : -1;
    }

}
