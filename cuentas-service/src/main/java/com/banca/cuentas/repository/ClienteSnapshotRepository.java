package com.banca.cuentas.repository;

import com.banca.cuentas.domain.model.ClienteSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteSnapshotRepository extends JpaRepository<ClienteSnapshot, String> {
}
