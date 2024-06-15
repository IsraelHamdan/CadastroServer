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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.io.PrintWriter;

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
   
    private void sellingProduct(BufferedReader in, PrintWriter out, String command) throws IOException {
        
        Integer idProduto = Integer.parseInt(in.readLine());
        Produtos produto = ctrl.findProduto(idProduto);
        Movimentos movimentos = new Movimentos();
        movimentos.setIdProduto(produto);
        
        Integer idPessoa = Integer.parseInt(in.readLine());
        movimentos.setIdPessoa(ctrlPessoa.findPessoa(idPessoa));
        
        Integer idUser = Integer.parseInt(in.readLine());
        movimentos.setIdUsuario(ctrlUsu.findUsuario(idUser));
        
        movimentos.setTipo(command);
        
        int quantidade = Integer.parseInt(in.readLine());
        movimentos.setQuantidade(quantidade);
        
        
        Float preco = Float.parseFloat(in.readLine());
        movimentos.setValorUnitario(preco);
        
        produto.setQuantidade(produto.getQuantidade() - quantidade);
        try {
            ctrl.edit(produto);
            ctrlMov.create(movimentos);
            out.println("Produto vendido com sucesso");
            outputMovimment(out);
        } catch (NonexistentEntityException | PreexistingEntityException ex) {
            logger.log(Level.SEVERE, "Erro ao vender o produto", ex);
            out.println("Erro ao vender o produto");
        }
    }
    
    private void buyingProduct(BufferedReader in, PrintWriter out, String command) throws IOException, NonexistentEntityException {
        Integer idProduto = Integer.parseInt(in.readLine());
        Produtos produto = ctrl.findProduto(idProduto);
        Movimentos movimentos = new Movimentos();
        movimentos.setIdProduto(produto);
        
        Integer idPessoa = Integer.parseInt(in.readLine());
        movimentos.setIdPessoa(ctrlPessoa.findPessoa(idPessoa));

        Integer idUser = Integer.parseInt(in.readLine());
        movimentos.setIdUsuario(ctrlUsu.findUsuario(idUser));

        movimentos.setTipo(command);
        
        int quantidade = Integer.parseInt(in.readLine());
        movimentos.setQuantidade(quantidade);
       
        
        Float preco = Float.parseFloat(in.readLine());
        movimentos.setValorUnitario(preco);
        
        produto.setQuantidade(produto.getQuantidade() + quantidade);
        try {
            ctrl.edit(produto);
            ctrlMov.create(movimentos);
            out.println("Produto comprado com sucesso");
            outputMovimment(out);
        } catch (NonexistentEntityException | PreexistingEntityException ex) {
            logger.log(Level.SEVERE, "Erro ao comprar o produto", ex);
            out.println("Erro ao comprar o produto");
        }
    }
    
    private void ListingProducts(PrintWriter out) {
        List<Produtos> produtos = ctrl.findProdutoEntities();
        for (Produtos produto : produtos) {
            out.println("Produtos: " + produto.getNome() + " - Quantidade: " + produto.getQuantidade());
        }
        out.println("END");
    }
    
    private void outputMovimment(PrintWriter out) {
        List<Movimentos> movimentos = ctrlMov.findMovimentoEntities();
        int indice = movimentos.size() -1;
        String tipo = movimentos.get(indice).getTipo();
        String loginU = movimentos.get(indice).getIdUsuario().getLogin() ;
        String produto = movimentos.get(indice).getIdProduto().getNome();
        String pessoa = movimentos.get(indice).getIdPessoa().getNome();
        Float valor = movimentos.get(indice).getQuantidade() * movimentos.get(indice).getIdProduto().getPreco();
        
        String msg = String.format("Movimento: "+"Tipo: %s | Usuario: %s | Produto: %s | Pessoa: %s | valor %.2f"  , tipo, 
                loginU, produto,pessoa,valor
        ) ;
        out.println("begin");
        out.println(msg);
        out.flush();
        out.println("END");
        System.out.println(msg);
                
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

            boolean running = true;
            while (running) {
                String command = in.readLine();
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
                        out.println("Comando desconhecido: " + command);
                        break;
                }
            }
        } catch (IOException e) {
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
