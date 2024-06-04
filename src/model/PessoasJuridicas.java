/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 *
 * @author Israe
 */
@Entity
@Table(name = "PessoasJuridicas")
@NamedQueries({
    @NamedQuery(name = "PessoasJuridicas.findAll", query = "SELECT p FROM PessoasJuridicas p"),
    @NamedQuery(name = "PessoasJuridicas.findByIdPJ", query = "SELECT p FROM PessoasJuridicas p WHERE p.idPJ = :idPJ"),
    @NamedQuery(name = "PessoasJuridicas.findByCnpj", query = "SELECT p FROM PessoasJuridicas p WHERE p.cnpj = :cnpj")})
public class PessoasJuridicas implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "idPJ")
    private Integer idPJ;
    @Basic(optional = false)
    @Column(name = "cnpj")
    private String cnpj;
    @JoinColumn(name = "idPJ", referencedColumnName = "idPessoa", insertable = false, updatable = false)
    @OneToOne(optional = false)
    private Pessoas pessoas;

    public PessoasJuridicas() {
    }

    public PessoasJuridicas(Integer idPJ) {
        this.idPJ = idPJ;
    }

    public PessoasJuridicas(Integer idPJ, String cnpj) {
        this.idPJ = idPJ;
        this.cnpj = cnpj;
    }

    public Integer getIdPJ() {
        return idPJ;
    }

    public void setIdPJ(Integer idPJ) {
        this.idPJ = idPJ;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public Pessoas getPessoas() {
        return pessoas;
    }

    public void setPessoas(Pessoas pessoas) {
        this.pessoas = pessoas;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idPJ != null ? idPJ.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PessoasJuridicas)) {
            return false;
        }
        PessoasJuridicas other = (PessoasJuridicas) object;
        if ((this.idPJ == null && other.idPJ != null) || (this.idPJ != null && !this.idPJ.equals(other.idPJ))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "cadastroserver.PessoasJuridicas[ idPJ=" + idPJ + " ]";
    }
    
}
