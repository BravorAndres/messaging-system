package com.msj_app.api_gateway.service;

import org.springframework.stereotype.Service;
import com.msj_app.api_gateway.repository.AuthorizedLineRepository;

@Service
public class AuthorizedLineService {

    private final AuthorizedLineRepository repository;

    public AuthorizedLineService(AuthorizedLineRepository repository) {
        this.repository = repository;
    }

    public void validateOrigin(String origin) {
        if (repository.findByLineNumberAndActiveTrue(origin).isEmpty()) {
            throw new IllegalArgumentException("Origin line not authorized");
        }
    }
}
