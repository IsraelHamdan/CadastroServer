package cadastroServer;

import model.Produtos;
import model.Usuarios;
import model.Movimentos;
import controller.MovimentosJpaController;
import controller.PessoasJpaController;
import controller.ProdutosJpaController;
import controller.UsuariosJpaController;
import controller.exceptions.NonexistentEntityException;
import controller.exceptions.PreexistingEntityException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CadastroThreadV2 extends Thread {
    private final PessoasJpaController ctrlPessoa;
    private final ProdutosJpaController ctrl;
    private final UsuariosJpaController ctrlUsu;
    private final MovimentosJpaController ctrlMov;
    private final Movimentos movimentos;

    private Produtos produto;
    
    private final Socket s1;
    private static final Logger logger = Logger.getLogger(CadastroThread.class.getName());

    public CadastroThreadV2(ProdutosJpaController ctrl, UsuariosJpaController ctrlUsu,
            MovimentosJpaController ctrlMov, PessoasJpaController ctrlPessoa, Socket s1) {
        this.ctrl = ctrl;
        this.ctrlUsu = ctrlUsu;
        this.ctrlMov = ctrlMov;
        this.ctrlPessoa = ctrlPessoa;
        movimentos = new Movimentos();
        this.s1 = s1;
    }
    
    private boolean isStocked(Produtos produto) {
        if (produto == null) {
            logger.log(Level.INFO, "Produto não encontrado");
            return false;
        }
        return true;
    }
    
    private void sellingProduct(ObjectInputStream in, ObjectOutputStream out, String command) throws IOException, ClassNotFoundException {
        produto = ctrl.findProduto(in.readInt());
        if (!isStocked(produto)) return;
        
        int quantidade = in.readInt();

        movimentos.setIdPessoa(ctrlPessoa.findPessoa(in.readInt()));
        movimentos.setIdProduto(produto);
        movimentos.setIdUsuario(ctrlUsu.findUsuario(in.readInt()));
        movimentos.setTipo(command);
        movimentos.setValorUnitario(in.readFloat());
        movimentos.setQuantidade(quantidade);

        produto.setQuantidade(produto.getQuantidade() + quantidade);
        try {
            ctrl.edit(produto);
            ctrlMov.create(movimentos);
        } catch (NonexistentEntityException | PreexistingEntityException ex) {
            logger.log(Level.SEVERE, "Erro ao vender o produto", ex);
        }
    }
    
    private void buyingProduct(ObjectInputStream in, ObjectOutputStream out, String command)  throws IOException, NonexistentEntityException {
        produto = ctrl.findProduto(in.readInt());
        if (!isStocked(produto)) return;
        
        int quantidade = in.readInt();
        
        movimentos.setIdPessoa(ctrlPessoa.findPessoa(in.readInt()));
        movimentos.setIdProduto(produto);
        movimentos.setIdUsuario(ctrlUsu.findUsuario(in.readInt()));
        movimentos.setTipo(command);
        movimentos.setValorUnitario(in.readFloat());
        movimentos.setQuantidade(quantidade);

        produto.setQuantidade(produto.getQuantidade() - quantidade);
        try {
            ctrl.edit(produto);
            ctrlMov.create(movimentos);
        } catch (PreexistingEntityException ex) {
            logger.log(Level.SEVERE, "Não foi possivel adicionar o produto ao estoque", ex);
        }
    }
    
    private void ListingProducts(ObjectOutputStream out) throws IOException {
        List<Produtos> produtos = ctrl.findProdutoEntities();
        produtos.forEach(produto -> {
            try {
                out.writeObject(produto.getNome());
            } catch (IOException ex) {
                Logger.getLogger(CadastroThreadV2.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        out.writeObject("END");

       
    }

    @Override
    public void run() {
        try (ObjectInputStream in = new ObjectInputStream(s1.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(s1.getOutputStream())) {

            logger.log(Level.INFO, "Thread iniciada para comunicação com o cliente");

            String login = (String) in.readObject();
            String senha = (String) in.readObject();
            

            Usuarios usuario = ctrlUsu.findUsuarioByLogin(login);
            if (usuario == null || !usuario.getSenha().equals(senha)) {
                out.writeObject("Login inválido");
                logger.log(Level.SEVERE, "Não foi possível fazer login");
                return;
            }
            out.writeObject("Login bem-sucedido");
            

            while (true) {
                String command = in.readLine();
                if (command == null) break;
                logger.log(Level.INFO, "Comando recebido: " + command);
                
                if("L".equalsIgnoreCase(command)) ListingProducts(out);
                if("E".equalsIgnoreCase(command)) buyingProduct(in, out, command);
                if("S".equalsIgnoreCase(command)) sellingProduct(in, out, command);
                if("F".equalsIgnoreCase(command)) break;
            }
        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Erro ao se comunicar com o cliente: ", e);
        } catch (NonexistentEntityException ex) {
            Logger.getLogger(CadastroThreadV2.class.getName()).log(Level.SEVERE, null, ex);
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
