

public class Runner {

    public static void main(String[] args) {
        String text = "An 18-year-old student, Abdel Aziz Hussein Abdelbaki, " +
                "was jailed for 15 years.";
        PatternExtractor extractor = new PatternExtractor("{pos:/VB.*/=verb >/NMOD:FOR/ {NER:DURATION}=duration");
    }
}
