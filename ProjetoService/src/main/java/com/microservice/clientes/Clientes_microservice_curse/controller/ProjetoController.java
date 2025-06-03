package com.microservice.clientes.Clientes_microservice_curse.controller;

import com.microservice.clientes.Clientes_microservice_curse.dto.ProjetoDTO;
import com.microservice.clientes.Clientes_microservice_curse.enums.StatusProjetoModel;
import com.microservice.clientes.Clientes_microservice_curse.exception.ProjetoNaoEncontradoException;
import com.microservice.clientes.Clientes_microservice_curse.service.GestaoProjetoService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt; // Importante para acessar claims
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken; // Alternativa
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/projetos")
public class ProjetoController {

    private static final Logger log = LoggerFactory.getLogger(ProjetoController.class);
    private final GestaoProjetoService gestaoProjetoService;

    @Autowired
    public ProjetoController(GestaoProjetoService gestaoProjetoService) {
        this.gestaoProjetoService = gestaoProjetoService;
    }

    private Long getUsuarioIdFromAuthentication(Authentication authentication) {
        if (authentication == null) {
            log.warn("Tentativa de obter ID do usuário de uma autenticação nula.");
            throw new IllegalArgumentException("Autenticação não pode ser nula.");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Jwt) {
            Jwt jwt = (Jwt) principal;
            Object userIdClaim = jwt.getClaim("userId");

            if (userIdClaim instanceof Number) {
                return ((Number) userIdClaim).longValue();
            } else if (userIdClaim instanceof String) {
                try {
                    return Long.parseLong((String) userIdClaim);
                } catch (NumberFormatException e) {
                    log.warn("Claim 'userId' é uma String mas não pôde ser convertido para Long: {}", userIdClaim, e);
                    throw new IllegalArgumentException("Claim 'userId' no token JWT é inválido (formato String incorreto).");
                }
            } else if (userIdClaim == null) {
                log.warn("Claim 'userId' não encontrado no token JWT. Subject: {}", jwt.getSubject());
                throw new IllegalStateException("Claim 'userId' não encontrado no token JWT. Verifique a configuração do Authorization Server (UserService).");
            } else {
                log.warn("Claim 'userId' possui tipo inesperado: {}. Valor: {}", userIdClaim.getClass().getName(), userIdClaim);
                throw new IllegalArgumentException("Claim 'userId' no token JWT possui tipo inesperado.");
            }
        } else if (authentication instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken jwtAuthToken = (JwtAuthenticationToken) authentication;
            Object userIdClaim = jwtAuthToken.getToken().getClaim("userId");
            if (userIdClaim instanceof Number) {
                return ((Number) userIdClaim).longValue();
            }
        }

        log.warn("Principal da autenticação não é do tipo Jwt ou JwtAuthenticationToken. Tipo recebido: {}", principal.getClass().getName());
        throw new IllegalArgumentException("Autenticação inválida ou não é baseada em JWT, ou não contém o claim 'userId'.");
    }


    @PostMapping("/novo-projeto")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<?> solicitarNovoProjeto(@Valid @RequestBody ProjetoDTO projetoDTO, Authentication authentication) {
        try {
            Long professorId = getUsuarioIdFromAuthentication(authentication);
            projetoDTO.setProfessorCriadorId(professorId);
            ProjetoDTO projetoCriado = gestaoProjetoService.solicitarNovoProjeto(projetoDTO, professorId);
            return ResponseEntity.status(HttpStatus.CREATED).body(projetoCriado);
        } catch (IllegalArgumentException e) {
            log.warn("Falha ao solicitar projeto: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Argumento inválido", "message", e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("Erro de estado ao processar a solicitação do projeto (ex: claim 'userId' ausente): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Erro interno", "message", e.getMessage()));
        }
    }

