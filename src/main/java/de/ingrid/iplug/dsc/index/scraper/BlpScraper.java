/*-
 * **************************************************-
 * InGrid iPlug BLP
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
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
package de.ingrid.iplug.dsc.index.scraper;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlunit.BrowserVersion;
import org.htmlunit.WebClient;
import org.htmlunit.html.DomNodeList;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlPage;
import org.springframework.stereotype.Service;

@Service
public class BlpScraper {

    private static final Log log = LogFactory.getLog( BlpScraper.class );

    private WebClient client;

    public BlpScraper() {
        client = new WebClient( BrowserVersion.BEST_SUPPORTED );
        client.getOptions().setCssEnabled( false );
        client.getOptions().setJavaScriptEnabled( false );
        client.getOptions().setPrintContentOnFailingStatusCode( false );
        client.getOptions().setTimeout( 10000 );
    }

    public String scrapeUrl(String url) {
        String content = "";
        HtmlPage blpPage = getHtmlPage( url );
        if (blpPage != null) {
            content = getContentFromPage( blpPage );
        }
        return content;
    }

    public HtmlPage getHtmlPage(String url) {
        HtmlPage blpPage = null;
        try {
            blpPage = client.getPage( url );
        } catch (Exception e) {
            log.warn( "Error getting URL: " + e.getMessage() );
        }
        return blpPage;
    }

    public String getContentFromPage(HtmlPage blpPage) {
        String content = null;
        try {
            HtmlElement document = blpPage.getDocumentElement();
            // Remove non-content
            document.removeChild( "footer", 0 );
            document.removeChild( "header", 0 );
            document.removeChild("noscript", 0);
            content = document.asNormalizedText();
            content = content.replace( "\n", " " );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content;
    }

    public void removeAllChildrenWithTag(HtmlElement htmlElement, String tag) {
        DomNodeList<HtmlElement> anchors = htmlElement.getElementsByTagName( tag );
        for (int i = 0; i < anchors.size(); i++) {
            anchors.get( 0 ).remove();
        }
    }

}
