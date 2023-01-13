/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2023 wemove digital solutions GmbH
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
/*
 * Copyright (c) 1997-2006 by wemove GmbH
 */
package de.ingrid.iplug.dsc.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import de.ingrid.utils.statusprovider.StatusProvider;
import de.ingrid.utils.statusprovider.StatusProvider.Classification;

/**
 * UVP data importer. Imports data from an excel file directly into the url
 * database.
 *
 * All urls from instance are deleted.
 *
 * The limit urls are set to the domain of the start url. Make sure the depth of
 * the crawl is set to 1 and no outlinks should be extracted.
 *
 * The excel table must have a specific layout. The first row must contain the
 * column names.
 *
 * <ul>
 * <li>NAME (alternate: STADT/GEMEINDE): BLP name that appears on map popup as
 * title.</li>
 * <li>LAT: LAT of map marker coordinate.</li>
 * <li>LON: LON of map marker coordinate.</li>
 * <li>URL_VERFAHREN_OFFEN: Url to BLPs (Bauleitpläne) in progress.</li>
 * <li>URL_VERFAHREN_ABGESCHLOSSEN: Url to finished BLPs (Bauleitpläne).</li>
 * <li>URL_VERFAHREN_FNP_LAUFEND: Url to FNPs (Flächennutzungspläne) in
 * progress.</li>
 * <li>URL_VERFAHREN_FNP_ABGESCHLOSSEN: Url to finished FNPs
 * (Flächennutzungspläne).</li>
 * <li>URL_VERFAHREN_BEBAUUNGSPLAN_LAUFEND: Url to BPs (Bebauungspläne) in
 * progress.</li>
 * <li>URL_VERFAHREN_BEBAUUNGSPLAN_ABGESCHLOSSEN: Url to finished BPs
 * (Bebauungspläne).</li>
 * <li>MITGLIEDSGEMEINDEN: BLP description that appears on map popup.</li>
 * </ul>
 *
 * The column names are treated as prefixes. More descriptive column names could
 * be used (i.e. URL_VERFAHREN_BEBAUUUNGSPLAN_LAUFEND/ SATZUNGEN NACH § 34 Abs.
 * 4 und § 35 Abs. 6 BauGB).
 *
 * "Flächennutzungspläne" and "Bebauungspläne" are an alternative to
 * "Bauleitpläne".
 *
 * Columns can be mixed. The excel file can contain other columns, as long as
 * the specified columns exist.
 *
 * @author joachim@wemove.com
 */
public class UVPDataImporter implements Runnable {

    private StatusProvider statusProvider;

    private File excelFile;
    private InputStream excelFileInputStream;

    private String importReport = "";

    public UVPDataImporter(File excelFile) {
        this.excelFile = excelFile;
    }

    public UVPDataImporter() {

    }

    /**
     * Scan Excel file and gather all infos. Requires a specific excel table
     * layout
     *
     * @return
     * @throws IOException
     */
    public List<BlpModel> readData() throws IOException {
        return readData( excelFile );
    }

    /**
     * Scan Excel file and gather all infos. Requires a specific excel table
     * layout
     *
     * @param excelFile
     * @return
     * @throws IOException
     */
    public List<BlpModel> readData(File excelFile) throws IOException {
        FileInputStream inputStream = new FileInputStream( excelFile );
        return readData( inputStream, excelFile.getName() );
    }

