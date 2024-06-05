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

import model.Produtos;

public class ProdutosJpaController implements Serializable {
    private static final Logger logger = Logger.getLogger(ProdutosJpaController.class.getName());
    private EntityManagerFactory emf; 

    public ProdutosJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
    
    public void create (Produtos produto) throws PreexistingEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            em.persist(produto);
            tx.commit();
        } catch (PersistenceException pe){
            if(em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            if (findProduto(produto.getIdProduto()) != null) {
                throw new PreexistingEntityException("Produto" + produto +  "já existe." + pe);
            }
            logger.log(Level.SEVERE, "Erro ao criar o produto", pe);
            throw new PersistenceException("Erro ao criar produto", pe);
        } finally {
            if(em != null) {
                em.close();
            }
        }
        
    }
    
    public Produtos findProduto(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Produtos.class, id);
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
    

    public List<Produtos> findProdutoEntities() {
        return findProdutoEntities(true, -1, -1);
    }
    public List<Produtos> findProdutoEntities (boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery<Produtos> cq = em.getCriteriaBuilder().createQuery(Produtos.class);
            cq.select(cq.from(Produtos.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erro ao buscar a lista de produtos", e);
            return null;
        } finally {
            em.close();
        }
    }
    
    public void edit(Produtos produto) throws NonexistentEntityException {
        EntityManager em = null; 
        try {
            em = getEntityManager();
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            produto = em.merge(produto);
            tx.commit();
        } catch (IllegalArgumentException ie) {
            if(em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new NonexistentEntityException("O produto com id" + produto.getIdProduto() + "não existe");
        } catch (PersistenceException pe) {
            if(em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.log(Level.SEVERE, "Erro ao editar o produto", pe);
            throw new PersistenceException("Erro ao editar produto", pe);
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }
    
    public void destroy (Integer idProduto) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            Produtos produto; 
            try {
                produto = em.getReference(Produtos.class, idProduto);
                produto.getIdProduto();
            } catch (IllegalArgumentException e) {
                tx.rollback();
                throw new NonexistentEntityException("O produto com id" + idProduto + "não existe", e);
            }
            em.remove(produto);
            tx.commit();
        } catch (PersistenceException pe) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.log(Level.SEVERE, "Erro ao remover produto", pe);
            throw new PersistenceException("Erro ao rmover o prouto", pe);
        } finally {
            if(em != null) {
                em.close();
            }
        }
    }
}
