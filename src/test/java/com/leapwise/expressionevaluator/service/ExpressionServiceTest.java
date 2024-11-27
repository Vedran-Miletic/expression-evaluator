package com.leapwise.expressionevaluator.service;

import com.leapwise.expressionevaluator.model.Expression;
import com.leapwise.expressionevaluator.repository.ExpressionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.List;

@SpringBootTest
public class ExpressionServiceTest {

    private ExpressionRepository repository;
    private ExpressionService service;

    @BeforeEach
    public void setUp() {
        repository = mock(ExpressionRepository.class);
        service = new ExpressionService(repository);
    }

    @Test
    public void contextLoads() {
        assertNotNull(service);
    }

    @Test
    public void saveExpression_newExpression_savedSuccessfully() {
        String name = "TestExpression";
        String expression = "(x > 5 && y < 10)";
        String generatedId = "123e4567-e89b-12d3-a456-426614174000";

        when(repository.findByExpression(expression)).thenReturn(Optional.empty());
        when(repository.save(any(Expression.class))).thenAnswer(invocation -> {
            Expression expr = invocation.getArgument(0);
            assertEquals(name, expr.getName());
            assertEquals(expression, expr.getExpression());
            return expr;
        });

        String resultId = service.saveExpression(name, expression);

        assertNotNull(resultId);
        verify(repository, times(1)).save(any(Expression.class));
    }

    @Test
    public void getExpressionById_validId_returnsExpression() {
        String id = "123e4567-e89b-12d3-a456-426614174000";
        Expression expression = new Expression(id, "TestExpression", "(x > 5 && y < 10)");

        when(repository.findById(id)).thenReturn(Optional.of(expression));

        Expression result = service.getExpressionById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(repository, times(1)).findById(id);
    }

    @Test
    public void evaluateExpression_validInput_returnsTrue() {
        String id = "123e4567-e89b-12d3-a456-426614174000";
        String expressionString = "(x > 5 && y < 10)";
        Expression expression = new Expression(id, "TestExpression", expressionString);

        Map<String, Object> variables = Map.of("x", 6, "y", 5);

        when(repository.findById(id)).thenReturn(Optional.of(expression));

        boolean result = service.evaluateExpression(id, variables);

        assertTrue(result);
        verify(repository, times(1)).findById(id);
    }

    @Test
    public void testSingleAmpersand() {
        String expression = "&";
        String invalidPattern = "(?<!&)\\&(?![&])";
        assertTrue(expression.matches(".*" + invalidPattern + ".*"), "Single `&` was not detected.");
    }

    @Test
    public void testComplexExpressionWithAmpersand() {
        String expression = "(x > 5 & y < 10)";
        String invalidPattern = "(?<!&)\\&(?![&])";

        boolean matches = expression.matches(".*" + invalidPattern + ".*");
        assertTrue(matches, "Invalid operator `&` in complex expression was not detected.");
    }


    @Test
    public void evaluateExpression_missingVariable_throwsException() {
        String id = "123e4567-e89b-12d3-a456-426614174000";
        String expressionString = "(x > 5 && y < 10)";
        Expression expression = new Expression(id, "TestExpression", expressionString);

        Map<String, Object> variables = Map.of("x", 6); // Missing `y`.

        when(repository.findById(id)).thenReturn(Optional.of(expression));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            service.evaluateExpression(id, variables);
        });

        assertTrue(exception.getMessage().contains("Error during expression evaluation"));
    }

    @Test
    public void saveExpression_existingExpression_returnsExistingId() {
        String name = "TestExpression";
        String expression = "(x > 5 && y < 10)";
        String existingId = "123e4567-e89b-12d3-a456-426614174000";

        when(repository.findByExpression(expression)).thenReturn(Optional.of(new Expression(existingId, name, expression)));

        String resultId = service.saveExpression(name, expression);

        assertEquals(existingId, resultId);
        verify(repository, times(0)).save(any(Expression.class));
    }

    @Test
    public void evaluateExpression_validInput_returnsFalse() {
        String id = "123e4567-e89b-12d3-a456-426614174000";
        String expressionString = "(x > 5 && y < 10)";
        Expression expression = new Expression(id, "TestExpression", expressionString);

        Map<String, Object> variables = Map.of("x", 3, "y", 15); // Fails both conditions.

        when(repository.findById(id)).thenReturn(Optional.of(expression));

        boolean result = service.evaluateExpression(id, variables);

        assertFalse(result);
        verify(repository, times(1)).findById(id);
    }

    @Test
    public void validateExpression_unbalancedParentheses_throwsException() {
        String invalidExpression = "(x > 5 && (y < 10)"; // Missing closing parenthesis.

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.validateExpression(invalidExpression);
        });

        assertEquals("Unbalanced parentheses in expression.", exception.getMessage());
    }
}