    /**
     * Scan Excel file and gather all infos. Requires a specific excel table
     * layout
     *
     * @param excelFile
     * @return
     * @throws IOException
     */
    public List<BlpModel> readData(InputStream inputStream, String excelFile) throws IOException {
        List<BlpModel> blpModels = new ArrayList<BlpModel>();

        Workbook workbook = null;

        try {

            if (excelFile.endsWith( "xlsx" )) {
                workbook = new XSSFWorkbook( inputStream );
            } else if (excelFile.endsWith( "xls" )) {
                workbook = new HSSFWorkbook( inputStream );
            } else {
                throw new IllegalArgumentException( "The specified file is not an Excel file" );
            }
            Sheet sheet = workbook.getSheetAt( 0 );
            Iterator<Row> it = sheet.iterator();
            boolean gotHeader = false;
            Map<Integer, String> columnNames = new HashMap<Integer, String>();
            if (it.hasNext()) {
                // iterate over all rows
                while (it.hasNext()) {
                    Iterator<Cell> ci = it.next().cellIterator();
                    // handle header
                    if (!gotHeader) {
                        // iterate over all columns
                        while (ci.hasNext()) {
                            Cell cell = ci.next();
                            int columnIndex = cell.getColumnIndex();
                            String columnName = cell.getStringCellValue();
                            if (columnName == null || columnName.length() == 0) {
                                throw new IllegalArgumentException( "No column name specified for column " + columnIndex + "." );
                            }
                            columnNames.put( columnIndex, columnName );
                        }
                        validateColumnNames( columnNames );
                        gotHeader = true;
                    } else {

                        BlpModel bm = new UVPDataImporter().new BlpModel();
                        while (ci.hasNext()) {
                            Cell cell = ci.next();
                            int columnIndex = cell.getColumnIndex();

                            if (columnIndex < columnNames.size()) {
                                String colName = columnNames.get( columnIndex );

                                if (colName.equals( "NAME" )) {
                                    bm.setName( cell.getStringCellValue() );
                                } else if (colName.equals( "STADT/GEMEINDE" )) {
                                    bm.setName( cell.getStringCellValue() );
                                } else if (colName.equals( "LAT" )) {
                                    try {
                                        bm.lat = cell.getNumericCellValue();
                                    } catch (Exception e) {
                                        try {
                                            bm.lat = Double.valueOf( cell.getStringCellValue() );
                                        } catch (Exception e1) {
                                            // ignore
                                        }
                                    }
                                } else if (colName.equals( "LON" )) {
                                    try {
                                        bm.lon = cell.getNumericCellValue();
                                    } catch (Exception e) {
                                        try {
                                            bm.lon = Double.valueOf( cell.getStringCellValue() );
                                        } catch (Exception e1) {
                                            // ignore
                                        }
                                    }
                                } else if (colName.startsWith( "URL_VERFAHREN_OFFEN" )) {
                                    bm.urlBlpInProgress = cell.getStringCellValue();
                                } else if (colName.startsWith( "URL_VERFAHREN_ABGESCHLOSSEN" )) {
                                    bm.urlBlpFinished = cell.getStringCellValue();
                                } else if (colName.startsWith( "URL_VERFAHREN_FNP_LAUFEND" )) {
                                    bm.urlFnpInProgress = cell.getStringCellValue();
                                } else if (colName.startsWith( "URL_VERFAHREN_FNP_ABGESCHLOSSEN" )) {
                                    bm.urlFnpFinished = cell.getStringCellValue();
                                } else if (colName.startsWith( "URL_VERFAHREN_BEBAUUNGSPLAN_LAUFEND" )) {
                                    bm.urlBpInProgress = cell.getStringCellValue();
                                } else if (colName.startsWith( "URL_VERFAHREN_BEBAUUNGSPLAN_ABGESCHLOSSEN" )) {
                                    bm.urlBpFinished = cell.getStringCellValue();
                                } else if (colName.startsWith( "MITGLIEDSGEMEINDEN" )) {
                                    bm.descr = cell.getStringCellValue();
                                }
                            }
                        }

                        System.out.print( "." );

                        if (bm.getName() != null && bm.getName().length() > 0) {
                            validate( bm );
                            blpModels.add( bm );
                        }
                    }
                }
            }
        } finally {
            if (workbook != null) {
                workbook.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }

        return blpModels;

    }

    /**
     * Validates a BLP model entry.
     *
     * @param bm
     * @return True if BLP model is valid. False if not.
     */
    private static boolean validate(BlpModel bm) {
        boolean isValid = true;

        if (bm.getName() == null || bm.getName().length() <= 2) {
            isValid = false;
            bm.errors.add( new UVPDataImporter().new StatusEntry( "Name is null or too short.", "IGNORED" ) );
        }

        if (bm.lat == null || bm.lat < 47 || bm.lat > 56) {
            isValid = false;
            bm.errors.add( new UVPDataImporter().new StatusEntry( "Lat not between 47 and 56.", "IGNORED" ) );
        }

        if (bm.lon == null || bm.lon < 5 || bm.lon > 15) {
            isValid = false;
            bm.errors.add( new UVPDataImporter().new StatusEntry( "Lon not between 5 and 15.", "IGNORED" ) );
        }

        List<String> blpUrls = Arrays.asList( new String[] { bm.urlBlpInProgress, bm.urlBlpFinished, bm.urlFnpInProgress, bm.urlFnpFinished, bm.urlBpInProgress, bm.urlBpFinished } );

        /*
         *
         * for (String url : blpUrls) { if (url != null && url.length() > 0) {
         * try { URLConnection conn = new URL( url ).openConnection();
         * TrustModifier.relaxHostChecking( (HttpURLConnection) conn );
         * conn.connect(); } catch (Exception e) { isValid = false; bm.info.add(
         * new UVPDataImporter().new StatusEntry( "Problems accessing '" + url,
         * "URL_IGNORED" ) ); } } }
         */
        // check if any URL is set.
        boolean hasUrlSet = blpUrls.stream().filter( entry -> (entry != null && entry.trim().length() > 0) ).count() > 0;
        if (!hasUrlSet) {
            isValid = false;
            bm.errors.add( new UVPDataImporter().new StatusEntry( "No URL set.", "IGNORED" ) );
        }

        bm.hasMarker = isValid;

        return isValid;
    }

    /**
     * Checks all URLs in a BLP model entry.
     *
     * @param bm
     * @return True if at least one URL of the BLP model is reachable. False if not.
     */
    private static boolean checkUrls(BlpModel bm) {
        int reachableUrls = 0;
        List<String> blpUrls = Arrays.asList( new String[] { bm.urlBlpInProgress, bm.urlBlpFinished, bm.urlFnpInProgress, bm.urlFnpFinished, bm.urlBpInProgress, bm.urlBpFinished } );

        for (String url : blpUrls) {
            try {
                getActualUrl( url, bm );
                reachableUrls++;
            } catch (Exception e) {}
        }
        return reachableUrls > 0;
    }

    /**
     * Validates the excel header.
     *
     * @param columnNames
     * @throws IllegalArgumentException
     */
    private static void validateColumnNames(Map<Integer, String> columnNames) throws IllegalArgumentException {

        if (!columnNames.containsValue( "NAME" ) && !columnNames.containsValue( "STADT/GEMEINDE" )) {
            throw new IllegalArgumentException( "Required elective column header \"NAME\" or \"STADT/GEMEINDE\" not specified in excel file." );
        }
        if (!columnNames.containsValue( "LON" )) {
            throw new IllegalArgumentException( "Required column header \"LON\" not specified in excel file." );
        }
        if (!columnNames.containsValue( "LAT" )) {
            throw new IllegalArgumentException( "Required column header \"LAT\" not specified in excel file." );
        }

    }

    public InputStream getExcelFileInputStream() {
        return excelFileInputStream;
    }

    public void setExcelFileInputStream(InputStream excelFileInputStream) {
        this.excelFileInputStream = excelFileInputStream;
    }

    public class StatusEntry {
        String message;
        String type;

        StatusEntry(String message, String type) {
            super();
            this.message = message;
            this.type = type;
        }

    }

    public class BlpModel {

        public String name;
        public Double lat;
        public Double lon;
        public String urlBlpInProgress;
        public String urlBlpFinished;
        public String urlFnpInProgress;
        public String urlFnpFinished;
        public String urlBpInProgress;
        public String urlBpFinished;
        public String descr;
        public boolean hasMarker = false;
        public List<StatusEntry> errors = new ArrayList<StatusEntry>();

        @Override
        public String toString() {
            return "[name: " + getName() + "; lat:" + lat + "; lon:" + lon + "; urlBlpInProgress:" + urlBlpInProgress + "; urlBlpFinished:" + urlBlpFinished + "; urlFnpInProgress:" + urlFnpInProgress
                    + "; urlFnpFinished:" + urlFnpFinished + "; urlBpInProgress:" + urlBpInProgress + "; urlBpFinished:" + urlBpFinished + "; descr:" + descr + "]";
        }

        public String getName() {
            return name;
        }

        private void setName(String name) {
            this.name = name;
        }

        public boolean isIgnored() {
            return errors.stream().anyMatch( entry -> entry.type.equals( "IGNORED" ) );
        }

    }

    public String analyzeExcelFile() throws IOException {
        logSp( "readFile", "Datei wird eingelesen ..." );
        String errors = "";
        List<BlpModel> models;
        try {
            models = readData();
        } catch (Exception e) {
            logSp( "importERROR", "Error: " + e.getMessage(), Classification.ERROR);
            return importReport;
        }
        logSp( "readFile", "Datei wird eingelesen ... fertig\n" );
        logSp( "totalEntries", String.format( "Analysiere URLs von %d Einträgen...", models.size() ) );
        int ignoredModels = 0;
        int modelsWithErrors = 0;
        int total = models.size();
        for (int i = 0; i < models.size(); i++) {
            BlpModel model = models.get( i );
            checkUrls( model );

            if (model.isIgnored())
                ignoredModels++;
            if (!model.errors.isEmpty()) {
                modelsWithErrors++;
                errors += model.getName() + ": \n";
                for (StatusEntry se : model.errors) {
                    errors += String.format( "%s %s \n", se.type, se.message );
                }
                errors += "\n";
            }
            logSp( "importProgress", String.format( "%d von %d Einträge analysiert.", i + 1, models.size() ) );
        }
        if (ignoredModels < 1) {
            importReport += String.format( "Excel Datei hochgeladen. Es wurden alle %d Einträge erfolgreich validiert.", total );
            if (modelsWithErrors > 0) {
                importReport += String.format( " Bei %d Einträgen kam es zu Warnungen: \n\n %s", modelsWithErrors, errors );
            }
        } else {
            importReport += String.format( "Excel Datei hochgeladen. Es wurden %d von %d Einträgen erfolgreich validiert. %d Einträge werden ignoriert. Bei %d Einträgen kam es zu Warnungen: \n\n %s",
                    total - ignoredModels, total, ignoredModels, modelsWithErrors, errors );
        }
        logSp( "finished", "Fertig" );
        logSp( "emptyInfo", " " );
        logSp( "summary", importReport.replaceAll( "\n", "<br>" ) );
        return importReport;
    }

    public List<BlpModel> getValidModels() throws IOException {
        return getValidModels( this.getExcelFile() );
    }

    public List<BlpModel> getValidModels(File excelFile) throws IOException {
        List<BlpModel> models = readData( excelFile );
        List<BlpModel> validModels = new ArrayList<>();
        for (BlpModel model : models) {
            if (!model.isIgnored()) {
                validModels.add( model );
            }
        }
        return validModels;
    }

    public File getExcelFile() {
        return excelFile;
    }

    public void setExcelFile(File excelFile) {
        this.excelFile = excelFile;
    }

    public static String getActualUrl(String url, BlpModel bm) throws Exception {

        int termination = 10;
        while (termination-- > 0) {
            String actualUrl = null;
            actualUrl = getRedirect( url, bm );
            if (actualUrl.equals( url )) {
                return url;
            }
            bm.errors.add( new UVPDataImporter().new StatusEntry( "Redirect detected: '" + url + "' -> '" + actualUrl + "'.", "REDIRECTS" ) );
            if (actualUrl.startsWith( "/" )) {
                // redirect to local absolute url
                url = getDomain( url ).concat( actualUrl );
            } else if (actualUrl.indexOf( "://" ) < 0 || actualUrl.indexOf( "://" ) > 10) {
                // redirect to local relative url
                url = getParent( url ).concat( "/" ).concat( actualUrl );
            } else if (actualUrl.startsWith( "../" )) {
                // redirect to local parent directory based url
                while (actualUrl.startsWith( "../" )) {
                    url = stripLastPath( url );
                    actualUrl = actualUrl.substring( 3 );
                }
                url = url.concat( actualUrl );
            } else {
                url = actualUrl;
            }
        }
        bm.errors.add( new UVPDataImporter().new StatusEntry( "Too many redirects.", "WARNING" ) );
        throw new Exception( "Too many Redirects: 10" );
    }

    private static String getRedirect(String urlstring, BlpModel bm) throws Exception {
        HttpURLConnection con = null;

        int responseCode = -1;
        try {
            con = (HttpURLConnection) (new URL( urlstring ).openConnection());
            TrustModifier.relaxHostChecking( con );
            con.setRequestProperty( "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:58.0) Gecko/20100101 Firefox/58.0" );
            con.setInstanceFollowRedirects( false );
            con.setRequestMethod( "HEAD" );
            con.setConnectTimeout( 5000 );
            con.setReadTimeout( 5000 );
            con.connect();
            responseCode = con.getResponseCode();
            if (300 <= responseCode && responseCode <= 308) {
                Map<String, List<String>> headers = con.getHeaderFields();
                for (String header : headers.keySet()) {
                    if (header != null && header.equalsIgnoreCase( "location" )) {
                        return con.getHeaderField( header );
                    }
                }
            } else {
                String metaURL = getMetaRedirectURL( con );
                if (metaURL != null) {
                    if (!metaURL.startsWith( "http" )) {
                        URL u = new URL( new URL( urlstring ), metaURL );
                        return u.toString();
                    }
                    return metaURL;
                }
            }

        } catch (Throwable e) {
            if (responseCode == -1) {
                throw new Exception( "Problems accessing '" + urlstring + " (HTTP_ERROR: " + responseCode + ") (" + e + ")" );
            } else {
                bm.errors.add( new UVPDataImporter().new StatusEntry( "Problems accessing '" + urlstring + " (HTTP_ERROR: " + responseCode + ")", "HTTP" ) );
                throw new Exception( "Problems accessing '" + urlstring + " (HTTP_ERROR: " + responseCode + ")" );

            }
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
        return urlstring;

    }

    private static String getMetaRedirectURL(HttpURLConnection con) throws IOException {
        StringBuilder sb = new StringBuilder();

        try (BufferedReader reader = new BufferedReader( new InputStreamReader( con.getInputStream() ) )) {

            String content = null;
            while ((content = reader.readLine()) != null) {
                sb.append( content );
                if (content.toLowerCase().contains( "</head" ) || content.matches( "(?i)<meta.*?http-equiv=.*?refresh.*?>" )) {
                    break;
                }
            }
            String html = sb.toString();
            html = html.replace( "\n", "" );
            if (html.length() == 0)
                return null;
            int indexHttpEquiv = html.toLowerCase().indexOf( "http-equiv=\"refresh\"" );
            if (indexHttpEquiv < 0) {
                return null;
            }
            html = html.substring( indexHttpEquiv );
            int indexContent = html.toLowerCase().indexOf( "content=" );
            if (indexContent < 0) {
                return null;
            }
            html = html.substring( indexContent );
            int indexURLStart = html.toLowerCase().indexOf( "url=" );
            if (indexURLStart < 0) {
                return null;
            }
            html = html.substring( indexURLStart + 4 );
            int indexURLEnd = html.toLowerCase().indexOf( "\"" );
            if (indexURLEnd < 0) {
                return null;
            }
            return html.substring( 0, indexURLEnd );
        }

    }

    /**
     * Get the parent of the given URL. If the URL contains only of an domain,
     * return the domain.
     *
     * <p>
     * http://test.domain.de/ -> http://test.domain.de<br>
     * http://test.domain.de -> http://test.domain.de<br>
     * http://test.domain.de/a -> http://test.domain.de<br>
     * http://test.domain.de/a/ -> http://test.domain.de/a<br>
     * http://test.domain.de/a/b.de -> http://test.domain.de/a<br>
     * </p>
     *
     * @param urlStr
     * @return
     * @throws MalformedURLException
     */
    public static String getParent(String urlStr) throws MalformedURLException {
        String d = getDomain( urlStr );
        if (urlStr.equals( d ) || urlStr.equals( d.concat( "/" ) )) {
            return d;
        } else {
            return FilenameUtils.getPath( urlStr ).substring( 0, FilenameUtils.getPath( urlStr ).length() - 1 );
        }
    }

    /**
     * Get Limit URL from URL.
     *
     * @param urlStr
     * @return
     * @throws MalformedURLException
     */
    public static String getDomain(String urlStr) throws MalformedURLException {
        URL url = new URL( urlStr );
        String host = url.getHost();
        return urlStr.substring( 0, urlStr.indexOf( host ) + host.length() );
    }

    static String stripLastPath(String urlString) throws MalformedURLException {
        String domain = getDomain( urlString );
        String part = urlString.substring( 0, urlString.lastIndexOf( "/" ) );
        if (part.equals( domain )) {
            return domain.concat( "/" );
        } else {
            return urlString.substring( 0, urlString.lastIndexOf( "/", urlString.lastIndexOf( "/" ) - 1 ) ).concat( "/" );
        }
    }

    @Override
    public void run() {
        try {
            analyzeExcelFile();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public String getImportReport() {
        return this.importReport;
    }

    public StatusProvider getStatusProvider() {
        return statusProvider;
    }

    public void setStatusProvider(StatusProvider statusProvider) {
        this.statusProvider = statusProvider;
    }

    private void logSp(String key, String message) {
        logSp( key, message, Classification.INFO );
    }

    private void logSp(String key, String message, Classification classification) {
        if (this.statusProvider != null)
            this.statusProvider.addState( key, message, classification );
    }

}
