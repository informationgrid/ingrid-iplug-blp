package de.ingrid.iplug.dsc.index;

import de.ingrid.admin.JettyStarter;
import de.ingrid.iplug.dsc.index.scraper.BlpScraper;
import org.junit.Test;

public class SimpleBLPScraperTest {

    public SimpleBLPScraperTest() throws Exception {
        new JettyStarter(false);
    }

    @Test
    public void testScraping()  {
        long startTime = System.nanoTime();
        String url = "http://www.ueberlingen.de/startseite/bauen+_+wohnen/beteiligungen.html";
        BlpScraper blpScraper = new BlpScraper();
//        Set<String> entries = blpScraper.scrapeUrl(url);
        String content = blpScraper.scrapeUrl( url );
        System.out.println(content);

        long endTime   = System.nanoTime();
        long totalTime = (endTime - startTime) / 1_000_000_000;
        System.out.println( "Total time in seconds: " + totalTime );
    }

    @Test
    public void testScrapingRedirect() {
        long startTime = System.nanoTime();
        String url = "https://www.boldecker-land.de/portal/startseite.html";
        BlpScraper blpScraper = new BlpScraper();
        //        Set<String> entries = blpScraper.scrapeUrl(url);
        String content = blpScraper.scrapeUrl( url );
        System.out.println(content);

        long endTime   = System.nanoTime();
        long totalTime = (endTime - startTime) / 1_000_000_000;
        System.out.println( "Total time in seconds: " + totalTime );
    }

}
