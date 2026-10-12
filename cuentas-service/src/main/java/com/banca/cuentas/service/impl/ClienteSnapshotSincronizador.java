package com.banca.cuentas.service.impl;

import com.banca.cuentas.domain.model.ClienteSnapshot;
import com.banca.cuentas.messaging.ClienteEvent;
import com.banca.cuentas.messaging.TipoEventoCliente;
import com.banca.cuentas.repository.ClienteSnapshotRepository;
import com.banca.cuentas.repository.CuentaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClienteSnapshotSincronizador {

    private final ClienteSnapshotRepository clienteSnapshotRepository;
    private final CuentaRepository cuentaRepository;

    @Transactional
    public void procesar(ClienteEvent evento) {
        LocalDateTime momento = LocalDateTime.ofInstant(evento.ocurridoEn(), ZoneOffset.UTC);
        ClienteSnapshot existente = clienteSnapshotRepository.findById(evento.clienteId()).orElse(null);

        if (existente != null && existente.esPosteriorA(momento)) {
            log.warn("Evento {} del cliente {} descartado: es anterior al último aplicado",
                    evento.eventoId(), evento.clienteId());
            return;
        }

        boolean eliminado = evento.tipo() == TipoEventoCliente.CLIENTE_ELIMINADO;
        Boolean estado = eliminado ? Boolean.FALSE : evento.estado();

        if (existente == null) {
            clienteSnapshotRepository.save(new ClienteSnapshot(evento.clienteId(), evento.nombre(), estado, momento));
        } else {
            existente.actualizar(evento.nombre(), estado, momento);
        }

        if (eliminado) {
            int desactivadas = cuentaRepository.desactivarPorCliente(evento.clienteId());
            log.info("Cliente {} eliminado: {} cuenta(s) desactivada(s)", evento.clienteId(), desactivadas);
        }
    }
}
