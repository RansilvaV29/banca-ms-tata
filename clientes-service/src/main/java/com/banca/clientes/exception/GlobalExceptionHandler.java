package com.banca.clientes.exception;

import com.banca.clientes.domain.exception.NegocioException;
import com.banca.clientes.domain.exception.RecursoDuplicadoException;
import com.banca.clientes.domain.exception.RecursoNoEncontradoException;
import com.banca.clientes.domain.exception.ReglaNegocioException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.List;

/**
 * Centraliza la traducción de excepciones a respuestas HTTP (los controladores quedan limpios).
 * Cada tipo de excepción de negocio tiene su código HTTP; los errores inesperados devuelven 500
 * sin filtrar detalles internos al cliente, pero quedan completos en el log.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(RecursoNoEncontradoException ex, HttpServletRequest req) {
        return negocio(HttpStatus.NOT_FOUND, ex, req);
    }

    @ExceptionHandler(RecursoDuplicadoException.class)
    public ResponseEntity<ErrorResponse> duplicado(RecursoDuplicadoException ex, HttpServletRequest req) {
        return negocio(HttpStatus.CONFLICT, ex, req);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponse> reglaNegocio(ReglaNegocioException ex, HttpServletRequest req) {
        return negocio(HttpStatus.UNPROCESSABLE_CONTENT, ex, req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .sorted()
                .toList();
        return construir(HttpStatus.BAD_REQUEST, "VALIDACION_FALLIDA",
                "La petición contiene datos inválidos", req, detalles);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> cuerpoInvalido(HttpMessageNotReadableException ex, HttpServletRequest req) {
        // Si el error viene de una validación propia (p. ej. género inválido) se muestra ese mensaje.
        String mensaje = ex.getMostSpecificCause() instanceof IllegalArgumentException causa
                ? causa.getMessage()
                : "El cuerpo de la petición no es un JSON válido o tiene valores no permitidos";
        return construir(HttpStatus.BAD_REQUEST, "CUERPO_INVALIDO", mensaje, req, List.of());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> metodoNoSoportado(HttpRequestMethodNotSupportedException ex,
                                                           HttpServletRequest req) {
        return construir(HttpStatus.METHOD_NOT_ALLOWED, "METODO_NO_SOPORTADO", ex.getMessage(), req, List.of());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> rutaNoExiste(NoResourceFoundException ex, HttpServletRequest req) {
        return construir(HttpStatus.NOT_FOUND, "RUTA_NO_ENCONTRADA", "El recurso solicitado no existe", req, List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> integridad(DataIntegrityViolationException ex, HttpServletRequest req) {
        log.warn("Violación de integridad de datos: {}", ex.getMostSpecificCause().getMessage());
        return construir(HttpStatus.CONFLICT, "INTEGRIDAD_DATOS",
                "La operación viola una restricción de datos (registro duplicado o relacionado)", req, List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> inesperado(Exception ex, HttpServletRequest req) {
        log.error("Error no controlado en {} {}", req.getMethod(), req.getRequestURI(), ex);
        return construir(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO",
                "Ocurrió un error inesperado, intente nuevamente", req, List.of());
    }

    private ResponseEntity<ErrorResponse> negocio(HttpStatus status, NegocioException ex, HttpServletRequest req) {
        return construir(status, ex.getCodigo(), ex.getMessage(), req, List.of());
    }

    private ResponseEntity<ErrorResponse> construir(HttpStatus status, String codigo, String mensaje,
                                                    HttpServletRequest req, List<String> detalles) {
        ErrorResponse body = new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(),
                codigo, mensaje, req.getRequestURI(), detalles);
        return ResponseEntity.status(status).body(body);
    }
}
