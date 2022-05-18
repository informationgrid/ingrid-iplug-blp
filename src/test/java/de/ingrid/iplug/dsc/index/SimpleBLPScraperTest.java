package de.ingrid.iplug.dsc.index;

import de.ingrid.admin.JettyStarter;
import de.ingrid.iplug.dsc.index.scraper.BlpScraper;
import org.junit.Test;

import java.io.IOException;
import java.util.Set;

public class SimpleBLPScraperTest {

    public SimpleBLPScraperTest() throws Exception {
        new JettyStarter(false);
    }

    @Test
    public void testScraping() throws IOException {
        long startTime = System.nanoTime();
        String url = "http://www.ueberlingen.de/startseite/bauen+_+wohnen/beteiligungen.html";
        BlpScraper blpScraper = new BlpScraper();
        Set<String> entries = blpScraper.scrapeUrl(url);
        System.out.println(entries);

        long endTime   = System.nanoTime();
        long totalTime = (endTime - startTime) / 1_000_000_000;
        System.out.println( "Total time in seconds: " + totalTime );
    }

    @Test
    public void testScrapingRedirect() {
        long startTime = System.nanoTime();
        String url = "https://www.boldecker-land.de/portal/startseite.html";
        BlpScraper blpScraper = new BlpScraper();
        Set<String> entries = blpScraper.scrapeUrl(url);
        if (entries.size() > 0) {
            System.out.println(entries);
            System.out.println(entries.size());
        }

        long endTime   = System.nanoTime();
        long totalTime = (endTime - startTime) / 1_000_000_000;
        System.out.println( "Total time in seconds: " + totalTime );
    }

}
