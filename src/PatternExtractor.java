import edu.stanford.nlp.parser.lexparser.EnglishTreebankParserParams;
import edu.stanford.nlp.parser.lexparser.TreebankLangParserParams;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphFactory;
import edu.stanford.nlp.semgraph.semgrex.SemgrexMatcher;
import edu.stanford.nlp.semgraph.semgrex.SemgrexPattern;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.Tree;

public class PatternExtractor {
    SemgrexPattern semgrex;
    GrammaticalStructureFactory factory;

    public PatternExtractor(String pattern){
        semgrex = SemgrexPattern.compile(pattern);
        TreebankLangParserParams params = new EnglishTreebankParserParams();
        factory = params.treebankLanguagePack().grammaticalStructureFactory(
                params.treebankLanguagePack().punctuationWordRejectFilter(),
                params.typedDependencyHeadFinder());
    }

    public SemgrexMatcher matches(Tree tree) {
        GrammaticalStructure structure = factory.newGrammaticalStructure(tree);
        SemanticGraph graph = SemanticGraphFactory.generateEnhancedDependencies(structure);
        SemgrexMatcher matcher = semgrex.matcher(graph);
        return matcher;
    }
}
