import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.semgraph.semgrex.SemgrexMatcher;
import edu.stanford.nlp.semgraph.semgrex.SemgrexPattern;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.TreeLemmatizer;
import edu.stanford.nlp.util.CoreMap;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class GigawordParser {
    /**
     * Takes in a directory and returns an iterator of Strings
     * over all the files in the directory
     */

    Properties properties;
    DependencyParser depParser;
    MaxentTagger tokenizer;
    StanfordCoreNLP pipeline;

    public GigawordParser() {
        // tokenizer = new MaxentTagger(MaxentTagger.DEFAULT_JAR_PATH);
        // lemmatizer = new TreeLemmatizer();
        // depParser = DependencyParser.loadFromModelFile(DependencyParser.DEFAULT_MODEL);
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
        pipeline = new StanfordCoreNLP(props);
    }

    public List<SemanticGraph> parse(String text){
        List<SemanticGraph> graphs = new ArrayList<SemanticGraph>();
        DocumentPreprocessor splitter = new DocumentPreprocessor(new StringReader(text));
        for (List<HasWord> sentence : splitter) {
            List<TaggedWord> tagged = tokenizer.tagSentence(sentence);
            GrammaticalStructure gs = depParser.predict(tagged);
            graphs.add(new SemanticGraph(gs.typedDependenciesEnhancedPlusPlus()));
        }
        return graphs;
    }

    public List<SemanticGraph> pipe(String text){
        Annotation document = new Annotation(text);
        pipeline.annotate(document);
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        List<SemanticGraph> graphs = new ArrayList<SemanticGraph>();
        for(CoreMap sentence: sentences) {
            SemanticGraph dependencies = sentence.get(
                    SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation.class);
            graphs.add(dependencies);
        }
        return graphs;
    }

    public static void main(String[] args) {
        GigawordParser p = new GigawordParser();
        String text = "It was the first vote in 30 years in which 88-year-old Balaguer, who has \n" +
                "dominated politics in this Caribbean country for decades, was not a candidate. \n";
        System.out.println(text);
        List<SemanticGraph> graphs = p.pipe(text);
        SemgrexPattern pattern = SemgrexPattern.compile("{tag:/VB.*/}=verb >=reln {ner:DURATION}=duration");
        for (SemanticGraph g : graphs){
            System.out.println(g);
            System.out.println(g.findAllRelns(GrammaticalRelation.valueOf("nmod:for")));
            SemgrexMatcher matcher = pattern.matcher(g);
            while (matcher.find()) {
                System.out.println(matcher.getNode("verb") + " " +
                        matcher.getNode("reln") + " " +
                        matcher.getNode("duration"));
            }
        }
    }
}
