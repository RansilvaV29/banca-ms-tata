package com.banca.cuentas.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

public record PaginaResponse<T>(
        List<T> contenido,
        int pagina,
        int tamanio,
        long totalElementos,
        int totalPaginas
) {

    public static <E, T> PaginaResponse<T> de(Page<E> pagina, Function<? super E, ? extends T> mapper) {
        List<T> contenido = pagina.getContent().stream()
                .<T>map(mapper)
                .toList();
        return new PaginaResponse<>(contenido, pagina.getNumber(), pagina.getSize(),
                pagina.getTotalElements(), pagina.getTotalPages());
    }
}
