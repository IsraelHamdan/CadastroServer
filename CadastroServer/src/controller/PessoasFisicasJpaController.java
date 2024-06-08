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

import model.PessoasFisicas;

public class PessoasFisicasJpaController implements Serializable {
    private static final Logger logger = Logger.getLogger(PessoasFisicasJpaController.class.getName());
    private EntityManagerFactory emf; 

    public PessoasFisicasJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
    
        public void create (PessoasFisicas pessoaFisica) throws PreexistingEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            em.persist(pessoaFisica);
            tx.commit();
        } catch (PersistenceException pe){
            if(em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            if (findPessoaFisica(pessoaFisica.getIdPessoaFisica()) != null) {
                throw new PreexistingEntityException("Produto" + pessoaFisica +  "já existe." + pe);
            }
            logger.log(Level.SEVERE, "Erro ao criar o pessoaFisica", pe);
            throw new PersistenceException("Erro ao criar pessoaFisica", pe);
        } finally {
            if(em != null) {
                em.close();
            }
        }
    }
        
    public PessoasFisicas findPessoaFisica(Integer idPessoaFisica) {
        EntityManager em = getEntityManager();
        try {
            return em.find(PessoasFisicas.class, idPessoaFisica);
        } catch (IllegalArgumentException e) {
            logger.log(Level.SEVERE, "Argumento invalido ao buscar a pessoa física:", e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erro ao bucar a pessoa física", e);
        } finally {
            if (em != null) {
                em.close();
            }
        }
        return null;
    }
    
    public List<PessoasFisicas> findPessoaFisicaEntities() {
        return findPessoaFisicaEntities(true, -1, -1);
    }
    
    public List<PessoasFisicas> findPessoaFisicaEntities (boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery<PessoasFisicas> cq = em.getCriteriaBuilder().createQuery(PessoasFisicas.class);
            cq.select(cq.from(PessoasFisicas.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erro ao buscar a lista de pessoaFisicas", e);
            return null;
        } finally {
            em.close();
        }
    }
    
    public void edit(PessoasFisicas pessoa) throws NonexistentEntityException {
        EntityManager em = null; 
        try {
            em = getEntityManager();
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            pessoa = em.merge(pessoa);
            tx.commit();
        } catch (IllegalArgumentException ie) {
            if(em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new NonexistentEntityException("O pessoa com id" + pessoa.getIdPessoaFisica()+ "não existe");
        } catch (PersistenceException pe) {
            if(em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.log(Level.SEVERE, "Erro ao editar o pessoa", pe);
            throw new PersistenceException("Erro ao editar pessoa", pe);
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }
    
    public void destroy (Integer idPessoasFisica) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            PessoasFisicas pessoa; 
            try {
                pessoa = em.getReference(PessoasFisicas.class, idPessoasFisica);
                pessoa.getIdPessoaFisica();
            } catch (IllegalArgumentException e) {
                tx.rollback();
                throw new NonexistentEntityException("A pessoa com id" + idPessoasFisica + "não existe", e);
            }
            em.remove(pessoa);
            tx.commit();
        } catch (PersistenceException pe) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.log(Level.SEVERE, "Erro ao remover a pessoa", pe);
            throw new PersistenceException("Erro ao rmover a pessoa", pe);
        } finally {
            if(em != null) {
                em.close();
            }
        }
    }
    
}
