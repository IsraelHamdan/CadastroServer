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
    private final BufferedReader in; 
    private BufferedReader res;
    private JTextArea textArea;
    private SaidaFrame frame;
   
    private static final java.util.logging.Logger LOGGER = Logger.getLogger(CadastroClientV2.class.getName());

    public ThreadClient(BufferedReader in, JTextArea textArea) {
        this.in = in;
        this.textArea = textArea;
    }

    @Override
    public void run() {
       try {
           while (true) {
               String recivedMessage = in.readLine();
               if (recivedMessage == null) {
                   break;
               }
               if (recivedMessage.startsWith("Produto: ")) {
                   SwingUtilities.invokeLater(() -> textArea.append(recivedMessage + "\n"));
               } else {
                   SwingUtilities.invokeLater(() -> textArea.append(recivedMessage + "\n"));
               }
           }
       } catch (IOException e) {
           LOGGER.log(Level.SEVERE, "Não foi possiivel realizar a operação", e);
       }
    }
}
