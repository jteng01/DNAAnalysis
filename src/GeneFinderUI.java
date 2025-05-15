import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;

public class GeneFinderUI {
    private final JTextArea sequenceArea;
    private final JTextArea resultArea;
    private String dnaSequence = "";


    public GeneFinderUI() {
        JFrame frame = new JFrame("DNA Gene Finder");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        JButton loadButton = new JButton("Load DNA File");
        JButton findButton = new JButton("Locate Genes");
        loadButton.addActionListener(this::handleLoad);
        findButton.addActionListener(this::handleFindGenes);

        sequenceArea = new JTextArea();
        sequenceArea.setEditable(true);
        JScrollPane sequenceScrollPane = new JScrollPane(sequenceArea);
        sequenceScrollPane.setBorder(BorderFactory.createTitledBorder("Loaded DNA Sequence"));

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        JScrollPane resultScrollPane = new JScrollPane(resultArea);
        resultScrollPane.setBorder(BorderFactory.createTitledBorder("Results"));

        JPanel topPanel = new JPanel();
        topPanel.add(loadButton);
        topPanel.add(findButton);

        JPanel textAreasPanel = new JPanel();
        textAreasPanel.setLayout(new BoxLayout(textAreasPanel, BoxLayout.Y_AXIS));
        textAreasPanel.add(sequenceScrollPane);
        textAreasPanel.add(resultScrollPane);

        frame.getContentPane().add(topPanel, BorderLayout.NORTH);
        frame.getContentPane().add(textAreasPanel, BorderLayout.CENTER);

        frame.setVisible(true);
    }

    private void handleLoad(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();

        FileNameExtensionFilter filter = new FileNameExtensionFilter("Text Files (*.txt)", "txt");
        chooser.setFileFilter(filter);

        int result = chooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();

            try {
                String content = FileHandler.readDNAFromFile(selected).toUpperCase();

                if (content.isEmpty()) {
                    resultArea.setText("Error: The selected file is empty.");
                    sequenceArea.setText("");
                    return;
                }

                if (!content.matches("[CATG]*")) {
                    resultArea.setText("Error: File contains invalid characters.\nOnly C, A, T, and G are allowed.");
                    sequenceArea.setText("");
                    return;
                }

                dnaSequence = content;
                resultArea.setText("Loaded DNA from: " + selected.getName());
                sequenceArea.setText(dnaSequence);

            } catch (Exception ex) {
                resultArea.setText("Error loading file: " + ex.getMessage());
                sequenceArea.setText("");
            }
        }
    }

    private void handleFindGenes(ActionEvent e) {
        String userInput = sequenceArea.getText().toUpperCase().trim();

        if (userInput.isEmpty()) {
            resultArea.setText("DNA sequence is empty. Load a file or enter a sequence.");
            return;
        }

        if (!userInput.matches("[CATG]+")) {
            resultArea.setText("Error: Sequence contains invalid characters.\nOnly C, A, T, and G are allowed.");
            return;
        }

        DNAReader reader = new DNAReader(userInput);
        ArrayList<String> genes = reader.locateGenes();

        StringBuilder output = new StringBuilder();
        if (genes.isEmpty()) {
            output.append("No genes found.");
        } else {
            output.append("Potential Genes found:\n");
            for (String gene : genes) {
                output.append(gene).append("\n");
            }
        }

        resultArea.setText(output.toString());

        try {
            FileHandler.writeGenesToFile(new File("output.txt"), genes);
        } catch (Exception ex) {
            resultArea.append("\nFailed to write to output.txt: " + ex.getMessage());
        }
    }
}
