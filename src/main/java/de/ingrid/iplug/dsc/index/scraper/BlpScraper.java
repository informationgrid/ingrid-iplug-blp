/*-
 * **************************************************-
 * InGrid iPlug BLP
 * ==================================================
 * Copyright (C) 2014 - 2022 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.iplug.dsc.index.scraper;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BlpScraper {

    private static final Log log = LogFactory.getLog( BlpScraper.class );

    public BlpScraper() {
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
        try (WebClient client = new WebClient()) {
            client.getOptions().setCssEnabled( false );
            client.getOptions().setJavaScriptEnabled( false );
            client.getOptions().setPrintContentOnFailingStatusCode( false );
            client.getOptions().setTimeout( 5000 );

            try {
                blpPage = client.getPage( url );
            } catch (Exception e) {
                log.warn( "Error getting URL: " + e.getMessage() );
            }
            return blpPage;
        }
    }

    public String getContentFromPage(HtmlPage blpPage) {
        String content = null;
        try {
            HtmlElement htmlElement = blpPage.getDocumentElement();
            // Remove non-content
            htmlElement.removeChild( "footer", 0 );
            htmlElement.removeChild( "nav", 0 );
            htmlElement.removeChild( "header", 0 );
            removeAllChildrenWithTag( htmlElement, "a" );
            removeAllChildrenWithTag( htmlElement, "table" );
            removeAllChildrenWithTag( htmlElement, "aside" );
            content = htmlElement.asNormalizedText();
            content = content.replace( "\n", " " );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content;
    }

    public void removeAllChildrenWithTag(HtmlElement htmlElement, String tag) {
        DomNodeList<HtmlElement> anchors = htmlElement.getElementsByTagName( tag );
        int anchorsSize = anchors.size();
        for (int i = 0; i < anchorsSize; i++) {
            anchors.get( 0 ).remove();
        }
    }

}
