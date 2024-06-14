package cadastroclient;
import javax.swing.*;


public class SaidaFrame extends JDialog {
    private JTextArea texto;

    public SaidaFrame() {
        setBounds(100, 100, 450, 300);
        setModal(false);
        texto = new JTextArea();
        add(new JScrollPane(texto));
        setVisible(true);
    }

    public JTextArea getTextArea() {
        return texto;
    }
}
