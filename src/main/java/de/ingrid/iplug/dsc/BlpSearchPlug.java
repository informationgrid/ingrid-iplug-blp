/*
 * **************************************************-
 * InGrid-iPlug DSC
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
package de.ingrid.iplug.dsc;

import de.ingrid.admin.Config;
import de.ingrid.admin.elasticsearch.IndexScheduler;
import de.ingrid.elasticsearch.ElasticConfig;
import de.ingrid.elasticsearch.IBusIndexManager;
import de.ingrid.elasticsearch.search.IndexImpl;
import de.ingrid.iplug.HeartBeatPlug;
import de.ingrid.iplug.PlugDescriptionFieldFilters;
import de.ingrid.utils.*;
import de.ingrid.utils.dsc.Record;
import de.ingrid.utils.metadata.IMetadataInjector;
import de.ingrid.utils.processor.IPostProcessor;
import de.ingrid.utils.processor.IPreProcessor;
import de.ingrid.utils.query.ClauseQuery;
import de.ingrid.utils.query.FieldQuery;
import de.ingrid.utils.query.IngridQuery;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.util.scan.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.ImportResource;

import java.io.IOException;

/**
 * This iPlug connects to the iBus delivers search results based on an index.
 *
 * @author joachim@wemove.com
 */
@ImportResource({"/springapp-servlet.xml", "/override/*.xml"})
@SpringBootApplication(scanBasePackages = "de.ingrid")
@ComponentScan(
        basePackages = "de.ingrid",
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "de.ingrid.admin.object.DefaultDataType"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "de.ingrid.admin.object.BasePlug"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "de.ingrid.admin.BaseWebappApplication"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "de.ingrid.admin.controller.RedirectController"),

        })
public class BlpSearchPlug extends HeartBeatPlug implements IRecordLoader {

    /**
     * The logging object
     */
    private static final Log log = LogFactory.getLog(BlpSearchPlug.class);

    private final ElasticConfig elasticConfig;

    private final IBusIndexManager iBusIndexManager;

    private final IndexImpl _indexSearcher;
    private final IndexScheduler indexScheduler;

    private final Config baseConfig;
    
    @Autowired
    public BlpSearchPlug(final IndexImpl indexSearcher, Config baseConfig,
                         Configuration externalConfig, IMetadataInjector[] injector, IPreProcessor[] preProcessors, IPostProcessor[] postProcessors, IndexScheduler indexScheduler, ElasticConfig elasticConfig, IBusIndexManager iBusIndexManager) {
        super(60000, new PlugDescriptionFieldFilters(), injector, preProcessors, postProcessors);
        _indexSearcher = indexSearcher;
        this.indexScheduler = indexScheduler;
        this.baseConfig = baseConfig;

        try {
            baseConfig.initialize();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (externalConfig != null) {
            externalConfig.initialize();
        } else {
            log.info("No external configuration found.");
        }
        this.elasticConfig = elasticConfig;
        this.iBusIndexManager = iBusIndexManager;
    }

    /*
     * (non-Javadoc)
     *
     * @see de.ingrid.utils.ISearcher#search(de.ingrid.utils.query.IngridQuery, int, int)
     */
    @Override
    public final IngridHits search(final IngridQuery query, final int start, final int length) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("Incoming query: " + query.toString() + ", start=" + start + ", length=" + length);
        }
        preProcess(query);

        // request iBus directly to get search results from within this iPlug
        // adapt query to only get results coming from this iPlug and activated in iBus
        // But when not connected to an iBus then use direct connection to Elasticsearch
        if (elasticConfig.esCommunicationThroughIBus) {

            ClauseQuery cq = new ClauseQuery(true, false);
            cq.addField(new FieldQuery(true, false, "iPlugId", baseConfig.communicationProxyUrl));
            query.addClause(cq);
            return this.iBusIndexManager.search(query, start, length);
        }

        return _indexSearcher.search(query, start, length);
    }

    /*
     * (non-Javadoc)
     *
     * @see de.ingrid.utils.IRecordLoader#getRecord(de.ingrid.utils.IngridHit)
     */
    @Override
    public Record getRecord(IngridHit hit) {
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
            return this.iBusIndexManager.getDetail(hit, query, fields);
        }

        return _indexSearcher.getDetail(hit, query, fields);
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
            return this.iBusIndexManager.getDetails(hits, query, fields);
        }

        return _indexSearcher.getDetails(hits, query, fields);
    }

    public static void main(String[] args) {
        System.setProperty(Constants.SKIP_JARS_PROPERTY, "tools.jar,derby*.jar,unit-api*.jar,geo*.jar,si*.jar,jai*.jar,commons*.jar,Geo*.jar,jgrid*.jar,uo*.jar,system*.jar,gt*.jar,jackson*.jar,org*.jar,ej*.jar,jt*.jar,net*.jar,serial*.jar,xml*.jar,xerc*.jar,mchan*.jar");
        SpringApplication.run(BlpSearchPlug.class, args);
    }

    @Override
    public IngridDocument call(IngridCall info) {
        IngridDocument doc = null;

        if ("index".equals(info.getMethod())) {
            indexScheduler.triggerManually();
            doc = new IngridDocument();
            doc.put("success", true);
        }
        log.warn("The following method is not supported: " + info.getMethod());

        return doc;
    }

}
