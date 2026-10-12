package com.banca.cuentas.mapper;

import com.banca.cuentas.domain.model.Movimiento;
import com.banca.cuentas.dto.MovimientoResponse;
import org.springframework.stereotype.Component;

@Component
public class MovimientoMapper {

    public MovimientoResponse toResponse(Movimiento movimiento) {
        return new MovimientoResponse(
                movimiento.getId(),
                movimiento.getFecha(),
                movimiento.getCuenta().getNumeroCuenta(),
                movimiento.getTipoMovimiento(),
                movimiento.getValor(),
                movimiento.getSaldo()
        );
    }
}
