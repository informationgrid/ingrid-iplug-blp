/*
 * **************************************************-
 * InGrid-iPlug DSC
 * ==================================================
 * Copyright (C) 2014 - 2021 wemove digital solutions GmbH
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
package de.ingrid.iplug.dsc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.admin.JettyStarter;
import de.ingrid.admin.elasticsearch.IndexScheduler;
import de.ingrid.elasticsearch.ElasticConfig;
import de.ingrid.elasticsearch.IBusIndexManager;
import de.ingrid.elasticsearch.search.IndexImpl;
import de.ingrid.iplug.HeartBeatPlug;
import de.ingrid.iplug.PlugDescriptionFieldFilters;
import de.ingrid.utils.IRecordLoader;
import de.ingrid.utils.IngridCall;
import de.ingrid.utils.IngridDocument;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.dsc.Record;
import de.ingrid.utils.metadata.IMetadataInjector;
import de.ingrid.utils.processor.IPostProcessor;
import de.ingrid.utils.processor.IPreProcessor;
import de.ingrid.utils.query.ClauseQuery;
import de.ingrid.utils.query.FieldQuery;
import de.ingrid.utils.query.IngridQuery;

/**
 * This iPlug connects to the iBus delivers search results based on a index.
 *
 * @author joachim@wemove.com
 */
@Service
public class BlpSearchPlug extends HeartBeatPlug implements IRecordLoader {

    /**
     * The logging object
     */
    private static Log log = LogFactory.getLog( BlpSearchPlug.class );

    @Autowired
    private ElasticConfig elasticConfig;

    @Autowired
    private IBusIndexManager iBusIndexManager;

    private final IndexImpl _indexSearcher;
    private final IndexScheduler indexScheduler;

    @Autowired
    public BlpSearchPlug(final IndexImpl indexSearcher, IMetadataInjector[] injector, IPreProcessor[] preProcessors, IPostProcessor[] postProcessors, IndexScheduler indexScheduler) {
        super( 60000, new PlugDescriptionFieldFilters(), injector, preProcessors, postProcessors );
        _indexSearcher = indexSearcher;
        this.indexScheduler = indexScheduler;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.ingrid.utils.ISearcher#search(de.ingrid.utils.query.IngridQuery, int, int)
     */
    @Override
    public final IngridHits search(final IngridQuery query, final int start, final int length) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug( "Incoming query: " + query.toString() + ", start=" + start + ", length=" + length );
        }
        preProcess( query );

        // request iBus directly to get search results from within this iPlug
        // adapt query to only get results coming from this iPlug and activated in iBus
        // But when not connected to an iBus then use direct connection to Elasticsearch
        if (elasticConfig.esCommunicationThroughIBus) {

            ClauseQuery cq = new ClauseQuery( true, false );
            cq.addField( new FieldQuery( true, false, "iPlugId", JettyStarter.baseConfig.communicationProxyUrl ) );
            query.addClause( cq );
            return this.iBusIndexManager.search( query, start, length );
        }

        return _indexSearcher.search( query, start, length );
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.ingrid.utils.IRecordLoader#getRecord(de.ingrid.utils.IngridHit)
     */
    @Override
    public Record getRecord(IngridHit hit) throws Exception {
        // no recordproducer implemented in BLP iPlug
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.ingrid.iplug.HeartBeatPlug#close()
     */
    @Override
    public void close() {
        _indexSearcher.close();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.ingrid.iplug.HeartBeatPlug#close()
     */
    @Override
    public IngridHitDetail getDetail(IngridHit hit, IngridQuery query, String[] fields) {
        // request iBus directly to get search results from within this iPlug
        // adapt query to only get results coming from this iPlug and activated in iBus
        // But when not connected to an iBus then use direct connection to Elasticsearch
        if (elasticConfig.esCommunicationThroughIBus) {
            return this.iBusIndexManager.getDetail( hit, query, fields );
        }

        return _indexSearcher.getDetail( hit, query, fields );
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.ingrid.iplug.HeartBeatPlug#close()
     */
    @Override
    public IngridHitDetail[] getDetails(IngridHit[] hits, IngridQuery query, String[] fields) {
        // request iBus directly to get search results from within this iPlug
        // adapt query to only get results coming from this iPlug and activated in iBus
        // But when not connected to an iBus then use direct connection to Elasticsearch
        if (elasticConfig.esCommunicationThroughIBus) {
            return this.iBusIndexManager.getDetails( hits, query, fields );
        }

        return _indexSearcher.getDetails( hits, query, fields );
    }

    public static void main(String[] args) throws Exception {
        new JettyStarter( Configuration.class );
    }

    @Override
    public IngridDocument call(IngridCall info) {
        IngridDocument doc = null;

        if ("index".equals( info.getMethod() )) {
            indexScheduler.triggerManually();
            doc = new IngridDocument();
            doc.put( "success", true );
        }
        log.warn( "The following method is not supported: " + info.getMethod() );

        return doc;
    }

}
