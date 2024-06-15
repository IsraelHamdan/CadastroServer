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
    
    private void display(String msg) {
        SwingUtilities.invokeLater(() -> frame.getTextArea().append(msg + "\n"));
    }

    private void connect() {
        try (Socket socket = new Socket("localhost", 4321);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {

            LOGGER.log(Level.INFO, "Cliente conectado ao servidor");

            System.out.println("Insira o login:");
            String login = console.readLine();
            
            System.out.println("Insira a senha:");
            String password = console.readLine();

            out.println(login);
            out.println(password);

            String res = in.readLine();
            System.out.println(res);

            if ("Login bem-sucedido".equals(res)) {
                display("Escolha uma opção");
                boolean running = true;
                tc = new ThreadClient(in, frame.getTextArea());
                tc.start();

                while (running) {
                    System.out.println("Escolha uma opção:");
                    System.out.println("L -> Listar | E -> Entrada | S -> Saída | F -> Finalizar");
                    String command = console.readLine();
                    out.println(command);

                    switch (command.toUpperCase()) {
                        case "L":
                            handleListProducts(in);
                            break;
                        case "E": 
                        case "S": 
                            handleMoviment(out, in, console);
                            break;
                        case "F":
                            running = false;
                            out.println("F");
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
            LOGGER.log(Level.SEVERE, "Não foi possível listar os produtos", e);
        }
    }

    private void handleMoviment(PrintWriter out, BufferedReader in, BufferedReader console) throws IOException {
        System.out.println("Insira o id do produto:");
        String idProduto = console.readLine();
        out.println(idProduto);

        System.out.println("Insira o id da pessoa:");
        String idPessoa = console.readLine();
        out.println(idPessoa);

        System.out.println("Insira o id do usuário:");
        String idUsuario = console.readLine();
        out.println(idUsuario);

        System.out.println("Insira a quantidade:");
        String quantidade = console.readLine();
        out.println(quantidade);

        System.out.println("Insira o preço:");
        String preco = console.readLine();
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
            client.connect();
        });
    }
}
