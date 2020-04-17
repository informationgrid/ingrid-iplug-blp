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
/**
 * 
 */
package de.ingrid.iplug.dsc.index.producer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import de.ingrid.utils.statusprovider.StatusProvider;
import de.ingrid.utils.statusprovider.StatusProvider.Classification;
import de.ingrid.utils.statusprovider.StatusProviderService;
import de.ingrid.iplug.dsc.om.BLPSourceRecord;
import de.ingrid.iplug.dsc.om.SourceRecord;
import de.ingrid.iplug.dsc.utils.UVPDataImporter;
import de.ingrid.iplug.dsc.utils.UVPDataImporter.BlpModel;
import de.ingrid.utils.IConfigurable;
import de.ingrid.utils.PlugDescription;

/**
 * Takes care of selecting all source record Ids from a database. The SQL
 * statement is configurable via Spring.
 * 
 * The database connection is configured via the PlugDescription.
 * 
 * 
 * @author joachim@wemove.com
 * 
 */
// Bean created depending on SpringConfiguration
// @Service
public class BLPRecordSetProducer implements IRecordSetProducer, IConfigurable {

    private StatusProvider statusProvider;

    Iterator<BlpModel> recordIterator = null;
    private int numRecords;
    private File excelFile = null;
    private File workingDir;
    private String organisation;

    final private static Log log = LogFactory.getLog( BLPRecordSetProducer.class );

    @Autowired
    public BLPRecordSetProducer(StatusProviderService statusProviderService) {
        this.statusProvider = statusProviderService.getDefaultStatusProvider();
        log.info( "BLPRecordSetProducer started." );
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.ingrid.iplug.dsc.index.IRecordProducer#hasNext()
     */
    @Override
    public boolean hasNext() throws Exception {
        if (recordIterator == null) {
            createBLPRecordsFromExcelFile();
        }
        if (recordIterator.hasNext()) {
            return true;
        } else {
            reset();
            return false;
        }
    }

    /**
     * Closes the connection to the database and resets the iterator for the records.
     * After a reset, the hasNext() function will start from the beginning again.
     */
    @Override
    public void reset() {
        recordIterator = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.ingrid.iplug.dsc.index.IRecordProducer#next()
     */
    @Override
    public SourceRecord next() {
        return new BLPSourceRecord( recordIterator.next(), organisation );
    }

    private void createBLPRecordsFromExcelFile() throws Exception {
        try {
            UVPDataImporter importer = new UVPDataImporter( getExcelFile() );
            List<BlpModel> blpRecords = importer.getValidModels();
            if (log.isDebugEnabled()) {
                log.debug( "Found records: " + blpRecords.size() );
            }

            recordIterator = blpRecords.listIterator();
            numRecords = blpRecords.size();
        } catch (Exception e) {
            log.error( "Error creating records.", e );
            statusProvider.addState( "ERROR", "Excel file was not found. Did you upload it?", Classification.ERROR );
            throw e;
        }
    }

    @Override
    public int getDocCount() {
        return numRecords;
    }

    public File getExcelFile() throws FileNotFoundException {
        if (excelFile == null) {
            File dataDir = new File( workingDir, "data" );
            for (File f : dataDir.listFiles()) {
                String name = f.getName().toLowerCase();
                if (name.endsWith( ".xls" ) || name.endsWith( ".xlsx" )) {
                    return f;
                }
            }
            throw new FileNotFoundException( String.format( "No Excel file found in '%s'", dataDir.toString() ) );
        }
        return excelFile;
    }

    public void setExcelFile(File excelFile) {
        this.excelFile = excelFile;
    }

    public void setExcelFilename(String excelFilename) {
        setExcelFile( new File( excelFilename ) );
    }

    @Override
    public void configure(PlugDescription plugDescription) {
        this.workingDir = plugDescription.getWorkinDirectory();
        this.organisation = plugDescription.getOrganisationPartnerAbbr();
    }

}
