package com.banca.cuentas.integration;

import com.banca.cuentas.messaging.ClienteEvent;
import com.banca.cuentas.messaging.ClienteEventListener;
import com.banca.cuentas.messaging.TipoEventoCliente;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EstadoCuentaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ClienteEventListener clienteEventListener;

    @Test
    void flujoCompletoDeCuentasMovimientosYReporte() throws Exception {
        // 1. Llega (simulado) el evento que publicaría clientes-service por RabbitMQ
        clienteEventListener.onClienteEvent(new ClienteEvent(UUID.randomUUID().toString(),
                TipoEventoCliente.CLIENTE_CREADO, "mmontalvo", "Marianela Montalvo", true, Instant.now()));

        // 2. Creación de cuentas (casos de uso 2)
        crearCuenta("225487", "Corriente", 100);
        crearCuenta("496825", "Ahorros", 540);

        // 3. Movimientos (caso de uso 4)
        registrarMovimiento("225487", 600)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saldo").value(700.0))
                .andExpect(jsonPath("$.tipoMovimiento").value("Deposito"));
        registrarMovimiento("496825", -540)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saldo").value(0.0))
                .andExpect(jsonPath("$.tipoMovimiento").value("Retiro"));

        // 4. F3: sin saldo
        registrarMovimiento("496825", -1)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.mensaje").value("Saldo no disponible"));

        // 5. F4: estado de cuenta del día
        String hoy = LocalDate.now().toString();
        mockMvc.perform(get("/api/reportes")
                        .param("fecha", hoy + "," + hoy)
                        .param("cliente", "mmontalvo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cliente").value("Marianela Montalvo"))
                .andExpect(jsonPath("$.cuentas", hasSize(2)))
                .andExpect(jsonPath("$.cuentas[0].numeroCuenta").value("225487"))
                .andExpect(jsonPath("$.cuentas[0].saldoDisponible").value(700.0))
                .andExpect(jsonPath("$.cuentas[0].movimientos", hasSize(1)))
                .andExpect(jsonPath("$.cuentas[1].saldoDisponible").value(0.0))
                .andExpect(jsonPath("$.totalCreditos").value(600.0))
                .andExpect(jsonPath("$.totalDebitos").value(540.0));

        // 6. Mismo reporte en el formato plano del enunciado
        mockMvc.perform(get("/api/reportes")
                        .param("fecha", hoy + "," + hoy)
                        .param("cliente", "mmontalvo")
                        .param("formato", "plano"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0]['Cliente']").value("Marianela Montalvo"))
                .andExpect(jsonPath("$[0]['Numero Cuenta']").value("225487"))
                .andExpect(jsonPath("$[0]['Movimiento']").value(600.0))
                .andExpect(jsonPath("$[0]['Saldo Disponible']").value(700.0));
    }

    @Test
    void noSePuedeCrearCuentaParaClienteDesconocido() throws Exception {
        mockMvc.perform(post("/api/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"numeroCuenta":"999999","tipoCuenta":"Ahorros","saldoInicial":10,"clienteId":"fantasma"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("CLIENTE_NO_ENCONTRADO"));
    }

    private void crearCuenta(String numero, String tipo, int saldoInicial) throws Exception {
        mockMvc.perform(post("/api/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"numeroCuenta":"%s","tipoCuenta":"%s","saldoInicial":%d,"estado":true,"clienteId":"mmontalvo"}
                                """.formatted(numero, tipo, saldoInicial)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saldoDisponible").value((double) saldoInicial));
    }

    private org.springframework.test.web.servlet.ResultActions registrarMovimiento(String numero, int valor)
            throws Exception {
        return mockMvc.perform(post("/api/movimientos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"numeroCuenta":"%s","valor":%d}
                        """.formatted(numero, valor)));
    }
}
