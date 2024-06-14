package cadastroclient;

import java.io.*;
import java.net.Socket;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class CadastroClientV2 {
    private SaidaFrame frame;
    private ThreadClient tc;

    private static final Logger LOGGER = Logger.getLogger(CadastroClientV2.class.getName());

    public CadastroClientV2() {
        frame = new SaidaFrame();
        
        
    }
    
    private  void display(String msg) {
        SwingUtilities.invokeLater(() -> frame.getTextArea().append(msg + "\n"));
    }

    private void conect() {
        try (Socket socket = new Socket("localhost", 4321);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {

            LOGGER.log(Level.INFO, "Cliente conectado ao servidor");

            System.out.println("Insira o login");
            String login = console.readLine();
            
            System.out.println("Insria a senha ");
            String password = console.readLine();

            out.println(login);
            out.println(password);

            String res = in.readLine();
            System.out.println(res);

            if ("Login bem-sucedido".equals(res)) {
                display("Escolha uma opção");
                boolean running = true;
                while (running) {
                    tc = new ThreadClient(in, frame.getTextArea());
                    display("L -> Listar | E -> Entrada | S -> Saída | X - Finalizar");
                    String command = JOptionPane.showInputDialog("Escolha uma opção");
                    out.println(command);
                    tc.start();
                    switch (command.toUpperCase()) {
                        case "L":
                            handleListProducts(in);
                            break;
                        case "E": handleMoviment(out, in);
                            break;
                        case "S": handleMoviment(out, in);
                            break;
                        case "F":
                            running = false;
                            break;
                        default:
                            System.out.println("Opção inválida");
                            break;
                    }
                }
            } else {
                System.out.println("Falha no login: " + res);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Não foi possível se conectar ao servidor", e);
        }
    }

    private void handleListProducts(BufferedReader in) throws IOException {
        try {
            String produtos;
            while ((produtos = in.readLine()) != null && !produtos.equals("END")) {
               display(produtos);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Não foi possivel listar os produtos, pelo motivo de: ", e);
        }

    }

    private  void handleMoviment(PrintWriter out, BufferedReader in) throws IOException {
        
        String idProduto = JOptionPane.showInputDialog("Insira o ID do produto");
        out.println(idProduto);
        
        String idPessoa = JOptionPane.showInputDialog("Insira o id da pessoa");
        out.println(idPessoa);

        String idUsuario = JOptionPane.showInputDialog("Insira o Id do Usuario");
        out.println(idUsuario);      
        
        String quantidade = JOptionPane.showInputDialog("Insira a quantidade");
        out.println(quantidade);

        String preco = JOptionPane.showInputDialog("Insira o valor unitário");
        out.println(preco);

        String response = in.readLine();
        display(response);
         
        String movimentInfo;
        while ((movimentInfo = in.readLine()) != null && !movimentInfo.equals("END")) {
            display(movimentInfo);
        }
        
    }
   
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CadastroClientV2 client = new CadastroClientV2();
            client.conect();
        });
    }
    
}
