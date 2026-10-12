package com.banca.cuentas.repository;

import com.banca.cuentas.domain.model.Movimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    /** @EntityGraph trae la cuenta en la misma consulta (evita el problema N+1). */
    @Override
    @EntityGraph(attributePaths = "cuenta")
    Page<Movimiento> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "cuenta")
    Page<Movimiento> findByCuentaNumeroCuenta(String numeroCuenta, Pageable pageable);

    @EntityGraph(attributePaths = "cuenta")
    Optional<Movimiento> findWithCuentaById(Long id);

    @Query("select m.cuenta.id from Movimiento m where m.id = :id")
    Optional<Long> buscarIdCuenta(@Param("id") Long id);

    boolean existsByCuentaId(Long cuentaId);

    Optional<Movimiento> findFirstByCuentaIdOrderByIdDesc(Long cuentaId);


    @Query("""
            select m from Movimiento m
              join fetch m.cuenta c
             where c.clienteId = :clienteId
               and m.fecha >= :desde
               and m.fecha < :hasta
             order by c.numeroCuenta, m.fecha, m.id
            """)
    List<Movimiento> buscarParaEstadoCuenta(@Param("clienteId") String clienteId,
                                            @Param("desde") LocalDateTime desde,
                                            @Param("hasta") LocalDateTime hasta);
}
