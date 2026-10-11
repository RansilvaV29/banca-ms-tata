package com.banca.clientes.dto;

import com.banca.clientes.domain.model.Genero;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClientePatchRequest(

        @Size(min = 1, max = 100)
        String nombre,

        Genero genero,

        @Min(0) @Max(120)
        Integer edad,

        @Size(min = 1, max = 200)
        String direccion,

        @Pattern(regexp = "^\\d{7,15}$", message = "debe tener entre 7 y 15 dígitos")
        String telefono,

        @Size(min = 4, max = 50)
        String contrasena,

        Boolean estado
) {
}
