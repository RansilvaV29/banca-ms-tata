package com.banca.clientes.mapper;

import com.banca.clientes.domain.model.Cliente;
import com.banca.clientes.dto.ClienteResponse;
import org.springframework.stereotype.Component;

//para no exponer el modelo en la api
@Component
public class ClienteMapper {

    public ClienteResponse toResponse(Cliente cliente) {
        return new ClienteResponse(
                cliente.getClienteId(),
                cliente.getNombre(),
                cliente.getGenero(),
                cliente.getEdad(),
                cliente.getIdentificacion(),
                cliente.getDireccion(),
                cliente.getTelefono(),
                cliente.getEstado()
        );
    }
}
