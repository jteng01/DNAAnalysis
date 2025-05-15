import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;

public class GeneFinderUI {
    private final JTextArea sequenceArea;
    private final JTextArea resultArea;
    private final JLabel statusLabel;
    private String dnaSequence = "";
    private ArrayList<String> currentGenes = new ArrayList<>();

    public GeneFinderUI() {
        JFrame frame = new JFrame("DNA Gene Finder");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 450);

        JButton loadButton = new JButton("Load DNA File");
        JButton findButton = new JButton("Locate Genes");
        JButton saveButton = new JButton("Save Genes to File");

        loadButton.addActionListener(this::handleLoad);
        findButton.addActionListener(this::handleFindGenes);
        saveButton.addActionListener(this::handleSaveGenes);

        sequenceArea = new JTextArea();
        sequenceArea.setEditable(true);
        JScrollPane sequenceScrollPane = new JScrollPane(sequenceArea);
        sequenceScrollPane.setBorder(BorderFactory.createTitledBorder("Loaded DNA Sequence"));

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        JScrollPane resultScrollPane = new JScrollPane(resultArea);
        resultScrollPane.setBorder(BorderFactory.createTitledBorder("Gene Results"));

        statusLabel = new JLabel("Status: Ready");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JPanel topPanel = new JPanel();
        topPanel.add(loadButton);
        topPanel.add(findButton);
        topPanel.add(saveButton);

        JPanel textAreasPanel = new JPanel();
        textAreasPanel.setLayout(new BoxLayout(textAreasPanel, BoxLayout.Y_AXIS));
        textAreasPanel.add(sequenceScrollPane);
        textAreasPanel.add(resultScrollPane);

        frame.getContentPane().add(topPanel, BorderLayout.NORTH);
        frame.getContentPane().add(textAreasPanel, BorderLayout.CENTER);
        frame.getContentPane().add(statusLabel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private void handleLoad(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Text Files (*.txt)", "txt"));

        int result = chooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();

            try {
                String content = FileHandler.readDNAFromFile(selected).toUpperCase();

                if (content.isEmpty()) {
                    sequenceArea.setText("");
                    resultArea.setText("");
                    statusLabel.setText("File is empty.");
                    return;
                }

                if (!content.matches("[CATG]*")) {
                    sequenceArea.setText("");
                    resultArea.setText("");
                    statusLabel.setText("Invalid characters found. Use only C, A, T, G.");
                    return;
                }

                dnaSequence = content;
                sequenceArea.setText(dnaSequence);
                statusLabel.setText("Loaded DNA from " + selected.getName());

            } catch (Exception ex) {
                sequenceArea.setText("");
                resultArea.setText("");
                statusLabel.setText("Error loading file - " + ex.getMessage());
            }
        } else {
            statusLabel.setText("Load operation cancelled.");
        }
    }

    private void handleFindGenes(ActionEvent e) {
        String userInput = sequenceArea.getText().toUpperCase().trim();

        if (userInput.isEmpty()) {
            resultArea.setText("");
            statusLabel.setText("No DNA sequence entered.");
            return;
        }

        if (!userInput.matches("[CATG]+")) {
            resultArea.setText("");
            statusLabel.setText("Invalid characters in sequence.");
            return;
        }

        DNAReader reader = new DNAReader(userInput);
        currentGenes = reader.locateGenes();

        if (currentGenes.isEmpty()) {
            resultArea.setText("");
            statusLabel.setText("No genes found.");
        } else {
            StringBuilder output = new StringBuilder();
            for (String gene : currentGenes) {
                output.append(gene).append("\n");
            }
            resultArea.setText(output.toString().trim());
            statusLabel.setText("Found " + currentGenes.size() + " gene(s).");
        }
    }

    private void handleSaveGenes(ActionEvent e) {
        if (currentGenes == null || currentGenes.isEmpty()) {
            statusLabel.setText("No genes to save.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Genes As");
        chooser.setFileFilter(new FileNameExtensionFilter("Text Files (*.txt)", "txt"));

        int option = chooser.showSaveDialog(null);

        if (option == JFileChooser.APPROVE_OPTION) {
            File fileToSave = chooser.getSelectedFile();

            if (!fileToSave.getName().toLowerCase().endsWith(".txt")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".txt");
            }

            if (fileToSave.exists()) {
                int confirm = JOptionPane.showConfirmDialog(null,
                        "File exists. Overwrite?",
                        "Confirm Overwrite",
                        JOptionPane.YES_NO_OPTION);
                if (confirm != JOptionPane.YES_OPTION) {
                    statusLabel.setText("Save cancelled.");
                    return;
                }
            }

            try {
                FileHandler.writeGenesToFile(fileToSave, currentGenes);
                statusLabel.setText("Genes saved to " + fileToSave.getAbsolutePath());
            } catch (Exception ex) {
                statusLabel.setText("Failed to save - " + ex.getMessage());
            }
        } else {
            statusLabel.setText("Save operation cancelled.");
        }
    }
}