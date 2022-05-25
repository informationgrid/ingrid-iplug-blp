package de.ingrid.iplug.dsc.index.scraper;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;

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
            } catch (FailingHttpStatusCodeException statusCode) {
                log.info( "could not get page content: Error Code " + statusCode.getStatusCode() + ":  " + url );
            } catch (MalformedURLException e) {
                log.info( "malformed url exception: " + url );
            } catch (SocketTimeoutException e) {
                log.info( "timed out reading url: " + url );
            } catch (IOException e) {
                e.printStackTrace();
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
