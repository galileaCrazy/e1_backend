package com.clinica.mi_app.exception;

import java.time.LocalDateTime;

public class ErrorResponse {

    private int status;
    private String mensaje;
    private String campo;
    private LocalDateTime timestamp;

    public ErrorResponse(int status, String mensaje) {
        this.status = status;
        this.mensaje = mensaje;
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(int status, String mensaje, String campo) {
        this.status = status;
        this.mensaje = mensaje;
        this.campo = campo;
        this.timestamp = LocalDateTime.now();
    }

    public int getStatus() { return status; }
    public String getMensaje() { return mensaje; }
    public String getCampo() { return campo; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
