package com.microservice.clientes.Clientes_microservice_curse.repository;

import com.microservice.clientes.Clientes_microservice_curse.model.ProjetoModel;
import com.microservice.clientes.Clientes_microservice_curse.enums.StatusProjetoModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjetoRepository extends JpaRepository<ProjetoModel, Long> {

    Optional<ProjetoModel> findByNome(String nome);

    List<ProjetoModel> findByStatus(StatusProjetoModel status);

    List<ProjetoModel> findByProfessorCriadorIdAndStatus(Long professorCriadorId, StatusProjetoModel status);

    List<ProjetoModel> findByProfessorCriadorId(Long professorCriadorId);

    Optional<ProjetoModel> findByGrupoId(Long grupoId);
}
