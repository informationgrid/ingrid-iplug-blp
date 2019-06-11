/*
 * **************************************************-
 * ingrid-iplug-se-iplug
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
package de.ingrid.iplug.dsc.webapp.controller;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartFile;

import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.admin.controller.AbstractController;
import de.ingrid.iplug.dsc.UploadBean;
import de.ingrid.iplug.dsc.utils.UVPDataImporter;

/**
 * Control the BLP Import page.
 *
 * @author joachim@wemove.com
 *
 */
@Controller
@SessionAttributes("plugDescription")
public class BlpImportController extends AbstractController {
    
    String resultLog = "Noch kein Upload gestartet.";

    @RequestMapping(value = { "/iplug-pages/welcome.html", "/iplug-pages/excelUpload.html" }, method = RequestMethod.GET)
    public String showBlpImport(@ModelAttribute("uploadBean") final UploadBean uploadBean, final ModelMap modelMap, HttpServletRequest request, HttpServletResponse response,
            @ModelAttribute("plugDescription") final PlugdescriptionCommandObject commandObject) throws Exception {
        modelMap.addAttribute( "resultLog", getResultLog());
        return AdminViews.EXCEL_UPLOAD;
    }

    /**
     * Upload excel file.
     *
     * @param uploadBean
     * @param model
     * @return
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.POST)
    public String upload(@ModelAttribute("uploadBean") final UploadBean uploadBean, 
            final Model model, 
            @ModelAttribute("plugDescription") final PlugdescriptionCommandObject commandObject)
            throws IOException {

        MultipartFile uploadedFile = uploadBean.getFile();
        if (uploadBean.getFile().isEmpty()) {
            return AdminViews.EXCEL_UPLOAD;
        }

        File wd = commandObject.getWorkinDirectory();
        File dataDir = new File( wd, "data" );
        dataDir.mkdirs();
        deleteAllFilesInDirectory( dataDir );
        createWarningFile( dataDir );

        File xlsFile = new File( dataDir, uploadedFile.getOriginalFilename() );
        Files.write( xlsFile.toPath(), uploadedFile.getBytes(), StandardOpenOption.CREATE );

        UVPDataImporter importer = new UVPDataImporter( xlsFile );
        resultLog = importer.analyzeExcelFile();
        System.out.println( resultLog );
        
        return redirect("/iplug-pages/excelUpload.html");
    }
    
    public String getResultLog() throws UnsupportedEncodingException {
        return resultLog.replaceAll( "\n", "<br>");
    }

    private void deleteAllFilesInDirectory(File directory) {
        for (File f : directory.listFiles()) {
            f.delete();
        }
    }

    private void createWarningFile(File directory) throws IOException {
        Files.write( new File( directory, "READ_ME" ).toPath(), "Warning! All files in here will be deleted when a new Excelfile is uploaded!!!".getBytes(), StandardOpenOption.CREATE );
    }

}