    @GetMapping ("/listar-projetos")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ProjetoDTO>> listarProjetos(
            @RequestParam(required = false) StatusProjetoModel status,
            @RequestParam(required = false) Long professorId) {
        if (professorId != null) {
            if (status != null) {
                return ResponseEntity.ok(gestaoProjetoService.buscarPorProfessorEStatus(professorId, status));
            } else {
                log.warn("Listando projetos: professorId fornecido sem status. Implementar lógica específica ou retornar todos os projetos.");
                return ResponseEntity.ok(gestaoProjetoService.listarTodos());
            }
        } else if (status != null) {
            return ResponseEntity.ok(gestaoProjetoService.buscarPorStatus(status));
        } else {
            return ResponseEntity.ok(gestaoProjetoService.listarTodos());
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProjetoDTO> buscarProjetoPorId(@PathVariable Long id) {
        return gestaoProjetoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/nome/{nome}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProjetoDTO> buscarProjetoPorNome(@PathVariable String nome) {
        return gestaoProjetoService.buscarPorNome(nome)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/aprovar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> aprovarProjeto(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(gestaoProjetoService.aprovarProjeto(id));
        } catch (ProjetoNaoEncontradoException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            log.warn("Falha ao aprovar projeto {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/recusar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> recusarProjeto(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(gestaoProjetoService.recusarProjeto(id));
        } catch (ProjetoNaoEncontradoException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            log.warn("Falha ao recusar projeto {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/finalizar")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<?> finalizarProjeto(@PathVariable Long id, Authentication authentication) {
        try {
            Long requisitanteId = getUsuarioIdFromAuthentication(authentication);
            List<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());


            String rolePrincipalRequisitante = roles.contains("ROLE_ADMIN") ? "ROLE_ADMIN" : (roles.contains("ROLE_PROFESSOR") ? "ROLE_PROFESSOR" : "ROLE_USER");

            ProjetoDTO projetoFinalizado = gestaoProjetoService.finalizarProjeto(id, requisitanteId, rolePrincipalRequisitante);
            return ResponseEntity.ok(projetoFinalizado);
        } catch (ProjetoNaoEncontradoException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException | SecurityException e) {
            log.warn("Falha ao finalizar projeto {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.error("Erro de argumento ao finalizar projeto (provavelmente ID do token): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Erro ao processar identidade do usuário: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<?> atualizarProjeto(@PathVariable Long id, @Valid @RequestBody ProjetoDTO projetoDTO, Authentication authentication) {
        try {
            Long requisitanteId = getUsuarioIdFromAuthentication(authentication);
            List<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            String rolePrincipalRequisitante = roles.contains("ROLE_ADMIN") ? "ROLE_ADMIN" : (roles.contains("ROLE_PROFESSOR") ? "ROLE_PROFESSOR" : "ROLE_USER");

            ProjetoDTO projetoAtualizado = gestaoProjetoService.atualizarDados(id, projetoDTO, requisitanteId, rolePrincipalRequisitante);
            return ResponseEntity.ok(projetoAtualizado);
        } catch (ProjetoNaoEncontradoException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException | SecurityException e) {
            log.warn("Falha ao atualizar projeto {}: {}", id, e.getMessage());
            if (e.getMessage().contains("token JWT") || e.getMessage().contains("Autenticação")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Erro ao processar identidade do usuário: " + e.getMessage()));
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletarProjeto(@PathVariable Long id) {
        try {
            gestaoProjetoService.deletarProjeto(id);
            return ResponseEntity.noContent().build();
        } catch (ProjetoNaoEncontradoException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            log.warn("Falha ao deletar projeto {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{projetoId}/associar-grupo/{grupoId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> associarGrupo(@PathVariable Long projetoId, @PathVariable Long grupoId) {
        try {
            ProjetoDTO projetoAtualizado = gestaoProjetoService.associarGrupo(projetoId, grupoId);
            return ResponseEntity.ok(projetoAtualizado);
        } catch (ProjetoNaoEncontradoException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            log.warn("Falha ao associar grupo {} ao projeto {}: {}", grupoId, projetoId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{projetoId}/desassociar-grupo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> desassociarGrupo(@PathVariable Long projetoId) {
        try {
            ProjetoDTO projetoAtualizado = gestaoProjetoService.desassociarGrupo(projetoId);
            return ResponseEntity.ok(projetoAtualizado);
        } catch (ProjetoNaoEncontradoException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            log.warn("Falha ao desassociar grupo do projeto {}: {}", projetoId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
