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

    
    private final Socket s1;
    private static final Logger logger = Logger.getLogger(CadastroThreadV2.class.getName());

    public CadastroThreadV2(ProdutosJpaController ctrl, UsuariosJpaController ctrlUsu,
            MovimentosJpaController ctrlMov, PessoasJpaController ctrlPessoa, Socket s1) {
        this.ctrl = ctrl;
        this.ctrlUsu = ctrlUsu;
        this.ctrlMov = ctrlMov;
        this.ctrlPessoa = ctrlPessoa;
        
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
        Integer  idProduto = Integer.parseInt((String)in.readObject());
        Produtos produto = ctrl.findProduto(idProduto);
        Movimentos movimentos = new Movimentos();
        movimentos.setIdProduto(produto);
        
        Integer idPessoa = Integer.parseInt((String) in.readObject());
        movimentos.setIdPessoa(ctrlPessoa.findPessoa(idPessoa));
        System.out.println("nome da pessoa: " + movimentos.getIdPessoa().getNome());
        
        Integer idUser = Integer.parseInt((String) in.readObject());
        movimentos.setIdUsuario(ctrlUsu.findUsuario(idUser));
        System.out.println("nome do usuario: " + movimentos.getIdUsuario().getLogin());
        
        movimentos.setTipo(command);
        
        int quantidade = Integer.parseInt((String) in.readObject());
        movimentos.setQuantidade(quantidade);
        System.out.println("quantidade" + quantidade);
        
        Float preco = Float.parseFloat((String) in.readObject());
        movimentos.setValorUnitario(preco);
        
        produto.setQuantidade(produto.getQuantidade() - quantidade);
        try {
            ctrl.edit(produto);
            ctrlMov.create(movimentos);
            out.writeObject("Produto vendido com sucesso");
        } catch (NonexistentEntityException | PreexistingEntityException ex) {
            logger.log(Level.SEVERE, "Erro ao vender o produto", ex);
            out.writeObject("Erro ao vender o produto");
        }
    }
    
    private void buyingProduct(ObjectInputStream in, ObjectOutputStream out, String command)  throws IOException, ClassNotFoundException, NonexistentEntityException {
        Integer  idProduto = Integer.parseInt((String)in.readObject());
        Produtos produto = ctrl.findProduto(idProduto);
        Movimentos movimentos = new Movimentos();
        movimentos.setIdProduto(produto);
        
        Integer idPessoa = Integer.parseInt((String) in.readObject());
        movimentos.setIdPessoa(ctrlPessoa.findPessoa(idPessoa));
        System.out.println("nome da pessoa: " + movimentos.getIdPessoa().getNome());
        
        Integer idUser = Integer.parseInt((String) in.readObject());
        movimentos.setIdUsuario(ctrlUsu.findUsuario(idUser));
        System.out.println("nome do usuario: " + movimentos.getIdUsuario().getLogin());
        
        movimentos.setTipo(command);
        
        int quantidade = Integer.parseInt((String) in.readObject());
        movimentos.setQuantidade(quantidade);
        System.out.println("quantidade" + quantidade);
        
        Float preco = Float.parseFloat((String) in.readObject());
        movimentos.setValorUnitario(preco);
        
        produto.setQuantidade(produto.getQuantidade() + quantidade);
        try {
            ctrl.edit(produto);
            ctrlMov.create(movimentos);
            out.writeObject("Produto vendido com sucesso");
        } catch (NonexistentEntityException | PreexistingEntityException ex) {
            logger.log(Level.SEVERE, "Erro ao vender o produto", ex);
            out.writeObject("Erro ao vender o produto");
        }
    }
    
    private void ListingProducts(ObjectOutputStream out) throws IOException {
        List<Produtos> produtos = ctrl.findProdutoEntities();
        for (Produtos produto : produtos) {
            out.writeObject(produto.getNome() + " - Quantidade: " + produto.getQuantidade());
        }
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

            boolean running = true;
            while (running) {
                String command = (String) in.readObject();
                if (command == null) break;
                logger.log(Level.INFO, "Comando recebido: " + command);
                
                switch (command.toUpperCase()) {
                    case "L":
                        ListingProducts(out);
                        break;
                    case "E":
                        buyingProduct(in, out, command);
                        break;
                    case "S":
                        sellingProduct(in, out, command);
                        break;
                    case "F":
                        running = false;
                        break;
                    default:
                        logger.log(Level.WARNING, "Comando desconhecido: " + command);
                        out.writeObject("Comando desconhecido: " + command);
                        break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Erro ao se comunicar com o cliente: ", e);
        } catch (NonexistentEntityException ex) {
            logger.log(Level.SEVERE, "Erro ao editar entidade", ex);
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