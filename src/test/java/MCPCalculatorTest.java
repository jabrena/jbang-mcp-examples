import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class MCPCalculatorTest {
    
    private MCPCalculator calculator;
    
    @BeforeEach
    public void setup() {
        calculator = new MCPCalculator();
    }
    
    @ParameterizedTest
    @CsvSource({
        "add,      5.0, 3.0, 8.0",
        "subtract, 10.0, 4.0, 6.0",
        "multiply, 5.0, 3.0, 15.0",
        "divide,   10.0, 2.0, 5.0"
    })
    public void testBasicOperations(String operation, double a, double b, String expectedResult) {
        String result = calculator.operation(operation, a, b);
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @ParameterizedTest
    @MethodSource("specialCasesProvider")
    public void testSpecialCases(String operation, double a, double b, String expectedResult) {
        String result = calculator.operation(operation, a, b);
        assertThat(result).isEqualTo(expectedResult);
    }
    
    private static Stream<Arguments> specialCasesProvider() {
        return Stream.of(
            Arguments.of("divide", 10.0, 0.0, "Division by zero is not allowed!"),
            Arguments.of("power", 5.0, 3.0, "Unknown operation!")
        );
    }
    
    @ParameterizedTest
    @CsvSource({
        // Operation,    a,     b,    expected
        "ADD,            5.0,   3.0,  8.0",
        "Add,            5.0,   3.0,  8.0",
        "aDd,            5.0,   3.0,  8.0",
        "add,            5.0,   3.0,  8.0",
        "SUBTRACT,       10.0,  4.0,  6.0",
        "Subtract,       10.0,  4.0,  6.0",
        "sUbTrAcT,       10.0,  4.0,  6.0",
        "subtract,       10.0,  4.0,  6.0",
        "MULTIPLY,       5.0,   3.0,  15.0",
        "Multiply,       5.0,   3.0,  15.0",
        "mUlTiPlY,       5.0,   3.0,  15.0",
        "multiply,       5.0,   3.0,  15.0",
        "DIVIDE,         10.0,  2.0,  5.0",
        "Divide,         10.0,  2.0,  5.0",
        "dIvIdE,         10.0,  2.0,  5.0",
        "divide,         10.0,  2.0,  5.0"
    })
    public void testCaseInsensitiveOperations(String operation, double a, double b, String expectedResult) {
        String result = calculator.operation(operation, a, b);
        assertThat(result).isEqualTo(expectedResult);
    }
} 