package com.microservice.clientes.Clientes_microservice_curse.service;

import com.microservice.clientes.Clientes_microservice_curse.Clients.GroupServiceClient;
import com.microservice.clientes.Clientes_microservice_curse.Clients.UserServiceClient;
import com.microservice.clientes.Clientes_microservice_curse.dto.GroupDTO;
import com.microservice.clientes.Clientes_microservice_curse.dto.ProjetoDTO;
import com.microservice.clientes.Clientes_microservice_curse.dto.UserDTO;
import com.microservice.clientes.Clientes_microservice_curse.enums.StatusProjetoModel;
import com.microservice.clientes.Clientes_microservice_curse.exception.ProjetoNaoEncontradoException;
import com.microservice.clientes.Clientes_microservice_curse.model.ProjetoModel;
import com.microservice.clientes.Clientes_microservice_curse.repository.ProjetoRepository;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GestaoProjetoService {

    private static final Logger log = LoggerFactory.getLogger(GestaoProjetoService.class);
    private final ProjetoRepository projetoRepository;
    private final UserServiceClient userServiceClient;
    private final GroupServiceClient groupServiceClient;

    @Autowired
    public GestaoProjetoService(ProjetoRepository projetoRepository,
                                UserServiceClient userServiceClient,
                                GroupServiceClient groupServiceClient) {
        this.projetoRepository = projetoRepository;
        this.userServiceClient = userServiceClient;
        this.groupServiceClient = groupServiceClient;
    }

    private ProjetoDTO toDTO(ProjetoModel projeto) {
        if (projeto == null) return null;
        ProjetoDTO dto = new ProjetoDTO();
        dto.setId(projeto.getId());
        dto.setNome(projeto.getNome());
        dto.setObjetivo(projeto.getObjetivo());
        dto.setEscopoResumo(projeto.getEscopoResumo());
        dto.setPublicoAlvo(projeto.getPublicoAlvo());
        dto.setDataInicio(projeto.getDataInicio());
        dto.setStatus(projeto.getStatus());
        dto.setProfessorCriadorId(projeto.getProfessorCriadorId());
        dto.setGrupoId(projeto.getGrupoId());
        if (projeto.getProfessorCriadorId() != null) {
            try {
                UserDTO user = userServiceClient.getUsuarioById(projeto.getProfessorCriadorId());
                if (user != null) {
                    dto.setNomeProfessorCriador(user.getNome());
                }
            } catch (FeignException e) {
                log.error("Falha ao buscar nome do professor ID {}: {}", projeto.getProfessorCriadorId(), e.getMessage());
                dto.setNomeProfessorCriador("Professor não encontrado/serviço indisponível");
            }
        }
        if (projeto.getGrupoId() != null) {
            try {
                GroupDTO group = groupServiceClient.getGrupoById(projeto.getGrupoId());
                if (group != null) {
                    dto.setNomeGrupo(group.getNome());
                }
            } catch (FeignException e) {
                log.error("Falha ao buscar nome do grupo ID {}: {}", projeto.getGrupoId(), e.getMessage());
                dto.setNomeGrupo("Grupo não encontrado/serviço indisponível");
            }
        }
        return dto;
    }

    private ProjetoModel toModel(ProjetoDTO dto) {
        if (dto == null) return null;
        ProjetoModel model = new ProjetoModel();
        model.setNome(dto.getNome());
        model.setObjetivo(dto.getObjetivo());
        model.setEscopoResumo(dto.getEscopoResumo());
        model.setPublicoAlvo(dto.getPublicoAlvo());
        model.setDataInicio(dto.getDataInicio());
        model.setStatus(dto.getStatus() != null ? dto.getStatus() : StatusProjetoModel.EM_ANALISE);
        model.setProfessorCriadorId(dto.getProfessorCriadorId());
        model.setGrupoId(dto.getGrupoId());
        return model;
    }

    @Transactional
    public ProjetoDTO solicitarNovoProjeto(ProjetoDTO projetoDTO, Long professorIdRequisitante) {
        if (projetoRepository.findByNome(projetoDTO.getNome()).isPresent()) {
            throw new IllegalArgumentException("Já existe um projeto com o nome: " + projetoDTO.getNome());
        }

        ProjetoModel novoProjeto = toModel(projetoDTO);
        novoProjeto.setProfessorCriadorId(professorIdRequisitante);
        novoProjeto.setStatus(StatusProjetoModel.EM_ANALISE);

        ProjetoModel projetoSalvo = projetoRepository.save(novoProjeto);
        log.info("Projeto solicitado: {} pelo professor ID: {}", projetoSalvo.getNome(), professorIdRequisitante);
        return toDTO(projetoSalvo);
    }

    @Transactional(readOnly = true)
    public List<ProjetoDTO> listarTodos() {
        return projetoRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<ProjetoDTO> buscarPorId(Long id) {
        return projetoRepository.findById(id).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Optional<ProjetoDTO> buscarPorNome(String nome) {
        return projetoRepository.findByNome(nome).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<ProjetoDTO> buscarPorStatus(StatusProjetoModel status) {
        return projetoRepository.findByStatus(status).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProjetoDTO> buscarPorProfessorEStatus(Long professorId, StatusProjetoModel status) {
        return projetoRepository.findByProfessorCriadorIdAndStatus(professorId, status)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    private ProjetoModel mudarStatusProjeto(Long projetoId, StatusProjetoModel novoStatus, String acaoOriginadora) {
        ProjetoModel projeto = projetoRepository.findById(projetoId)
                .orElseThrow(() -> new ProjetoNaoEncontradoException("Projeto não encontrado com ID: " + projetoId));

        log.info("Tentando mudar status do projeto ID {} de {} para {} (Ação: {})",
                projetoId, projeto.getStatus(), novoStatus, acaoOriginadora);


        projeto.setStatus(novoStatus);
        ProjetoModel projetoAtualizado = projetoRepository.save(projeto);

        if (novoStatus == StatusProjetoModel.FINALIZADO && projetoAtualizado.getGrupoId() != null) {
            log.info("Projeto ID {} finalizado. Notificar group-service para liberar grupo ID {}.",
                    projetoId, projetoAtualizado.getGrupoId());
        }
        return projetoAtualizado;
    }

    @Transactional
    public ProjetoDTO aprovarProjeto(Long projetoId) {
        return toDTO(mudarStatusProjeto(projetoId, StatusProjetoModel.EM_ANDAMENTO, "APROVAR_PROJETO_ADMIN"));
    }

    @Transactional
    public ProjetoDTO recusarProjeto(Long projetoId) {
        return toDTO(mudarStatusProjeto(projetoId, StatusProjetoModel.RECUSADO, "RECUSAR_PROJETO_ADMIN"));
    }

    @Transactional
    public ProjetoDTO finalizarProjeto(Long projetoId, Long requisitanteId, String roleRequisitante) {
        ProjetoModel projeto = projetoRepository.findById(projetoId)
                .orElseThrow(() -> new ProjetoNaoEncontradoException("Projeto não encontrado com ID: " + projetoId));

        if (!roleRequisitante.equals("ROLE_ADMIN") && !projeto.getProfessorCriadorId().equals(requisitanteId)) {
            throw new SecurityException("Usuário não tem permissão para finalizar este projeto.");
        }
        if (projeto.getStatus() != StatusProjetoModel.EM_ANDAMENTO) {
            throw new IllegalStateException("Projeto só pode ser finalizado se estiver EM_ANDAMENTO. Status atual: " + projeto.getStatus());
        }
        return toDTO(mudarStatusProjeto(projetoId, StatusProjetoModel.FINALIZADO, "FINALIZAR_PROJETO"));
    }

    @Transactional
    public ProjetoDTO atualizarDados(Long projetoId, ProjetoDTO projetoDTO, Long requisitanteId, String roleRequisitante) {
        ProjetoModel projetoExistente = projetoRepository.findById(projetoId)
                .orElseThrow(() -> new ProjetoNaoEncontradoException("Projeto não encontrado com ID: " + projetoId));

        if (!roleRequisitante.equals("ROLE_ADMIN")) {
            if (!projetoExistente.getProfessorCriadorId().equals(requisitanteId) ||
                    projetoExistente.getStatus() != StatusProjetoModel.EM_ANALISE) {
                throw new SecurityException("Usuário não tem permissão para editar este projeto neste estado.");
            }
        }

        if (projetoDTO.getNome() != null) {
            Optional<ProjetoModel> outroComMesmoNome = projetoRepository.findByNome(projetoDTO.getNome());
            if (outroComMesmoNome.isPresent() && !outroComMesmoNome.get().getId().equals(projetoId)) {
                throw new IllegalArgumentException("Outro projeto já existe com o nome: " + projetoDTO.getNome());
            }
            projetoExistente.setNome(projetoDTO.getNome());
        }
        if (projetoDTO.getObjetivo() != null) projetoExistente.setObjetivo(projetoDTO.getObjetivo());
        if (projetoDTO.getEscopoResumo() != null) projetoExistente.setEscopoResumo(projetoDTO.getEscopoResumo());
        if (projetoDTO.getPublicoAlvo() != null) projetoExistente.setPublicoAlvo(projetoDTO.getPublicoAlvo());
        if (projetoDTO.getDataInicio() != null) projetoExistente.setDataInicio(projetoDTO.getDataInicio());

        ProjetoModel projetoAtualizado = projetoRepository.save(projetoExistente);
        return toDTO(projetoAtualizado);
    }

    @Transactional
    public void deletarProjeto(Long projetoId) {
        if (!projetoRepository.existsById(projetoId)) {
            throw new ProjetoNaoEncontradoException("Projeto não encontrado com ID: " + projetoId);
        }

        projetoRepository.deleteById(projetoId);
        log.info("Projeto ID {} deletado.", projetoId);
    }

    @Transactional
    public ProjetoDTO associarGrupo(Long projetoId, Long grupoId) {
        ProjetoModel projeto = projetoRepository.findById(projetoId)
                .orElseThrow(() -> new ProjetoNaoEncontradoException("Projeto não encontrado com ID: " + projetoId));

        if (projeto.getStatus() != StatusProjetoModel.EM_ANDAMENTO) {
            throw new IllegalStateException("Projeto deve estar EM_ANDAMENTO para associar um grupo. Status atual: " + projeto.getStatus());
        }
        if (projeto.getGrupoId() != null) {
            throw new IllegalStateException("Projeto já possui um grupo associado (ID: " + projeto.getGrupoId() + ").");
        }

        log.info("Simulando associação: Projeto ID {} será associado ao Grupo ID {}.", projetoId, grupoId);
        projeto.setGrupoId(grupoId);
        ProjetoModel projetoAtualizado = projetoRepository.save(projeto);

        return toDTO(projetoAtualizado);
    }

    @Transactional
    public ProjetoDTO desassociarGrupo(Long projetoId) {
        ProjetoModel projeto = projetoRepository.findById(projetoId)
                .orElseThrow(() -> new ProjetoNaoEncontradoException("Projeto não encontrado com ID: " + projetoId));

        Long grupoIdAtual = projeto.getGrupoId();
        if (grupoIdAtual == null) {
            throw new IllegalStateException("Projeto não possui grupo associado.");
        }


        log.info("Simulando desassociação: Grupo ID {} será liberado do Projeto ID {}.", grupoIdAtual, projetoId);
        projeto.setGrupoId(null);
        return toDTO(projetoRepository.save(projeto));
    }
}