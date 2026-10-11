package com.banca.clientes.dto;

import com.banca.clientes.domain.model.Genero;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClienteRequest(

        @Size(max = 50)
        @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "solo admite letras, números, punto, guion y guion bajo")
        String clienteId,

        @NotBlank @Size(max = 100)
        String nombre,

        Genero genero,

        @Min(0) @Max(120)
        Integer edad,

        @NotBlank
        @Pattern(regexp = "^\\d{10,13}$", message = "debe tener entre 10 y 13 dígitos")
        String identificacion,

        @NotBlank @Size(max = 200)
        String direccion,

        @NotBlank
        @Pattern(regexp = "^\\d{7,15}$", message = "debe tener entre 7 y 15 dígitos")
        String telefono,

        @NotBlank @Size(min = 4, max = 50)
        String contrasena,

        Boolean estado
) {
}
