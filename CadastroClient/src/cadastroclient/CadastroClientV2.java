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
                            // Nada a fazer, ThreadClient irá lidar com a resposta
                            break;
                        case "E":
                        case "S":
                            handleMoviment(out, console, command.toUpperCase());
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

    private void handleMoviment(PrintWriter out, BufferedReader console, String command) throws IOException {
        String idProduto = JOptionPane.showInputDialog("Insira o id do produto");
        out.println(idProduto);

        String idPessoa = JOptionPane.showInputDialog("Insira o id da pessoa");
        out.println(idPessoa);

        String idUsuario = JOptionPane.showInputDialog("Insira o id do usuario");
        out.println(idUsuario);

        String quantidade = JOptionPane.showInputDialog("Insira a quantidade");
        out.println(quantidade);

        String preco = JOptionPane.showInputDialog("Insira o valor do produto");
        out.println(preco);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CadastroClientV2 client = new CadastroClientV2();
            client.connect();
        });
    }
}
