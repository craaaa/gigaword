import javax.sound.midi.Sequence;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Runner {

    public static final Path DTD_FILE = Paths.get("/home/craa/Documents/gigaword/docs/gigaword.dtd");
    private static final String GIGAWORD_DIR = "/home/craa/Documents/gigaword";
    private static final Path SAVE_DIR =  Paths.get("/home/craa/Documents/gigaword/save");
    private static final String[] PAPER_NAMES = {"afe", "apw", "nyt", "xie"};

    public static void parseFile(Path file) {
        GigawordParser parser = new GigawordParser();
        ArrayList<String> texts = parser.parse(wrapWithRoot(file));

        PatternExtractor extractor;
        try {
            extractor = new PatternExtractor(SAVE_DIR, file.getFileName().toString());
        } catch (IOException e) {
            return;
        }
        for (String text : texts) {
            extractor.pipe(text);
        }
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

    private static SequenceInputStream wrapWithRoot(Path file) {
        List<InputStream> streams = null;
        try {
            streams = Arrays.asList(
                    Files.newInputStream(DTD_FILE),
                    new ByteArrayInputStream("<GWENG>".getBytes()),
                    Files.newInputStream(file),
                    new ByteArrayInputStream("</GWENG>".getBytes()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new SequenceInputStream(Collections.enumeration(streams));
    }
}
