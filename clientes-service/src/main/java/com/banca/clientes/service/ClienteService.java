package com.banca.clientes.service;

import com.banca.clientes.dto.ClientePatchRequest;
import com.banca.clientes.dto.ClienteRequest;
import com.banca.clientes.dto.ClienteResponse;

import java.util.List;

public interface ClienteService {
    List<ClienteResponse> listar();

    ClienteResponse obtener(String clienteId);

    ClienteResponse crear(ClienteRequest request);

    ClienteResponse actualizar(String clienteId, ClienteRequest request);

    ClienteResponse actualizarParcial(String clienteId, ClientePatchRequest request);

    void eliminar(String clienteId);
}
