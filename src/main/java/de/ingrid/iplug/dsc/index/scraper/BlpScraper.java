package de.ingrid.iplug.dsc.index.scraper;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BlpScraper {

    private final static String stopWordsPath = "stop_words_german.json";
    //TODO: Keep only words starting with capital letter and keep words with hyphen
    private final static String germanRegex = "[^a-zA-Z\\u00F0-\\u02AF]";

    public BlpScraper(String url) throws IOException {
        scrapeHtmlUnit();
    }

    public final static Set<String> GERMAN_STOP_WORDS = new HashSet<>(
            Arrays.asList( "einer",
                    "eine", "eines", "einem", "einen", "der", "die", "das",
                    "dass", "daß", "du", "er", "sie", "es", "was", "wer",
                    "wie", "wir", "und", "oder", "ohne", "mit", "am", "im",
                    "in", "aus", "auf", "ist", "sein", "war", "wird", "ihr",
                    "ihre", "ihres", "ihnen", "ihrer", "als", "für", "von",
                    "mit", "dich", "dir", "mich", "mir", "mein", "sein",
                    "kein", "durch", "wegen", "wird", "sich", "bei", "beim",
                    "noch", "den", "dem", "zu", "zur", "zum", "auf", "ein",
                    "auch", "werden", "an", "des", "sein", "sind", "vor",
                    "nicht", "sehr", "um", "unsere", "ohne", "so", "da", "nur",
                    "diese", "dieser", "diesem", "dieses", "nach", "über",
                    "mehr", "hat", "bis", "uns", "unser", "unserer", "unserem",
                    "unsers", "euch", "euers", "euer", "eurem", "ihr", "ihres",
                    "ihrer", "ihrem", "alle", "vom" ) );

    public void scrapeHtmlUnit() {
        try (WebClient client = new WebClient();) {
            client.getOptions().setCssEnabled( false );
            client.getOptions().setJavaScriptEnabled( false );

            try {
                HtmlPage blpPage = client.getPage( "http://www.ueberlingen.de/startseite/bauen+_+wohnen/beteiligungen.html" );
                HtmlElement htmlElement  = blpPage.getDocumentElement();

                htmlElement.removeChild("footer", 0);
                htmlElement.removeChild("nav", 0);
                htmlElement.removeChild("header", 0);
                removeAllChildrenWithTag( htmlElement, "a" );
                removeAllChildrenWithTag( htmlElement, "table" );

                String text = htmlElement.asNormalizedText();
                text = text.replace( "\n", " " );

                Set<String> stringList = Collections.list(new StringTokenizer( text, " " )).stream()
                        .map(token -> (String) token)
                        .collect(Collectors.toSet());

                System.out.println(stringList);
                stringList.removeAll( GERMAN_STOP_WORDS );
                stringList.removeIf( Pattern.compile(germanRegex).asPredicate() );
                System.out.println(stringList);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void removeAllChildrenWithTag(HtmlElement htmlElement, String tag) {
        DomNodeList<HtmlElement> anchors = htmlElement.getElementsByTagName( tag );
        int anchorsSize = anchors.size();
        for (int i = 0; i < anchorsSize; i++) {
            anchors.get(0).remove();
        }
    }

    //TODO: Remove stop words case-insensitively
    public void filterStopWords() {

    }


}
