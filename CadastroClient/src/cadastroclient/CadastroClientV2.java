package cadastroclient;

import java.io.*;
import java.net.Socket;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CadastroClientV2 {
    
    private static final Logger LOGGER = Logger.getLogger(CadastroClientV2.class.getName());
    
    public static void main(String[] args) throws ClassNotFoundException {
       try (Socket socket = new Socket("localhost", 4321);
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())
           ) {
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
                   case "E":
                       handleMoviment(console, out, command);

                   case "S":
                       handleMoviment(console, out, command);
                       
                   case "F": 
                       out.writeObject(command);
                       break;
                    default: System.out.println("Opção invalida");
                    
               }
 
           } 
       } catch (IOException | ClassNotFoundException e) {
           LOGGER.log(Level.SEVERE, "Não foi possível se conectar ao servidor", e);
       }
   }
   
   private static void handleMoviment(BufferedReader console, ObjectOutputStream out, String command) throws IOException {
       
       System.out.println("Insira o ID da pessoa");
       out.writeObject(console.readLine());
       
       System.out.println("Insira o ID do produto");
       out.writeObject(console.readLine());
       
       System.out.println("Insira o Id do Usuario");
       out.writeObject(console.readLine());
       
       out.writeObject(command);
       
       System.out.println("Insira o valor_unitário ");
       out.writeObject(console.readLine());
       
       System.out.println("Insria a quantidade");
       out.writeObject(console.readLine());
   }
    
}
