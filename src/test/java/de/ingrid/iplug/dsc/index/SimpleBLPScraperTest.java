package de.ingrid.iplug.dsc.index;

import de.ingrid.admin.JettyStarter;
import de.ingrid.iplug.dsc.index.scraper.BlpScraper;
import org.junit.Test;

import java.io.IOException;

public class SimpleBLPScraperTest {

    public SimpleBLPScraperTest() throws Exception {
        new JettyStarter(false);
    }

    @Test
    public void testScraping() throws IOException {
        long startTime = System.nanoTime();
        String url = "http://www.ueberlingen.de/startseite/bauen+_+wohnen/beteiligungen.html";
        BlpScraper blpScraper = new BlpScraper(url);


        long endTime   = System.nanoTime();
        long totalTime = (endTime - startTime) / 1_000_000_000;
        System.out.println( "Total time in seconds: " + totalTime );
    }

}
