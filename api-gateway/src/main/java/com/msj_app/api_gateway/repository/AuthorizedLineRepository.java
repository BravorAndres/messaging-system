package com.msj_app.api_gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.msj_app.api_gateway.entity.AuthorizedLine;

public interface AuthorizedLineRepository extends JpaRepository<AuthorizedLine, Long> {
    boolean existsByLineNumberAndActiveTrue(String lineNumber);
    
    String findByLineNumberAndActiveTrue(String origin);
}   