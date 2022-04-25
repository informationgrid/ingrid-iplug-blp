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

    StanfordCoreNLP pipeline;

    public BlpScraper() throws IOException {
        pipeline = new StanfordCoreNLP( "german" );
    }

    /**
     * Scrape given url for location names
     *
     * @param url
     * @return
     */
    public List<String> scrapeUrlForLocationNames(String url) {

        Set<String> tokens = new HashSet<>();

        try (WebClient client = new WebClient()) {
            client.getOptions().setCssEnabled( false );
            client.getOptions().setJavaScriptEnabled( false );

            try {
                HtmlPage blpPage = client.getPage( url );
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

                for (HtmlElement header : headersAll) {
                    String text = header.asNormalizedText();
                    text = text.replace( "\"", "" );
                    // Tokenize by splitting at whitespace and adding to set to remove duplicates
                    tokens.addAll( Collections.list( new StringTokenizer( text, " " ) ).stream()
                            .map( token -> (String) token )
                            .collect( Collectors.toSet() ) );
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return extractLocationNames( tokens );
    }

    /**
     * https://stanfordnlp.github.io/CoreNLP/index.html
     * Given a string returns a list of German Location Names using Named Entity Recognition
     *
     * @param tokens
     * @return
     */
    public List<String> extractLocationNames(Set<String> tokens) {
        // Collect all tokens in single string
        String joinedTokens = String.join(" ", tokens);
        List<String> locationNames = new ArrayList<>();
        //        StanfordCoreNLP pipeline = new StanfordCoreNLP( "german" );
        CoreDocument document = pipeline.processToCoreDocument( joinedTokens );
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
