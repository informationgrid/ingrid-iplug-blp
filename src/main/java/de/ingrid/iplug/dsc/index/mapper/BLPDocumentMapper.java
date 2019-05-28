/*
 * **************************************************-
 * InGrid-iPlug DSC
 * ==================================================
 * Copyright (C) 2014 - 2019 wemove digital solutions GmbH
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
/**
 * 
 */
package de.ingrid.iplug.dsc.index.mapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.ingrid.iplug.dsc.om.BLPSourceRecord;
import de.ingrid.iplug.dsc.om.SourceRecord;
import de.ingrid.iplug.dsc.utils.UVPDataImporter.BlpModel;
import de.ingrid.utils.ElasticDocument;

public class BLPDocumentMapper implements IRecordMapper {

    final private static Log log = LogFactory.getLog( BLPDocumentMapper.class );

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.ingrid.iplug.dsc.index.IRecord2DocumentMapper#map(de.ingrid.iplug.
     * dsc.index.IRecord)
     */
    @Override
    public void map(SourceRecord record, ElasticDocument doc) {
        if (!(record instanceof BLPSourceRecord)) {
            throw new IllegalArgumentException( "Record is no BLPSourceRecord!" );
        }

        BlpModel model = (BlpModel) record.get( BLPSourceRecord.BLP_MODEL );

        doc.put( "name", model.getName());

    }
}
