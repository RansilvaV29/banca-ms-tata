package com.banca.cuentas.repository;

import com.banca.cuentas.domain.model.Cuenta;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CuentaRepository extends JpaRepository<Cuenta, Long> {

    Optional<Cuenta> findByNumeroCuenta(String numeroCuenta);

    boolean existsByNumeroCuenta(String numeroCuenta);

    List<Cuenta> findByClienteIdOrderByNumeroCuenta(String clienteId);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    @Query("select c from Cuenta c where c.numeroCuenta = :numeroCuenta")
    Optional<Cuenta> bloquearPorNumeroCuenta(@Param("numeroCuenta") String numeroCuenta);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    @Query("select c from Cuenta c where c.id = :id")
    Optional<Cuenta> bloquearPorId(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Cuenta c set c.estado = false where c.clienteId = :clienteId and c.estado = true")
    int desactivarPorCliente(@Param("clienteId") String clienteId);
}
