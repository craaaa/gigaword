import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.semgrex.SemgrexMatcher;
import edu.stanford.nlp.semgraph.semgrex.SemgrexPattern;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.logging.Redwood;
import edu.stanford.nlp.util.logging.RedwoodConfiguration;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Properties;

import static edu.stanford.nlp.util.logging.RedwoodConfiguration.Handlers.*;

public class PatternExtractor {
    /**
     * Takes in a directory and returns an iterator of Strings
     * over all the files in the directory
     */

    private StanfordCoreNLP pipeline;
    private FileOutputStream verbSavedSentences;
    private FileOutputStream nounSavedSentences;
    private Redwood.RedwoodChannels log;


    public PatternExtractor(Path saveFileDir, String fileName) throws IOException {
        RedwoodConfiguration.current().handlers(chain(showOnlyChannels(fileName), file(fileName + ".log")));
        log = Redwood.channels(fileName);
        Properties props = new Properties();
        verbSavedSentences = new FileOutputStream( saveFileDir.resolve(fileName + "v").toFile());
        nounSavedSentences = new FileOutputStream( saveFileDir.resolve(fileName + "n").toFile());
        props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
        pipeline = new StanfordCoreNLP(props);
    }


    public void pipe(String text){
        Annotation document = new Annotation(text);
        pipeline.annotate(document);
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        for(CoreMap sentence: sentences) {
            log.info("Checking if sentence matches:" + sentence);
            // System.out.println("Relations:" + g.findAllRelns(UniversalEnglishGrammaticalRelations.getNmod("for")));

            // Save verb patterns
            SemgrexPattern verbPattern = SemgrexPattern.compile("{tag:/VB.*/} >/nmod:for/ {ner:DURATION}");
            saveGraphIfMatches(verbSavedSentences, verbPattern, sentence);

            // Save noun patterns
            SemgrexPattern nounPattern = SemgrexPattern.compile("{tag:/NN.*/} >/nmod:for/ {ner:DURATION}");
            saveGraphIfMatches(nounSavedSentences, nounPattern, sentence);
        }
    }

    private void saveGraphIfMatches(FileOutputStream savedSentences,
                                           SemgrexPattern pattern, CoreMap sentence) {
        SemanticGraph g = sentence.get(
                SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation.class);
        if (matchesSemgrex(pattern, g)){
            try {
                savedSentences.write(sentence.toString().getBytes());
                log.info("Saved sentence: " + g.toRecoveredSentenceString());
            } catch (IOException e) {
                log.err("Failed to save sentence: " + g.toRecoveredSentenceString());
            }
            catch (ConcurrentModificationException e){
                log.err("Failed to save sentence: " + g.toRecoveredSentenceString());
            }
        }
    }

    private static void printMatches(SemgrexMatcher matcher) {
        while (matcher.find()) {
            System.out.println(matcher.getNode("verb") + " " +
                    matcher.getRelnString("rel") + " " +
                    matcher.getNode("duration"));
        }
    }

    private static boolean matchesSemgrex(SemgrexPattern pattern, SemanticGraph graph){
        SemgrexMatcher matcher = pattern.matcher(graph);
        return matcher.find();
    }
}
