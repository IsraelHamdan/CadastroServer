package controller;

import controller.exceptions.IllegalOrphanException;
import controller.exceptions.NonexistentEntityException;
import controller.exceptions.PreexistingEntityException;
import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaQuery;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.PersistenceException;

import model.PessoasJuridicas;

public class PessoasJuridicasJpaController implements Serializable {
        private static final Logger logger = Logger.getLogger(PessoasJuridicasJpaController.class.getName());
    private EntityManagerFactory emf; 

    public PessoasJuridicasJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
    
    public void create (PessoasJuridicas pessoaJuridica) throws PreexistingEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            em.persist(pessoaJuridica);
            tx.commit();
        } catch (PersistenceException pe){
            if(em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            if (findPessoaJuridica(pessoaJuridica.getIdPJ()) != null) {
                throw new PreexistingEntityException("Produto" + pessoaJuridica +  "já existe." + pe);
            }
            logger.log(Level.SEVERE, "Erro ao criar o produto", pe);
            throw new PersistenceException("Erro ao criar produto", pe);
        } finally {
            if(em != null) {
                em.close();
            }
        }
        
    }
    
    public PessoasJuridicas findPessoaJuridica(Integer idPJ) {
        EntityManager em = getEntityManager();
        try {
            return em.find(PessoasJuridicas.class, idPJ);
        } catch (IllegalArgumentException e) {
            logger.log(Level.SEVERE, "Argumento invalido ao buscar o pessoa jurídica:", e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erro ao bucar o pessoa jurídica", e);
        } finally {
            if (em != null) {
                em.close();
            }
        }
        return null;
    }
    
    public List<PessoasJuridicas> findPessoaJuridicasEntities() {
        return findPessoaJuridicasEntities(true, -1, -1);
    }
    
    public List<PessoasJuridicas> findPessoaJuridicasEntities (boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery<PessoasJuridicas> cq = em.getCriteriaBuilder().createQuery(PessoasJuridicas.class);
            cq.select(cq.from(PessoasJuridicas.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erro ao buscar a lista de pessoa jurídicas", e);
            return null;
        } finally {
            em.close();
        }
    }
    
    public void edit(PessoasJuridicas pessoaJuridica) throws NonexistentEntityException {
        EntityManager em = null; 
        try {
            em = getEntityManager();
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            pessoaJuridica = em.merge(pessoaJuridica);
            tx.commit();
        } catch (IllegalArgumentException ie) {
            if(em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new NonexistentEntityException("O pessoa jurídica com id" + pessoaJuridica.getIdPJ() + "não existe");
        } catch (PersistenceException pe) {
            if(em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.log(Level.SEVERE, "Erro ao editar o pessoa jurídica", pe);
            throw new PersistenceException("Erro ao editar pessoa jurídica", pe);
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }
    
    public void destroy (Integer idPJ) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            PessoasJuridicas pessoaJuridica; 
            try {
                pessoaJuridica = em.getReference(PessoasJuridicas.class, idPJ);
                pessoaJuridica.getIdPJ();
            } catch (IllegalArgumentException e) {
                tx.rollback();
                throw new NonexistentEntityException("O pessoaJuridica com id" + idPJ + "não existe", e);
            }
            em.remove(pessoaJuridica);
            tx.commit();
        } catch (PersistenceException pe) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.log(Level.SEVERE, "Erro ao remover pessoaJuridica", pe);
            throw new PersistenceException("Erro ao rmover o prouto", pe);
        } finally {
            if(em != null) {
                em.close();
            }
        }
    }
    
}
