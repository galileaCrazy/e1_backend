package com.clinica.mi_app.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String recurso, String id) {
        super(recurso + " con id " + id + " no encontrado");
    }
}
