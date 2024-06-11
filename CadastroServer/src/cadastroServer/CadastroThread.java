package cadastroServer;
import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import model.Produtos;
import model.Usuarios;
import controller.ProdutosJpaController;
import controller.UsuariosJpaController;


public class CadastroThread extends Thread {
    private final ProdutosJpaController ctrl;
    private final UsuariosJpaController ctrlUsu;
    private final Socket s1;
    private static final Logger logger = Logger.getLogger(CadastroThread.class.getName());

    public CadastroThread(ProdutosJpaController ctrl, UsuariosJpaController ctrlUsu, Socket s1) {
        this.ctrl = ctrl;
        this.ctrlUsu = ctrlUsu;
        this.s1 = s1;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(s1.getInputStream()));
             PrintWriter out = new PrintWriter(s1.getOutputStream(), true)) {

            logger.log(Level.INFO, "Thread iniciada para comunicação com o cliente");

            String login = in.readLine();
            String senha = in.readLine();
            

            Usuarios usuario = ctrlUsu.findUsuarioByLogin(login);
            if (usuario == null || !usuario.getSenha().equals(senha)) {
                out.println("Login inválido");
                logger.log(Level.SEVERE, "Não foi possível fazer login");
                return;
            }
            out.println("Login bem-sucedido");
            

            while (true) {
                String command = in.readLine();
                if (command == null) {
                    break;
                }
                logger.log(Level.INFO, "Comando recebido: " + command);
                if ("L".equalsIgnoreCase(command)) {
                    List<Produtos> produtos = ctrl.findProdutoEntities();
                    produtos.forEach(produto -> out.println(produto.getNome()));
                    out.println("END");
                } else logger.log(Level.INFO, "Comando invalido");
                
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Erro ao se comunicar com o cliente: ", e);
        } finally {
            try {
                if (s1 != null && !s1.isClosed()) {
                    s1.close();
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Erro ao fechar o socket", e);
            }
        }
    }
}
