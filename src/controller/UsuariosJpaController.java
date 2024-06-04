package controller;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.Query;
import model.Usuarios;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UsuariosJpaController {
    private static final Logger logger = Logger.getLogger(UsuariosJpaController.class.getName());
    private EntityManagerFactory emf;
    
    public UsuariosJpaController() {
        emf = Persistence.createEntityManagerFactory("NomeDaSuaUnidadeDePersistencia");
    }
    
    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
    
    public Usuarios findUsuario(String nome, String senha) {
        EntityManager em = getEntityManager();
        try {
            Query query = em.createQuery("SELECT u FROM Usuarios u WHERE u.nome = :nome  AND u.senha = :senha") ;
            query.setParameter("nome", nome);
            query.setParameter("senha", senha);
            return (Usuarios) query.getSingleResult();
        } catch (NoResultException e) {
            logger.log(Level.WARNING, "Nenhum usuário encotrado no banco", e);
            return null;
        } finally {
            em.close();
        }
    }
}
