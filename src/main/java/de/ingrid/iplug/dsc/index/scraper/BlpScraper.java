package de.ingrid.iplug.dsc.index.scraper;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import de.ingrid.iplug.dsc.BlpSearchPlug;
import de.ingrid.utils.statusprovider.StatusProvider;
import de.ingrid.utils.statusprovider.StatusProviderService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BlpScraper {
    private final static String regex = "^[A-Z\\u00c4-\\u00dc].*[a-zA-Z]$";
    private static final Log log = LogFactory.getLog( BlpSearchPlug.class );
    private final static String stopWordsPath = "src/main/resources/stop_words.txt";
    private Set<String> stopWords;
    private StatusProvider statusProvider;

    public BlpScraper() {
        this.stopWords = setStopWords();
    }

    public String scrapeUrl(String url) {
        String content = "";
//        Set<String> keySet = new HashSet<>();
        HtmlPage blpPage = getHtmlPage( url );
        if (blpPage != null) {
            content = getContentFromPage( blpPage );
//            keySet = filterContent( content );
        }
        return content;
}

    /**
     * Reads all words from stop_words.txt
     */
    public Set<String> setStopWords() {
        Set<String> stopWords = new HashSet<>();
        try (BufferedReader br = new BufferedReader( new FileReader( stopWordsPath ) )) {
            String line = br.readLine();

            while (line != null) {
                stopWords.add( line );
                //                stopWords.add(line.substring( 0,1 ).toUpperCase() + line.substring( 1 ));
                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stopWords;
    }

    public HtmlPage getHtmlPage(String url) {

        HtmlPage blpPage = null;
        try (WebClient client = new WebClient()) {
            client.getOptions().setCssEnabled( false );
            client.getOptions().setJavaScriptEnabled( false );
            client.getOptions().setPrintContentOnFailingStatusCode( false );
            client.getOptions().setTimeout( 5000 );
            //            client.getOptions().setThrowExceptionOnFailingStatusCodeblps( false );
            try {
                blpPage = client.getPage( url );
            } catch (FailingHttpStatusCodeException statusCode) {
                log.warn( "could not get page content: Error Code " + statusCode.getStatusCode() + ":  " + url );
            } catch (MalformedURLException e) {
                log.warn( "malformed url exception: " + url );
            } catch (SocketTimeoutException e) {
                log.warn( "timed out reading url: " + url );
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

    public Set<String> filterContent(String content) {
        Set<String> keySet = Collections.list( new StringTokenizer( content, " " ) ).stream()
                .map( token -> ((String) token) )
                .filter( Pattern.compile( regex ).asPredicate() )
                .filter( token -> !this.stopWords.contains( token.toLowerCase() ) )
                .collect( Collectors.toSet() );
        return keySet;
    }

    public void removeAllChildrenWithTag(HtmlElement htmlElement, String tag) {
        DomNodeList<HtmlElement> anchors = htmlElement.getElementsByTagName( tag );
        int anchorsSize = anchors.size();
        for (int i = 0; i < anchorsSize; i++) {
            anchors.get( 0 ).remove();
        }
    }

}
