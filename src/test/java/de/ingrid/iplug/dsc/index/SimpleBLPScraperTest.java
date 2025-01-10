/*-
 * **************************************************-
 * InGrid iPlug BLP
 * ==================================================
 * Copyright (C) 2014 - 2025 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.iplug.dsc.index;

import de.ingrid.iplug.dsc.index.scraper.BlpScraper;
import org.junit.jupiter.api.Test;

public class SimpleBLPScraperTest {

    @Test
    void testScraping()  {
        long startTime = System.nanoTime();
        String url = "http://www.ueberlingen.de/startseite/bauen+_+wohnen/beteiligungen.html";
        BlpScraper blpScraper = new BlpScraper();
        String content = blpScraper.scrapeUrl(url);
        System.out.println(content);

        long endTime   = System.nanoTime();
        long totalTime = (endTime - startTime) / 1_000_000_000;
        System.out.println("Total time in seconds: " + totalTime);
    }

    @Test
    void testScrapingRedirect() {
        long startTime = System.nanoTime();
        String url = "https://www.boldecker-land.de/portal/startseite.html";
        BlpScraper blpScraper = new BlpScraper();
        //        Set<String> entries = blpScraper.scrapeUrl(url);
        String content = blpScraper.scrapeUrl(url);
        System.out.println(content);

        long endTime   = System.nanoTime();
        long totalTime = (endTime - startTime) / 1_000_000_000;
        System.out.println("Total time in seconds: " + totalTime);
    }

    @Test
    void testPageWithJavaScript() {
        long startTime = System.nanoTime();
        String url = "https://geoportal-luckau.de/viewer2.php";
        BlpScraper blpScraper = new BlpScraper();
        String content = blpScraper.scrapeUrl(url);
        System.out.println(content);

        long endTime   = System.nanoTime();
        long totalTime = (endTime - startTime) / 1_000_000_000;
        System.out.println("Total time in seconds: " + totalTime);
    }

}
