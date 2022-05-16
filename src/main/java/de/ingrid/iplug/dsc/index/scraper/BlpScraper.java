package de.ingrid.iplug.dsc.index.scraper;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BlpScraper {
    private final static String regex = "\\b[A-Z\\u00c4-\\u00dc].*[a-z]$";

    public BlpScraper() throws IOException {
    }

    public Set<String> scrapeUrl(String url) {
        Set<String> stringSet = new HashSet<>();
        //TODO: Handle failing Status Code
        try (WebClient client = new WebClient()) {
            client.getOptions().setCssEnabled( false );
            client.getOptions().setJavaScriptEnabled( false );
//            client.getOptions().setThrowExceptionOnFailingStatusCode( false );

            try {
                HtmlPage blpPage = client.getPage( url );
                HtmlElement htmlElement  = blpPage.getDocumentElement();

                htmlElement.removeChild("footer", 0);
                htmlElement.removeChild("nav", 0);
                htmlElement.removeChild("header", 0);
                removeAllChildrenWithTag( htmlElement, "a" );
                removeAllChildrenWithTag( htmlElement, "table" );

                String content = htmlElement.asNormalizedText();
                content = content.replace( "\n", " " );

                stringSet = Collections.list(new StringTokenizer( content, " " )).stream()
                        .map(token -> ((String) token).replace( "\"", "" ))
                        .filter( Pattern.compile( regex ).asPredicate() )
                        .collect(Collectors.toSet());
//                System.out.println(stringSet);
//                System.out.println(stringSet.size());

            } catch (IOException | FailingHttpStatusCodeException e) {
                e.printStackTrace();
            }
        }
        return stringSet;
    }
    public void removeAllChildrenWithTag(HtmlElement htmlElement, String tag) {
        DomNodeList<HtmlElement> anchors = htmlElement.getElementsByTagName( tag );
        int anchorsSize = anchors.size();
        for (int i = 0; i < anchorsSize; i++) {
            anchors.get(0).remove();
        }
    }

}
