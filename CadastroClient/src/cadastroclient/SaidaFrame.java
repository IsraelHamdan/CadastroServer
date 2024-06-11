// author: Israel

package cadastroclient;

import javax.swing.JDialog;
import javax.swing.JTextArea;


public class SaidaFrame extends JDialog {
    private JTextArea texto;

    public SaidaFrame() {
        setBounds(100,100, 400, 300);
        setModal(false);
        
        texto = new JTextArea();
        getContentPane().add(texto);
    }
    
    public void addText(String texto) {
        this.texto.append(texto + "\n");
    }
    

}
