/*
 * **************************************************-
 * InGrid-iPlug DSC
 * ==================================================
 * Copyright (C) 2014 - 2020 wemove digital solutions GmbH
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
package de.ingrid.iplug.dsc.index;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import de.ingrid.admin.JettyStarter;
import de.ingrid.utils.statusprovider.StatusProviderService;
import de.ingrid.iplug.dsc.index.mapper.BLPDocumentMapper;
import de.ingrid.iplug.dsc.index.mapper.IRecordMapper;
import de.ingrid.iplug.dsc.index.producer.BLPRecordSetProducer;

public class SimpleBLPDocumentProducerTest {
    
    
    public SimpleBLPDocumentProducerTest() throws Exception {
        new JettyStarter(false);
    }

    @Test
    public void testDscDocumentProducer() throws Exception {

        BLPRecordSetProducer p = new BLPRecordSetProducer(new StatusProviderService());
        p.setExcelFilename( "src/test/resources/blp-urls-test.xlsx" );

        BLPDocumentMapper m = new BLPDocumentMapper();

        List<IRecordMapper> mList = new ArrayList<IRecordMapper>();
        mList.add( m );

        DscDocumentProducer dp = new DscDocumentProducer();
        dp.setRecordSetProducer( p );
        dp.setRecordMapperList( mList );

        if (dp.hasNext()) {
            while (dp.hasNext()) {
                Map<String, Object> doc = dp.next();
                assertNotNull( doc );
                Collection<String> keys = Arrays.asList( "blp_name" );
                assertTrue( doc.keySet().containsAll( keys ) );
            }
        } else {
            fail( "No document produced" );
        }
    }



}
