package com.banca.cuentas.exception;

import com.banca.cuentas.domain.exception.ConflictoException;
import com.banca.cuentas.domain.exception.NegocioException;
import com.banca.cuentas.domain.exception.RecursoNoEncontradoException;
import com.banca.cuentas.domain.exception.ReglaNegocioException;
import com.banca.cuentas.domain.exception.SolicitudInvalidaException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.List;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(RecursoNoEncontradoException ex, HttpServletRequest req) {
        return negocio(HttpStatus.NOT_FOUND, ex, req);
    }

    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<ErrorResponse> conflicto(ConflictoException ex, HttpServletRequest req) {
        return negocio(HttpStatus.CONFLICT, ex, req);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponse> reglaNegocio(ReglaNegocioException ex, HttpServletRequest req) {
        log.info("Regla de negocio rechazada [{}]: {}", ex.getCodigo(), ex.getMessage());
        return negocio(HttpStatus.UNPROCESSABLE_CONTENT, ex, req);
    }

    @ExceptionHandler(SolicitudInvalidaException.class)
    public ResponseEntity<ErrorResponse> solicitudInvalida(SolicitudInvalidaException ex, HttpServletRequest req) {
        return negocio(HttpStatus.BAD_REQUEST, ex, req);
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
        // Si el error viene de una validación propia (p. ej. tipo de cuenta inválido) se muestra ese mensaje.
        String mensaje = ex.getMostSpecificCause() instanceof IllegalArgumentException causa
                ? causa.getMessage()
                : "El cuerpo de la petición no es un JSON válido o tiene valores no permitidos";
        return construir(HttpStatus.BAD_REQUEST, "CUERPO_INVALIDO", mensaje, req, List.of());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> parametroFaltante(MissingServletRequestParameterException ex,
                                                           HttpServletRequest req) {
        return construir(HttpStatus.BAD_REQUEST, "PARAMETRO_REQUERIDO",
                "Falta el parámetro obligatorio '" + ex.getParameterName() + "'", req, List.of());
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, PropertyReferenceException.class})
    public ResponseEntity<ErrorResponse> parametroInvalido(Exception ex, HttpServletRequest req) {
        return construir(HttpStatus.BAD_REQUEST, "PARAMETRO_INVALIDO",
                "Uno de los parámetros de la petición tiene un valor inválido", req, List.of());
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

    @ExceptionHandler({OptimisticLockingFailureException.class, PessimisticLockingFailureException.class})
    public ResponseEntity<ErrorResponse> concurrencia(Exception ex, HttpServletRequest req) {
        log.warn("Conflicto de concurrencia: {}", ex.getMessage());
        return construir(HttpStatus.CONFLICT, "CONFLICTO_CONCURRENCIA",
                "La cuenta está siendo modificada por otra operación, intente nuevamente", req, List.of());
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
