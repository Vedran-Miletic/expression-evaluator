package com.leapwise.expressionevaluator.controller;

import com.leapwise.expressionevaluator.model.Expression;
import com.leapwise.expressionevaluator.service.ExpressionService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class ExpressionController {

    private final ExpressionService expressionService;

    public ExpressionController(ExpressionService expressionService) {
        this.expressionService = expressionService;
    }

    /**
     * Saves a logical expression or returns the UUID if it already exists.
     * Validates input and delegates processing to the service layer.
     *
     * @param expression the logical expression to save
     * @return ResponseEntity with the UUID of the expression
     */
    @PostMapping("/expression")
    public ResponseEntity<String> saveExpression(@Valid @RequestBody Expression expression) {
        log.info("saveExpression() -> Received request to save expression: {}", expression.getExpression());
        String id = expressionService.saveExpression(expression.getName(), expression.getExpression());
        log.info("saveExpression() -> Expression saved with ID: {}", id);
        return ResponseEntity.ok(id);
    }

    /**
     * Retrieves all saved logical expressions from the database.
     *
     * <p>This endpoint handles HTTP GET requests and returns a list of all expressions stored in the database.
     * Delegates the retrieval process to the service layer.</p>
     *
     * @return ResponseEntity containing a list of all expressions
     */
    @GetMapping("/all-expressions")
    public ResponseEntity<List<Expression>> getAllExpressions(){
        List<Expression> allExpressions = expressionService.getAll();
        return ResponseEntity.ok(allExpressions);
    }

    /**
     * Evaluates a logical expression using the provided variables.
     *
     * <p>This endpoint handles HTTP POST requests. It expects a JSON payload containing:
     * <ul>
     *   <li><b>id</b>: The unique identifier of the expression to evaluate.</li>
     *   <li><b>variables</b>: A map of variables used in the evaluation.</li>
     * </ul>
     * </p>
     *
     * <p>If the required fields are missing, returns a 400 Bad Request response.
     * Delegates evaluation to the service layer and returns the result or an error response.</p>
     *
     * @param request a map containing the "id" of the expression and the variables for evaluation
     * @return ResponseEntity with the result of the evaluation or an error message
     */
    @PostMapping("/evaluate")
    public ResponseEntity<?> evaluateExpression(@RequestBody Map<String, Object> request) {
        if (!request.containsKey("id") || !request.containsKey("variables")) {
            return ResponseEntity.badRequest().body("Missing required fields: 'id' or 'variables'");
        }

        String id = (String) request.get("id");
        Map<String, Object> variables = (Map<String, Object>) request.get("variables");

        try {
            boolean result = expressionService.evaluateExpression(id, variables);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error during evaluation: " + e.getMessage());
        }
    }

}
