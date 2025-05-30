package com.microservice.clientes.Clientes_microservice_curse.model;


import com.microservice.clientes.Clientes_microservice_curse.enums.StatusProjetoModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Date;

@Entity
@Table(name = "projetos")
public class ProjetoModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome do projeto não pode ser vazio")
    @Column(nullable = false, unique = true)
    private String nome;

    private String objetivo;

    @Column(length = 1000)
    private String escopoResumo;

    private String publicoAlvo;


    private Date dataInicio;

    @NotNull(message = "Status do projeto não pode ser nulo")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusProjetoModel status = StatusProjetoModel.EM_ANALISE;

    @Column(name = "professor_criador_id")
    private Long professorCriadorId;

    @Column(name = "grupo_id", unique = true, nullable = true)
    private Long grupoId;

    public ProjetoModel() {
    }

    public ProjetoModel(String nome, String objetivo, String escopoResumo, String publicoAlvo, Date dataInicio, Long professorCriadorId) {
        this.nome = nome;
        this.objetivo = objetivo;
        this.escopoResumo = escopoResumo;
        this.publicoAlvo = publicoAlvo;
        this.dataInicio = dataInicio;
        this.professorCriadorId = professorCriadorId;
        this.status = StatusProjetoModel.EM_ANALISE;
    }

    // Getters e Setters
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
    public Long getGrupoId() { return grupoId; }
    public void setGrupoId(Long grupoId) { this.grupoId = grupoId; }

    @Override
    public String toString() {
        return "ProjetoModel{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", status=" + status +
                ", professorCriadorId=" + professorCriadorId +
                ", grupoId=" + grupoId +
                '}';
    }
}
