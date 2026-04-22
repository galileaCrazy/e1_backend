package com.clinica.mi_app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public class ConsultorioRequest {

    @NotNull
    private UUID organizacionId;

    @NotBlank
    @Size(max = 120)
    private String nombre;

    private String direccion;

    @Size(max = 20)
    private String telefono;

    public UUID getOrganizacionId() { return organizacionId; }
    public void setOrganizacionId(UUID organizacionId) { this.organizacionId = organizacionId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
}
