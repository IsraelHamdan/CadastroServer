package cadastroclient;
import java.util.List;

import model.Produtos;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;


public class ThreadClient extends Thread {
    private Produtos produtos;
    private final ObjectInputStream in; 
    private BufferedReader res;
    private JTextArea textArea;
    private SaidaFrame frame;
   
    private static final java.util.logging.Logger LOGGER = Logger.getLogger(CadastroClientV2.class.getName());

    public ThreadClient(ObjectInputStream in, JTextArea textArea) {
        this.in = in;
        this.textArea = textArea;
    }
    @Override
    public void run() {
       try{
           while (true) {
               Object recivedObject = in.readObject();
               if(recivedObject instanceof String msg) {
                   SwingUtilities.invokeLater(() -> textArea.append(msg + "\n"));
               } else if (recivedObject instanceof List<?>) {
                   List<Produtos> listaDeProdutos = (List<Produtos>) recivedObject;
                   if(!listaDeProdutos.isEmpty()) {
                       textArea.append("\nLista de produtos\n");
                       for (Produtos produto : listaDeProdutos) {
                          textArea.append(produto.getNome() + " - Quantidade: " + produto.getQuantidade() + "\n");
                       }
                   }
               }
           }
       } catch (IOException | ClassNotFoundException e) {
           LOGGER.log(Level.SEVERE, "Não foi possiivel realizar a operação", e);
       }
    }
    
}
