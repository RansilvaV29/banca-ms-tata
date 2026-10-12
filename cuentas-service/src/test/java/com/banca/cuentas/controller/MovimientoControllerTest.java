package com.banca.cuentas.controller;

import com.banca.cuentas.domain.exception.SaldoNoDisponibleException;
import com.banca.cuentas.domain.model.TipoMovimiento;
import com.banca.cuentas.dto.MovimientoResponse;
import com.banca.cuentas.service.MovimientoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MovimientoController.class)
class MovimientoControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private MovimientoService movimientoService;

    @Test
    void postMovimientoRetorna201() throws Exception {
        when(movimientoService.registrar(any())).thenReturn(new MovimientoResponse(1L,
                LocalDateTime.of(2022, 2, 10, 10, 0), "225487", TipoMovimiento.DEPOSITO,
                new BigDecimal("600.00"), new BigDecimal("700.00")));

        mockMvc.perform(post("/api/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"numeroCuenta":"225487","valor":600}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoMovimiento").value("Deposito"))
                .andExpect(jsonPath("$.saldo").value(700.00));
    }

    @Test
    void postMovimientoSinSaldoRetorna422ConMensajeSaldoNoDisponible() throws Exception {
        when(movimientoService.registrar(any())).thenThrow(new SaldoNoDisponibleException());

        mockMvc.perform(post("/api/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"numeroCuenta":"495878","valor":-1000}
                                """))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.codigo").value("SALDO_NO_DISPONIBLE"))
                .andExpect(jsonPath("$.mensaje").value("Saldo no disponible"));
    }

    @Test
    void postMovimientoSinValorRetorna400() throws Exception {
        mockMvc.perform(post("/api/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"numeroCuenta":"225487"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACION_FALLIDA"))
                .andExpect(jsonPath("$.detalles[0]").value(startsWith("valor:")));
    }
}
