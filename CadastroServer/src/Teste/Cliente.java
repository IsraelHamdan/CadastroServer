package teste;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Cliente {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 28385;
    private static final Logger logger = Logger.getLogger(Cliente.class.getName());

    public static void main(String[] args) {
        try (
                Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        ) {
            System.out.println("Conectado com o servidor");

            // Efetuando login
            System.out.println("Login: ");
            String login = stdIn.readLine();
            System.out.println("Senha: ");
            String password = stdIn.readLine();
            out.println(login);  // Use println para enviar a quebra de linha
            out.println(password);  // Use println para enviar a quebra de linha

            // Ler a resposta do servidor
            String res = in.readLine();
            if (res != null && res.contains("Credenciais válidas")) {  // Alterei para contains para fazer a correspondência parcial
                System.out.println("Digite 'L' para ver a lista de produtos");

                String userInput;
                while ((userInput = stdIn.readLine()) != null) {
                    out.println(userInput);
                    System.out.println("Resposta do servidor: " + in.readLine());
                }
            } else {
                System.out.println("Credenciais inválidas");
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Não foi possível se conectar com o servidor: " + e);
        }
    }
}
