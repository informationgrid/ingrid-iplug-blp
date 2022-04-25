package de.ingrid.iplug.dsc.index;

import de.ingrid.admin.JettyStarter;
import de.ingrid.iplug.dsc.index.scraper.BlpScraper;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class SimpleBLPScraperTest {

    public SimpleBLPScraperTest() throws Exception {
        new JettyStarter(false);
    }

    @Test
    public void testScraping() throws IOException {
        long startTime = System.nanoTime();
        String url = "http://www.ueberlingen.de/startseite/bauen+_+wohnen/beteiligungen.html";
        BlpScraper blpScraper = new BlpScraper();
        List<String> locationNames = blpScraper.scrapeUrlForLocationNames( url );

        System.out.println(locationNames);

        assertTrue(locationNames.contains( "Nußdorf" ));
        assertTrue(locationNames.contains( "Uferpark" ));
        assertTrue(locationNames.contains( "Überlingen" ));
        assertTrue(locationNames.contains( "Altbirnau" ));

        long endTime   = System.nanoTime();
        long totalTime = (endTime - startTime) / 1_000_000_000;
        System.out.println( "Total time in seconds: " + totalTime );
    }

    @Test
    public void testLocationNER() {
        long startTime = System.nanoTime();

        StanfordCoreNLP pipeline = new StanfordCoreNLP("german");
        CoreDocument document = pipeline.processToCoreDocument("Goethe, geboren in Frankfurt am Main, studierte in Leipzig und war als Advokat in Wetzlar tätig. Er starb in Weimar");
        List<CoreLabel> coreLabelList = document.tokens();

        for (CoreLabel coreLabel: coreLabelList){
            String ner = coreLabel.get( CoreAnnotations.NamedEntityTagAnnotation.class);
            System.out.println(coreLabel.originalText() + "->"+ner);
        }

        long endTime   = System.nanoTime();
        long totalTime = (endTime - startTime) / 1_000_000_000;
        System.out.println( "Total time in seconds: " + totalTime );
    }

}
