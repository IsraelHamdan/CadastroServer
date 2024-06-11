package cadastroclient;
import java.io.*;
import java.net.Socket;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CadastroClient {
    private static final Logger logger = Logger.getLogger(CadastroClient.class.getName());

    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 4321);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
            ) {

            System.out.println("Insira o login:");
            String login = console.readLine();
            System.out.println("Insira a senha:");
            String password = console.readLine();

            out.println(login);
            out.println(password);

            String res = in.readLine();
            System.out.println(res);

            if ("Login bem-sucedido".equals(res)) {
                System.out.println("Digite 'L' para exibir os produtos");
                String command = console.readLine();
                out.println(command);
                String produtos;
                while ((produtos = in.readLine()) != null && !produtos.equals("END")) {
                    System.out.println(produtos);
                }
            } else {
                System.out.println("Falha no login. Tente novamente.");
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Não foi possível se conectar ao servidor", e);
        }
    }
}