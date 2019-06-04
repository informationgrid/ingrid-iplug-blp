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

import de.ingrid.iplug.dsc.om.BLPSourceRecord;
import de.ingrid.iplug.dsc.om.SourceRecord;
import de.ingrid.iplug.dsc.utils.UVPDataImporter.BlpModel;
import de.ingrid.utils.ElasticDocument;

public class BLPDocumentMapper implements IRecordMapper {

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

        doc.put( "title", "Bauleitplanung: " + model.name );
        // doc.put( "summary", model.descr );
        doc.put( "partner", (String) record.get( BLPSourceRecord.ORGANISATION ) );

        doc.put( "blp_name", model.name );
        doc.put( "blp_description", model.descr );
        doc.put( "x1", model.lon );
        doc.put( "x2", model.lon );
        doc.put( "y1", model.lat );
        doc.put( "y2", model.lat );
        
        String additionalHtml = "";
        if (model.descr != null) {
            additionalHtml += String.format( "<p>Mitgliedsgemeinden: %s</p>", model.descr );
        }
        
        
        additionalHtml += "<p>Nutzen Sie die folgenden Links um zu den Bauleitplanungs-Seiten zu gelangen:</p>";
        

        if (model.urlBlpInProgress != null) {
            doc.put( "blp_url_in_progress", model.urlBlpInProgress );
            additionalHtml += String.format( "<p><a href=\"%s\">%s</a></p>", model.urlBlpInProgress, "Bauleitpläne im Beteiligungsverfahren" );
        }
        if (model.urlBlpFinished != null) {
            doc.put( "blp_url_finished", model.urlBlpFinished );
            additionalHtml += String.format( "<p><a href=\"%s\">%s</a></p>", model.urlBlpFinished, "Wirksame/rechtskräftige Bauleitpläne" );
        }
        if (model.urlFnpInProgress != null) {
            doc.put( "fnp_url_in_progress", model.urlFnpInProgress );
            additionalHtml += String.format( "<p><a href=\"%s\">%s</a></p>", model.urlFnpInProgress, "Flächennutzungspläne im Beteiligungsverfahren" );
        }
        if (model.urlFnpFinished != null) {
            doc.put( "fnp_url_finished", model.urlFnpFinished );
            additionalHtml += String.format( "<p><a href=\"%s\">%s</a></p>", model.urlFnpFinished, "Wirksame/rechtskräftige Flächennutzungspläne" );
        }
        if (model.urlBpInProgress != null) {
            doc.put( "bp_url_in_progress", model.urlBpInProgress );
            additionalHtml += String.format( "<p><a href=\"%s\">%s</a></p>", model.urlBpInProgress, "Bebauungspläne im Beteiligungsverfahren" );
        }
        if (model.urlBpFinished != null) {
            doc.put( "bp_url_finished", model.urlBpFinished );
            additionalHtml += String.format( "<p><a href=\"%s\">%s</a></p>", model.urlBpFinished, "Wirksame/rechtskräftige Bebauungspläne" );
        }

        doc.put( "additional_html_1", additionalHtml );

        // constants
        doc.put( "datatype", "www" );
        doc.put( "blp_marker", "blp_marker" );
        doc.put( "procedure", "dev_plan" );
        doc.put( "lang", "de" );

    }
}
