package controller;
import controller.exceptions.IllegalOrphanException;
import controller.exceptions.NonexistentEntityException;
import controller.exceptions.PreexistingEntityException;
import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import model.Usuarios;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;

public class UsuariosJpaController implements Serializable {
    private static final Logger logger = Logger.getLogger(UsuariosJpaController.class.getName());
    private EntityManagerFactory emf; 

    public UsuariosJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
    
    public void create (Usuarios usuario) throws PreexistingEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            em.persist(usuario);
            tx.commit();
        } catch (PersistenceException pe){
            if(em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            if (findUsuario(usuario.getIdUsuario()) != null) {
                throw new PreexistingEntityException("Produto" + usuario +  "já existe." + pe);
            }
            logger.log(Level.SEVERE, "Erro ao criar o usuario", pe);
            throw new PersistenceException("Erro ao criar usuario", pe);
        } finally {
            if(em != null) {
                em.close();
            }
        }
        
    }
    
    
    public List<Usuarios> findUsuariosEntities (boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery<Usuarios> cq = em.getCriteriaBuilder().createQuery(Usuarios.class);
            cq.select(cq.from(Usuarios.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erro ao buscar a lista de usuarios", e);
            return null;
        } finally {
            em.close();
        }
    }
    public Usuarios findUsuario(Integer idUsuario) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Usuarios.class, idUsuario);
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
    
    public List<Usuarios> findUsuarioEntities() {
        return findUsuarioEntities(true, -1, -1);
    }
    public List<Usuarios> findUsuarioEntities (boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery<Usuarios> cq = em.getCriteriaBuilder().createQuery(Usuarios.class);
            cq.select(cq.from(Usuarios.class));
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
    
    public Usuarios findUsuarioByLogin(String login) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Usuarios> query = em.createQuery("SELECT u FROM Usuarios u where u.login = :login", Usuarios.class);
            query.setParameter("login", login);
            return query.getSingleResult();
        } catch (NoResultException e) {
            logger.log(Level.SEVERE, "Usuário com o login: " + login +"não foi encontrado, ", e);
            return null;
        } finally {
            em.close();
        }
    }
    
    public void edit(Usuarios usuario) throws NonexistentEntityException {
        EntityManager em = null; 
        try {
            em = getEntityManager();
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            usuario = em.merge(usuario);
            tx.commit();
        } catch (IllegalArgumentException ie) {
            if(em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new NonexistentEntityException("O usuario com id" + usuario.getIdUsuario()    + "não existe");
        } catch (PersistenceException pe) {
            if(em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.log(Level.SEVERE, "Erro ao editar o usuario", pe);
            throw new PersistenceException("Erro ao editar usuario", pe);
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }
    
    public void destroy (Integer idUsuario) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            Usuarios usuario; 
            try {
                usuario = em.getReference(Usuarios.class, idUsuario);
                usuario.getIdUsuario();
            } catch (IllegalArgumentException e) {
                tx.rollback();
                throw new NonexistentEntityException("O usuario com id" + idUsuario + "não existe", e);
            }
            em.remove(usuario);
            tx.commit();
        } catch (PersistenceException pe) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.log(Level.SEVERE, "Erro ao remover usuario", pe);
            throw new PersistenceException("Erro ao rmover o prouto", pe);
        } finally {
            if(em != null) {
                em.close();
            }
        }
    }
}
