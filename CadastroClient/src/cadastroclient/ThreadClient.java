package cadastroclient;

import java.io.BufferedReader;
import java.io.IOException;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class ThreadClient extends Thread {

    private final BufferedReader in;
    private final JTextArea textArea;

    public ThreadClient(BufferedReader in, JTextArea textArea) {
        this.in = in;
        this.textArea = textArea;
    }

    @Override
    public void run() {
        try {
            String mensagem;
            while ((mensagem = in.readLine()) != null) {
                processarMensagem(mensagem);
            }
        } catch (IOException e) {
            System.out.println("Erro ao ler mensagem do servidor: " + e.getMessage());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    System.out.println("Erro ao fechar BufferedReader: " + e.getMessage());
                }
            }
        }
    }

    private void processarMensagem(String mensagem) {
        SwingUtilities.invokeLater(() -> {
            if (mensagem.startsWith("Produtos: ")) {
                exibirListaDeProdutos(mensagem.substring("Produtos: ".length()));
            } else if (mensagem.startsWith("Movimento: ")) {
                exibirMovimento(mensagem.substring("Movimento: ".length()));
            } else {
                exibirMensagem(mensagem);
            }
        });
    }

    private JTextArea exibirListaDeProdutos(String produtosInfo) {
        textArea.append("Lista de Produtos:\n");
        String[] produtos = produtosInfo.split(";");
        for (String produto : produtos) {
            textArea.append(produto + "\n");
        }
        textArea.append("--------------------\n");
        return textArea;
    }

    private JTextArea exibirMovimento(String movimentoInfo) {
        textArea.append("Detalhes do Movimento:\n");
        textArea.append(movimentoInfo + "\n");
        textArea.append("--------------------\n");
        return textArea;
    }

    private JTextArea exibirMensagem(String mensagem) {
        textArea.append(mensagem + "\n");
        return textArea;
    }
}
