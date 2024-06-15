package cadastroServer;

import controller.PessoasJpaController;
import controller.MovimentosJpaController;
import controller.ProdutosJpaController;
import controller.UsuariosJpaController;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class CadastroServer {
    private static final Logger logger = Logger.getLogger(CadastroServer.class.getName());
    
    private static EntityManagerFactory emf;
    private static ProdutosJpaController ctrl;
    private static UsuariosJpaController ctrlUsu;
    private static PessoasJpaController ctrlPessoas;
    private static MovimentosJpaController ctrlMov;
    private static CadastroThread ct; 
    
    public static void main(String[] args) {
        emf = Persistence.createEntityManagerFactory("CadastroServerPU");
        ctrl = new ProdutosJpaController(emf);
        ctrlUsu = new UsuariosJpaController(emf);
        ctrlMov = new MovimentosJpaController(emf);
        ctrlPessoas = new PessoasJpaController(emf);

        try (ServerSocket serverSocket = new ServerSocket(4321)) {
            while (true) {
                try {
                    logger.log(Level.INFO, "Aguardando conexão do cliente...");
                    Socket clientSocket = serverSocket.accept();
                    if (clientSocket.isConnected()) {
                        ct = new CadastroThread(ctrl, ctrlUsu, clientSocket);
                        ct.start();
                    } else {
                        logger.log(Level.SEVERE, "A conexão com o cliente não foi estabelecida corretamente.");
                    }
                } catch (IOException ie) {
                    logger.log(Level.SEVERE, "Não foi possível se conectar com o cliente", ie);
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Não foi possível iniciar o servidor", e);
        }
    }
}