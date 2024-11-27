package com.leapwise.expressionevaluator.service;

import com.leapwise.expressionevaluator.model.Expression;
import com.leapwise.expressionevaluator.repository.ExpressionRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jexl3.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service layer responsible for managing logical expressions and their evaluations.
 * <p>
 * This service handles the following:
 * <ul>
 *   <li>Validation of logical expressions, including syntax and operator checks.</li>
 *   <li>Preprocessing of expressions to ensure compatibility with the evaluation engine.</li>
 *   <li>Storing and retrieving expressions in/from the database.</li>
 *   <li>Evaluating expressions dynamically using provided variables.</li>
 * </ul>
 * <p>
 * The service leverages JEXL (Java Expression Language) for dynamic expression evaluation.
 * </p>
 */
@Service
@Slf4j
public class ExpressionService {

    private final ExpressionRepository repository;

    public ExpressionService(ExpressionRepository repository) {
        this.repository = repository;
    }

    /**
     * Preprocesses the given expression by replacing logical operators with their Java equivalents.
     * Ensures the expression is trimmed and ready for further processing.
     *
     * @param expression the logical expression to preprocess
     * @return the preprocessed expression
     * @throws IllegalArgumentException if the expression is null or empty
     */
    private String preprocessExpression(String expression) {
        if (expression == null || expression.isEmpty()) {
            throw new IllegalArgumentException("Expression cannot be null or empty");
        }

        return expression.replace(" OR ", " || ")
                .replace(" AND ", " && ")
                .trim();
    }

    /**
     * Validates the logical operators within the expression.
     * Ensures no invalid operators (e.g., single `=` or `&`) are present.
     *
     * @param expression the logical expression to validate
     * @throws IllegalArgumentException if invalid operators or syntax are found
     */
    private void validateOperators(String expression) {
        String invalidPattern = "(?<![=!<>])=(?![=!<>])|(?<!&)\\&(?!&)|(?<!\\|)\\|(?!\\|)";

        if (expression.matches(".*" + invalidPattern + ".*")) {
            throw new IllegalArgumentException("Invalid operator or syntax found in expression.");
        }
    }

    /**
     * Validates the use of the assignment operator (`=`) in the expression.
     * Ensures it is only used in the form of `==` and not as a single assignment.
     *
     * @param expression the logical expression to validate
     * @throws IllegalArgumentException if invalid `=` operators are found
     */
    private void validateAssignmentOperator(String expression) {
        String invalidAssignmentPattern = "(?<![=!<>])=(?![=!<>])";

        if (expression.matches(".*" + invalidAssignmentPattern + ".*")) {
            throw new IllegalArgumentException("Invalid operator '=' found. Use '==' for comparisons.");
        }
    }

    /**
     * Validates that parentheses in the expression are balanced.
     *
     * @param expression the logical expression to validate
     * @throws IllegalArgumentException if parentheses are unbalanced
     */
    private void validateParentheses(String expression) {
        int balance = 0;
        for (char c : expression.toCharArray()) {
            if (c == '(') balance++;
            else if (c == ')') balance--;
            if (balance < 0) {
                throw new IllegalArgumentException("Unbalanced parentheses in expression.");
            }
        }
        if (balance != 0) {
            throw new IllegalArgumentException("Unbalanced parentheses in expression.");
        }
    }

    /**
     * Validates the syntax of the expression using JEXL.
     *
     * @param expression the logical expression to validate
     * @throws IllegalArgumentException if the syntax is invalid
     */
    private void validateExpressionSyntax(String expression) {
        try {
            JexlEngine jexl = new JexlBuilder().create();
            jexl.createExpression(expression);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid expression syntax: " + e.getMessage());
        }
    }

    /**
     * Validates the overall logical expression by checking operators, assignment,
     * parentheses, and syntax.
     *
     * @param expression the logical expression to validate
     * @throws IllegalArgumentException if the expression is invalid
     */
    public void validateExpression(String expression) {
        if (expression == null || expression.isEmpty()) {
            throw new IllegalArgumentException("Expression cannot be null or empty.");
        }

        validateOperators(expression);

        validateAssignmentOperator(expression);

        validateParentheses(expression);

        validateExpressionSyntax(expression);
    }

    /**
     * Saves a logical expression in the database after preprocessing and validation.
     * If the expression already exists, returns the existing UUID.
     *
     * @param name       the name of the expression
     * @param expression the logical expression to save
     * @return the UUID of the saved or existing expression
     */
    public String saveExpression(String name, String expression) {

        String processedExpression = preprocessExpression(expression);
        validateExpression(processedExpression);

        Optional<Expression> existingExpression = repository.findByExpression(processedExpression);

        if (existingExpression.isPresent()) {
            return existingExpression.get().getId();
        }

        String id = UUID.randomUUID().toString();
        Expression entity = new Expression(id, name, processedExpression);
        repository.save(entity);
        return id;
    }

    /**
     * Retrieves all saved logical expressions from the database.
     *
     * @return a list of all expressions
     */
    public List<Expression> getAll() {
        return repository.findAll();
    }

    /**
     * Retrieves a specific logical expression by its ID.
     *
     * @param id the UUID of the expression to retrieve
     * @return the requested logical expression
     * @throws RuntimeException if the expression is not found
     */
    public Expression getExpressionById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expression not found for ID: " + id));
    }

    /**
     * Evaluates a logical expression using the provided variables.
     * Retrieves the expression by ID, validates it, and evaluates it using JEXL.
     *
     * @param id        the UUID of the expression to evaluate
     * @param variables a map of variables used for evaluation
     * @return the result of the evaluation
     * @throws IllegalArgumentException if the ID or variables are invalid
     * @throws RuntimeException         if evaluation fails or the expression is not found
     */
    public boolean evaluateExpression(String id, Map<String, Object> variables) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }

        Expression expression = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expression not found for ID: " + id));

        log.info("evaluateExpression() -> Fetched Expression: " + expression.getExpression());

        if (variables == null || variables.isEmpty()) {
            throw new IllegalArgumentException("Variables cannot be null or empty");
        }

        log.info("evaluateExpression() -> Variables for evaluation: " + variables);
        JexlEngine jexl = new JexlBuilder().silent(false).create();
        JexlExpression jexlExpression = jexl.createExpression(expression.getExpression());
        JexlContext context = new MapContext(variables);

        try {
            Boolean result = (Boolean) jexlExpression.evaluate(context);
            log.info("evaluateExpression() -> Evaluation Result: " + result);
            return result;
        } catch (Exception e) {
            log.error("evaluateExpression() -> Error during expression evaluation: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error during expression evaluation: " + e.getMessage());
        }
    }
}
