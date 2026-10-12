package com.banca.clientes.controller;

import com.banca.clientes.domain.exception.RecursoNoEncontradoException;
import com.banca.clientes.domain.model.Genero;
import com.banca.clientes.dto.ClienteRequest;
import com.banca.clientes.dto.ClienteResponse;
import com.banca.clientes.service.ClienteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Pruebas unitarias de los endpoints: solo la capa web, el servicio se simula. */
@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JsonMapper jsonMapper;
    @MockitoBean
    private ClienteService clienteService;

    @Test
    void postClienteValidoRetorna201ConLocationYSinContrasena() throws Exception {
        ClienteRequest request = new ClienteRequest("jlema", "Jose Lema", Genero.MASCULINO, 35,
                "1712345678", "Otavalo sn y principal", "098254785", "1234", true);
        when(clienteService.crear(any())).thenReturn(new ClienteResponse("jlema", "Jose Lema",
                Genero.MASCULINO, 35, "1712345678", "Otavalo sn y principal", "098254785", true));

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/clientes/jlema"))
                .andExpect(jsonPath("$.clienteId").value("jlema"))
                .andExpect(jsonPath("$.contrasena").doesNotExist());
    }

    @Test
    void postClienteInvalidoRetorna400ConDetalles() throws Exception {
        String sinNombreNiContrasena = """
                {"identificacion":"1712345678","direccion":"Otavalo","telefono":"098254785"}
                """;

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sinNombreNiContrasena))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACION_FALLIDA"))
                .andExpect(jsonPath("$.detalles.length()").value(2));
    }

    @Test
    void getClienteInexistenteRetorna404() throws Exception {
        when(clienteService.obtener("nadie")).thenThrow(RecursoNoEncontradoException.cliente("nadie"));

        mockMvc.perform(get("/api/clientes/nadie"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("CLIENTE_NO_ENCONTRADO"));
    }
}
