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

import model.Pessoas;

public class PessoasJpaController implements Serializable {
    private static final Logger logger = Logger.getLogger(PessoasJpaController.class.getName());
    private EntityManagerFactory emf; 

    public PessoasJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
    
    public void create (Pessoas pessoa) throws PreexistingEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            em.persist(pessoa);
            tx.commit();
        } catch (PersistenceException pe){
            if(em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            if (findPessoa(pessoa.getIdPessoa()) != null) {
                throw new PreexistingEntityException("Produto" + pessoa +  "já existe." + pe);
            }
            logger.log(Level.SEVERE, "Erro ao criar o pessoa", pe);
            throw new PersistenceException("Erro ao criar pessoa", pe);
        } finally {
            if(em != null) {
                em.close();
            }
        }
    }
    
    public Pessoas findPessoa(Integer idPessoa) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Pessoas.class, idPessoa);
        } catch (IllegalArgumentException e) {
            logger.log(Level.SEVERE, "Argumento invalido ao buscar o produto:", e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erro ao bucar o produto", e);
        } finally {
            if (em != null) {
                em.close();
            }
        }
        return null;
    }
    
    public List<Pessoas> findPessoaEntities() {
        return findPessoaEntities(true, -1, -1);
    }
    
    public List<Pessoas> findPessoaEntities (boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery<Pessoas> cq = em.getCriteriaBuilder().createQuery(Pessoas.class);
            cq.select(cq.from(Pessoas.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erro ao buscar a lista de pessoas", e);
            return null;
        } finally {
            em.close();
        }
    }
    
    public void edit(Pessoas pessoa) throws NonexistentEntityException {
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
            throw new NonexistentEntityException("O pessoa com id" + pessoa.getIdPessoa()+ "não existe");
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
    
    public void destroy (Integer idPessoa) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            Pessoas pessoa; 
            try {
                pessoa = em.getReference(Pessoas.class, idPessoa);
                pessoa.getIdPessoa();
            } catch (IllegalArgumentException e) {
                tx.rollback();
                throw new NonexistentEntityException("O pessoa com id" + idPessoa + "não existe", e);
            }
            em.remove(pessoa);
            tx.commit();
        } catch (PersistenceException pe) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.log(Level.SEVERE, "Erro ao remover pessoa", pe);
            throw new PersistenceException("Erro ao rmover o prouto", pe);
        } finally {
            if(em != null) {
                em.close();
            }
        }
    }
}
