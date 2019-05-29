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
package de.ingrid.iplug.dsc.om;

import java.util.UUID;

import de.ingrid.iplug.dsc.utils.UVPDataImporter.BlpModel;
import de.ingrid.utils.ElasticDocument;

/**
 * Represents a record set from a blp excel file.
 * 
 * @author benjamin.roehrl@wemove.com
 * 
 */
public class BLPSourceRecord extends SourceRecord {

    private static final long serialVersionUID = 2466131851010594486L;

//    public static final String ROW = "row";
//
//    public static final String COLUMN = "column";
    
    public static final String INDEX_DOCUMENT = "idxDoc";
    
    public static final String BLP_MODEL = "blpModel";
    
    public static final String ORGANISATION = "organisation";

    
    /**
     * Creates a BLPRecord. It holds the excel blpModel aswell
     * as the organisation
     * 
     * @param blpModel
     * @param organisation
     */
    public BLPSourceRecord(BlpModel blpModel, String organisation) {
        super(UUID.randomUUID().toString());
        this.put( BLP_MODEL, blpModel );
        this.put( ORGANISATION, organisation );
    }
    
    /**
     * Creates a BLPRecord. It holds the excel blpModel aswell
     * as the organisation and an Elastic Index Document
     *  for further usage.
     * 
     * @param blpModel
     * @param idxDoc
     */
    public BLPSourceRecord(BlpModel blpModel, String organisation, ElasticDocument idxDoc) {
        super(UUID.randomUUID().toString());
        this.put( BLP_MODEL, blpModel );
        this.put( ORGANISATION, organisation );
        this.put( INDEX_DOCUMENT, idxDoc );
    }

}
