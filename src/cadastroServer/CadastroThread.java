
package cadastroServer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;

import java.net.Socket;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.zip.InflaterOutputStream;

import controller.ProdutosJpaController;
import controller.UsuariosJpaController;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import model.Produtos;
import model.Usuarios;

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
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(s1.getInputStream()));
                PrintWriter out = new PrintWriter(s1.getOutputStream(), true);
        ) {
            String login = (String) in.readLine();
            String senha = (String) in.readLine();
            Usuarios usuario = ctrlUsu.findUsuarioByLogin(login);
            if(usuario == null || !usuario.getSenha().equals(senha)) {
                out.println("Login inválido");
                logger.log(Level.SEVERE, "Não foi posivel fazer login");
                return;
            }
            out.println("Login bem-sucedido");
            String inputLine; 
            while((inputLine = in.readLine()) != null) {
                switch (inputLine) {
                    case "LISTAR_PRODUTOS":
                        List<Produtos> produtos = ctrl.findProdutoEntities();
                        produtos.forEach(produto -> out.println(produto.getNome()));
                        break;
                    case "LISTAR_USUARIOS":
                        List<Usuarios> usuarios = ctrlUsu.findUsuarioEntities();
                        usuarios.forEach(u -> out.println(usuario.getLogin()));
                        break;
                    default: out.println("Comando invalido"); break;
                }
                
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Erro ao se comunicar com o cliente: ", e);
        } finally {
            try {
                s1.close();
            } catch (Exception e){
                logger.log(Level.SEVERE, "Erro fechar o socket", e);
            }
        }
    }
    
}
