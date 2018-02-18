import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Runner {

    private static final String GIGAWORD_DIR = "/home/craa/Documents/gigaword";
    private static final Path SAVE_DIR =  Paths.get("/home/craa/Documents/gigaword/save");
    private static final String[] PAPER_NAMES = {"afe", "apw", "nyt", "xie"};

    public static void parseFile(Path file) {
        List<String> parsedText = parseSgmlFromFile(file);

        PatternExtractor extractor;
        try {
            extractor = new PatternExtractor(SAVE_DIR, file.getFileName().toString());
        } catch (IOException e) {
            return;
        }
        for (String text : parsedText) {
            extractor.pipe(text);
        }
    }

    private static List<String> parseSgmlFromFile(Path file) {

    }

    public static void main(String[] args) {

        Arrays.stream(PAPER_NAMES)
                .map(p -> Paths.get(GIGAWORD_DIR, p))
                .flatMap(path -> {
                    try {
                        return Files.list(path);
                    } catch (IOException e) {
                        System.err.println("Couldn't list files in " + path.toString());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .parallel()
                .forEach(Runner::parseFile);
    }
}
