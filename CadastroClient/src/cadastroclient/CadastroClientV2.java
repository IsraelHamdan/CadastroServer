package cadastroclient;

import java.io.*;
import java.net.Socket;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CadastroClientV2 {

    private static final Logger LOGGER = Logger.getLogger(CadastroClientV2.class.getName());

    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 4321);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
             BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {

            LOGGER.log(Level.INFO, "Cliente conectado ao servidor");

            System.out.println("===================Login====================");
            System.out.println("Insira o login:");
            String login = console.readLine();
            System.out.println("Insira a senha:");
            String password = console.readLine();

            out.writeObject(login);
            out.writeObject(password);

            String res = (String) in.readObject();
            System.out.println(res);

            if ("Login bem-sucedido".equals(res)) {
                boolean running = true;
                while (running) {
                    System.out.println("""
                            ========Opções========
                            L - Listar 
                            E - Entrada 
                            S - Saida 
                            F - Finalizar 
                            """);
                    String command = console.readLine();
                    out.writeObject(command);
                    switch (command) {
                        case "L":
                            String produtos;
                            while ((produtos = (String) in.readObject()) != null && !produtos.equals("END")) {
                                System.out.println(produtos);
                            }
                            break;
                        case "E": handleMoviment(console, out, in, command); 
                        case "S": handleMoviment(console, out, in, command);
                        case "F":
                            out.writeObject(command);
                            running = false;
                            break;
                        default:
                            System.out.println("Opção invalida");
                            break;
                    }
                }
            } else {
                System.out.println("Falha no login: " + res);
            }
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Não foi possível se conectar ao servidor", e);
        }
    }

    private static void handleMoviment(BufferedReader console, ObjectOutputStream out, ObjectInputStream in, String command) throws IOException {
        try {
            System.out.println("Insira o ID do produto");
            out.writeObject(console.readLine());
            
            System.out.println("Insira o ID da pessoa");
            out.writeObject(console.readLine());

            System.out.println("Insira o Id do Usuario");
            out.writeObject(console.readLine());
            
//            out.writeObject(command);
            
            System.out.println("Insira a quantidade");
            out.writeObject(console.readLine());

            System.out.println("Insira o valor_unitário ");
            out.writeObject(console.readLine());

            String response = (String) in.readObject();
            System.out.println(response);
        } catch (ClassNotFoundException e) {
            System.out.println("Erro ao ler a resposta do servidor");
        }
    }
}
