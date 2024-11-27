package com.leapwise.expressionevaluator.repository;

import com.leapwise.expressionevaluator.model.Expression;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExpressionRepository extends JpaRepository<Expression, String> {

    Optional<Expression> findByName(String name);
    Optional<Expression> findByExpression(String expression);
}