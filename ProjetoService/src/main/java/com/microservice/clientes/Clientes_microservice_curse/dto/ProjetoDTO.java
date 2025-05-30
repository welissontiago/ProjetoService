package com.microservice.clientes.Clientes_microservice_curse.dto;

import com.microservice.clientes.Clientes_microservice_curse.enums.StatusProjetoModel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.sql.Date;

public class ProjetoDTO {

    private Long id;

    @NotBlank(message = "Nome do projeto é obrigatório")
    private String nome;

    private String objetivo;
    private String escopoResumo;
    private String publicoAlvo;
    private Date dataInicio;

    @NotNull(message = "Status é obrigatório")
    private StatusProjetoModel status;

    private Long professorCriadorId;
    private String nomeProfessorCriador;

    private Long grupoId;
    private String nomeGrupo;


    public ProjetoDTO() {}

    public ProjetoDTO(Long id, String nome, String objetivo, String escopoResumo, String publicoAlvo, Date dataInicio, StatusProjetoModel status, Long professorCriadorId, String nomeProfessorCriador, Long grupoId, String nomeGrupo) {
        this.id = id;
        this.nome = nome;
        this.objetivo = objetivo;
        this.escopoResumo = escopoResumo;
        this.publicoAlvo = publicoAlvo;
        this.dataInicio = dataInicio;
        this.status = status;
        this.professorCriadorId = professorCriadorId;
        this.nomeProfessorCriador = nomeProfessorCriador;
        this.grupoId = grupoId;
        this.nomeGrupo = nomeGrupo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getObjetivo() { return objetivo; }
    public void setObjetivo(String objetivo) { this.objetivo = objetivo; }
    public String getEscopoResumo() { return escopoResumo; }
    public void setEscopoResumo(String escopoResumo) { this.escopoResumo = escopoResumo; }
    public String getPublicoAlvo() { return publicoAlvo; }
    public void setPublicoAlvo(String publicoAlvo) { this.publicoAlvo = publicoAlvo; }
    public Date getDataInicio() { return dataInicio; }
    public void setDataInicio(Date dataInicio) { this.dataInicio = dataInicio; }
    public StatusProjetoModel getStatus() { return status; }
    public void setStatus(StatusProjetoModel status) { this.status = status; }
    public Long getProfessorCriadorId() { return professorCriadorId; }
    public void setProfessorCriadorId(Long professorCriadorId) { this.professorCriadorId = professorCriadorId; }
    public String getNomeProfessorCriador() { return nomeProfessorCriador; }
    public void setNomeProfessorCriador(String nomeProfessorCriador) { this.nomeProfessorCriador = nomeProfessorCriador; }
    public Long getGrupoId() { return grupoId; }
    public void setGrupoId(Long grupoId) { this.grupoId = grupoId; }
    public String getNomeGrupo() { return nomeGrupo; }
    public void setNomeGrupo(String nomeGrupo) { this.nomeGrupo = nomeGrupo; }
}
