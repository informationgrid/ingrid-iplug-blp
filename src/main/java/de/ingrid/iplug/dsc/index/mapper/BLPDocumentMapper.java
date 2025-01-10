/*
 * **************************************************-
 * InGrid-iPlug DSC
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
/**
 *
 */
package de.ingrid.iplug.dsc.index.mapper;

import de.ingrid.admin.Config;
import de.ingrid.iplug.dsc.index.scraper.BlpScraper;
import de.ingrid.iplug.dsc.om.BLPSourceRecord;
import de.ingrid.iplug.dsc.om.SourceRecord;
import de.ingrid.iplug.dsc.utils.Link;
import de.ingrid.iplug.dsc.utils.UVPDataImporter.BlpModel;
import de.ingrid.utils.ElasticDocument;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;

@Service("recordMapper")
public class BLPDocumentMapper implements IRecordMapper {

    final
    Config config;

    final
    BlpScraper blpScraper;

    Configuration freemarkerCfg;

    public BLPDocumentMapper(Config config, BlpScraper blpScraper) {
        this.config = config;
        this.blpScraper = blpScraper;
    }

    public void createFreemarkerCfg() throws IOException {
        if (freemarkerCfg == null) {
            freemarkerCfg = new Configuration( Configuration.VERSION_2_3_27 );
            freemarkerCfg.setClassForTemplateLoading( this.getClass(), "/" );
            freemarkerCfg.setDefaultEncoding( "UTF-8" );
            freemarkerCfg.setTemplateExceptionHandler( TemplateExceptionHandler.RETHROW_HANDLER );
            freemarkerCfg.setLogTemplateExceptions( false );
            freemarkerCfg.setWrapUncheckedExceptions( true );
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * de.ingrid.iplug.dsc.index.IRecord2DocumentMapper#map(de.ingrid.iplug.
     * dsc.index.IRecord)
     */
    @Override
    public void map(SourceRecord record, ElasticDocument doc) throws IOException, TemplateException {
        if (!(record instanceof BLPSourceRecord)) {
            throw new IllegalArgumentException( "Record is no BLPSourceRecord!" );
        }

        BlpModel model = (BlpModel) record.get( BLPSourceRecord.BLP_MODEL );

        addToDoc(doc, "title", "Bauleitplanung: " + model.name );
        // addToDoc(doc, "summary", model.descr );

        addToDoc(doc, "blp_name", model.name );
        addToDoc(doc, "blp_description", model.descr );
        doc.put( "x1", model.lon );
        doc.put( "x2", model.lon );
        doc.put( "y1", model.lat );
        doc.put( "y2", model.lat );

        ArrayList<Link> links = new ArrayList<>();
        if (model.urlBlpInProgress != null && !model.urlBlpInProgress.trim().isEmpty()) {
            addToDoc(doc, "blp_url_in_progress", model.urlBlpInProgress );
            links.add( new Link( model.urlBlpInProgress, "Bauleitpläne im Beteiligungsverfahren" ) );
        }
        if (model.urlBlpFinished != null && !model.urlBlpFinished.trim().isEmpty()) {
            addToDoc(doc, "blp_url_finished", model.urlBlpFinished );
            links.add( new Link( model.urlBlpFinished, "Wirksame/rechtskräftige Bauleitpläne" ) );
        }
        if (model.urlFnpInProgress != null && !model.urlFnpInProgress.trim().isEmpty()) {
            addToDoc(doc, "fnp_url_in_progress", model.urlFnpInProgress );
            links.add( new Link( model.urlFnpInProgress, "Flächennutzungspläne im Beteiligungsverfahren" ) );
        }
        if (model.urlFnpFinished != null && !model.urlFnpFinished.trim().isEmpty()) {
            addToDoc(doc, "fnp_url_finished", model.urlFnpFinished );
            links.add( new Link( model.urlFnpFinished, "Wirksame/rechtskräftige Flächennutzungspläne" ) );
        }
        if (model.urlBpInProgress != null && !model.urlBpInProgress.trim().isEmpty()) {
            addToDoc(doc, "bp_url_in_progress", model.urlBpInProgress );
            links.add( new Link( model.urlBpInProgress, "Bebauungspläne im Beteiligungsverfahren" ) );
        }
        if (model.urlBpFinished != null && !model.urlBpFinished.trim().isEmpty()) {
            addToDoc(doc, "bp_url_finished", model.urlBpFinished );
            links.add( new Link( model.urlBpFinished, "Wirksame/rechtskräftige Bebauungspläne" ) );
        }

        // Collect all urls, remove duplicates with set
        Set<String> urls = links.stream().map( Link::getUrl ).collect( Collectors.toSet() );

        List<String> entries = crawlUrls( urls );
        for (String entry: entries) {
            addToDoc( doc, "scraped_content", entry);
        }

        createFreemarkerCfg();
        Template temp = freemarkerCfg.getTemplate( "additional_html.ftl" );

        Map<String, Object> root = new HashMap<>();
        root.put( "description", model.descr != null && !model.descr.trim().isEmpty() ? model.descr : "");
        root.put( "links", links );

        Writer out = new StringWriter();
        temp.process( root, out );
        String additionalHtml = out.toString();

        addToDoc(doc, "additional_html_1", additionalHtml );

        // constants
        addToDoc(doc, "blp_marker", "blp_marker" );
        addToDoc(doc, "procedure", "dev_plan" );
        addToDoc(doc, "lang", "de" );

    }

    public void addToDoc(ElasticDocument doc, String fieldName, String value) {
        addToDoc(doc, fieldName, value, true);
    }

    public void addToDoc(ElasticDocument doc, String fieldName, String value, boolean analyzed) {
        if (value == null) {
            value = "";
        }

        fieldName = fieldName.toLowerCase();
        doc.put(fieldName, value);
        if (analyzed && !value.isEmpty()) {
            doc.put("content", value);
        }

    }

    public List<String> crawlUrls(Set<String> urls) {
        List<String> contents = new ArrayList<>();

        for (String url: urls) {
            if (null != url && url.length() > 0){
                String content = this.blpScraper.scrapeUrl( url );
                if (null != content && !content.equals( "") ) {
                    contents.add( content );
                }
            }
        }
        return contents;
    }
}
