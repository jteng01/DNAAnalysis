import java.io.*;
import java.util.List;

public class FileHandler {
    public static String readDNAFromFile(File file) throws IOException {
        StringBuilder sb = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            System.out.println("File size (bytes): " + file.length());
            while ((line = reader.readLine()) != null) {
                sb.append(line.trim());
            }
        }


        return sb.toString();
    }

    public static void writeGenesToFile(File file, List<String> genes) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            if (genes.isEmpty()) {
                writer.write("No genes found.\n");
            } else {
                writer.write("Potential Genes found:\n");
                for (String gene : genes) {
                    writer.write(gene + "\n");
                }
            }
        }
    }
}