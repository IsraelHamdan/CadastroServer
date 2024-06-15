package cadastroclient;
import java.awt.BorderLayout;
import javax.swing.*;


public class SaidaFrame extends JDialog {
    private JTextArea texto;

    public SaidaFrame() {
        setTitle("Movimento");
        setBounds(100, 100, 450, 300);
     
        setModal(false);
        
        texto = new JTextArea();
        texto.setEditable(false);
         add(new JScrollPane(texto), BorderLayout.CENTER);
        setVisible(true);
    }

    public JTextArea getTextArea() {
        return texto;
    }
}
