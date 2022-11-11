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
package de.ingrid.iplug.dsc.index;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.*;

import de.ingrid.admin.Config;
import de.ingrid.iplug.dsc.index.scraper.BlpScraper;
import org.junit.jupiter.api.Test;

import de.ingrid.utils.statusprovider.StatusProviderService;
import de.ingrid.iplug.dsc.index.mapper.BLPDocumentMapper;
import de.ingrid.iplug.dsc.index.mapper.IRecordMapper;
import de.ingrid.iplug.dsc.index.producer.BLPRecordSetProducer;

public class SimpleBLPDocumentProducerTest {

    @Test
    void testDscDocumentProducer() {

        BLPRecordSetProducer p = new BLPRecordSetProducer(new StatusProviderService());
        p.setExcelFilename("src/test/resources/blp-urls-test.xlsx");

        BLPDocumentMapper m = new BLPDocumentMapper(new Config(), new BlpScraper());

        List<IRecordMapper> mList = new ArrayList<>();
        mList.add(m);

        DscDocumentProducer dp = new DscDocumentProducer();
        dp.setRecordSetProducer(p);
        dp.setRecordMapperList(mList);

        if (dp.hasNext()) {
            while (dp.hasNext()) {
                Map<String, Object> doc = dp.next();
                assertNotNull(doc);
                Collection<String> keys = Collections.singletonList("blp_name");
                assertTrue(doc.keySet().containsAll(keys));
            }
        } else {
            fail("No document produced");
        }
    }



}
