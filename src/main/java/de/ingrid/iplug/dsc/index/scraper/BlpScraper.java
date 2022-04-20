package de.ingrid.iplug.dsc.index.scraper;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BlpScraper {

    StanfordCoreNLP pipeline = new StanfordCoreNLP( "german" );
    Set<String> set = new HashSet<>();
    String words = "";
    HtmlPage blpPage;

    public BlpScraper() throws IOException {
    }

    /**
     * Scrape given url for location names
     *
     * @param url
     * @return
     */
    public List<String> scrapeUrlForLocationNames(String url) {

        try (WebClient client = new WebClient();) {
            client.getOptions().setCssEnabled( false );
            client.getOptions().setJavaScriptEnabled( false );

            try {
                blpPage = client.getPage( url );
                HtmlElement htmlElement = blpPage.getDocumentElement();

                // Collect content only from header tags
                List<HtmlElement> headers1 = htmlElement.getElementsByTagName( "h1" );
                List<HtmlElement> headers2 = htmlElement.getElementsByTagName( "h2" );
                List<HtmlElement> headers3 = htmlElement.getElementsByTagName( "h3" );
                List<HtmlElement> headers4 = htmlElement.getElementsByTagName( "h4" );

                // Join content into one List
                List<HtmlElement> headersAll = Stream.of( headers1, headers2, headers3, headers4 )
                        .flatMap( Collection::stream )
                        .collect( Collectors.toList() );

                // Remove duplicates with set
                for (HtmlElement header : headersAll) {
                    String text = header.asNormalizedText();
                    text = text.replace( "\"", "" );
                    // Tokenize by splitting at whitespace
                    List<String> tokens = Collections.list( new StringTokenizer( text, " " ) ).stream()
                            .map( token -> (String) token )
                            .collect( Collectors.toList() );

                    set.addAll( tokens );
                }

                // Collect all words in single string
                for (String word : set) {
                    words = words + word + " ";
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return extractLocationNames( words );
    }

    /**
     * https://stanfordnlp.github.io/CoreNLP/index.html
     * Given a string returns a list of German Location Names using Named Entity Recognition
     *
     * @param text
     * @return
     */
    public List<String> extractLocationNames(String text) {
        List<String> locationNames = new ArrayList<>();
        //        StanfordCoreNLP pipeline = new StanfordCoreNLP( "german" );
        CoreDocument document = pipeline.processToCoreDocument( text );
        List<CoreLabel> coreLabelList = document.tokens();

        for (CoreLabel coreLabel : coreLabelList) {
            String ner = coreLabel.get( CoreAnnotations.NamedEntityTagAnnotation.class );
            if (ner.equals( "LOCATION" )) {
                locationNames.add( coreLabel.originalText() );
            }
            //System.out.println(coreLabel.originalText() + "->" + ner);
        }
        return locationNames;
    }

    /**
     * Use to remove all html elements with given tag from HtmlElement
     * @param htmlElement
     * @param tag
     */
    public void removeAllChildrenWithTag(HtmlElement htmlElement, String tag) {
        DomNodeList<HtmlElement> anchors = htmlElement.getElementsByTagName( tag );
        int anchorsSize = anchors.size();
        for (int i = 0; i < anchorsSize; i++) {
            anchors.get( 0 ).remove();
        }
    }

}
