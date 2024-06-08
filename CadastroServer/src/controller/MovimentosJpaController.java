package controller;

import java.io.Serializable;
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

import model.Movimentos;

public class MovimentosJpaController implements Serializable {
    private static final Logger logger = Logger.getLogger(MovimentosJpaController.class.getName());
    private EntityManagerFactory emf; 

    public MovimentosJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
    
    public void create (Movimentos movimento) throws PreexistingEntityException {
    EntityManager em = null;
    try {
        em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        em.persist(movimento);
        tx.commit();
    } catch (PersistenceException pe){
        if(em != null && em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
        if (findMovimento(movimento.getIdMovimento()) != null) {
            throw new PreexistingEntityException("movimento" + movimento +  "já existe." + pe);
        }
        logger.log(Level.SEVERE, "Erro ao criar o produto", pe);
        throw new PersistenceException("Erro ao criar produto", pe);
    } finally {
        if(em != null) {
            em.close();
        }
    }
    }
    
    public Movimentos findMovimento(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Movimentos.class, id);
        } catch (IllegalArgumentException e) {
            logger.log(Level.SEVERE, "Argumento invalido ao buscar o movimento:", e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erro ao bucar o movimento", e);
        } finally {
            if (em != null) {
                em.close();
            }
        }
        return null;
    }
    
    public List<Movimentos> findProdutoEntities() {
        return findProdutoEntities(true, -1, -1);
    }
    public List<Movimentos> findProdutoEntities (boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery<Movimentos> cq = em.getCriteriaBuilder().createQuery(Movimentos.class);
            cq.select(cq.from(Movimentos.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erro ao buscar a lista de movimentos", e);
            return null;
        } finally {
            em.close();
        }
    }
    
    public void edit(Movimentos movimento) throws NonexistentEntityException {
        EntityManager em = null; 
        try {
            em = getEntityManager();
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            movimento = em.merge(movimento);
            tx.commit();
        } catch (IllegalArgumentException ie) {
            if(em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new NonexistentEntityException("O movimento com id" + movimento.getIdMovimento()+ "não existe");
        } catch (PersistenceException pe) {
            if(em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.log(Level.SEVERE, "Erro ao editar o movimento", pe);
            throw new PersistenceException("Erro ao editar movimento", pe);
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }
    
    public void destroy (Integer idMovimento) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            Movimentos movimento; 
            try {
                movimento = em.getReference(Movimentos.class, idMovimento);
                movimento.getIdProduto();
            } catch (IllegalArgumentException e) {
                tx.rollback();
                throw new NonexistentEntityException("O movimento com id" + idMovimento + "não existe", e);
            }
            em.remove(movimento);
            tx.commit();
        } catch (PersistenceException pe) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.log(Level.SEVERE, "Erro ao remover movimento", pe);
            throw new PersistenceException("Erro ao rmover o prouto", pe);
        } finally {
            if(em != null) {
                em.close();
            }
        }
    }
}
