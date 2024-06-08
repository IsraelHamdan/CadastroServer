package teste;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    private static final int PORT = 28385;
    private static Map<String, String> credentials = new HashMap<>();
    private static Map<String, String> products = new HashMap<>();
    private static final Logger logger = Logger.getLogger(Server.class.getName());

    public static void main(String[] args) {
        // Inicializar as credenciais
        credentials.put("Israel", "Israel");
        credentials.put("user2", "password2");

        // Inicializar os produtos
        products.put("01", "Vinho tinto seco");
        products.put("02", "Macarrão com molho");
        products.put("03", "Filet mignon grelhado");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor iniciado e aguardando conexões...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado: " + clientSocket.getInetAddress());

                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Erro ao se comunicar com o cliente: " + e);
        }
    }

    private static class ClientHandler extends Thread {
        private Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
            ) {
                // Receber login e senha
                String login = in.readLine();
                String password = in.readLine();

                // Validar credenciais
                if (credentials.containsKey(login) && credentials.get(login).equals(password)) {
                    out.println("Credenciais válidas para o usuário: " + login);

                    // Entrar no ciclo de resposta
                    String command;
                    while ((command = in.readLine()) != null) {
                        if (command.equalsIgnoreCase("L")) {
                            // Enviar lista de produtos
                            out.println(products.values());
                        } else {
                            out.println("Comando desconhecido");
                        }
                    }
                } else {
                    out.println("Credenciais inválidas");
                    logger.info("Credenciais inválidas para o usuário: " + login);
                    clientSocket.close();
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Erro ao se conectar com o servidor");
            }
        }
    }
}
