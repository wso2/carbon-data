package org.wso2.carbon.dataservices.sql.driver.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This is the helper class when we need to write workbook data to registry, Registry needs input stream with the
 * data, so this class will write to a output stream which will be read by registry at the end.
 */
public class WorkBookOutputWriter extends Thread {
    private static final Log log = LogFactory.getLog(WorkBookOutputWriter.class);
    private Workbook workbook;
    private OutputStream outputStream;
    public WorkBookOutputWriter(Workbook workbook, OutputStream outputStream) {
        this.workbook = workbook;
        this.outputStream = outputStream;
    }
    @Override
    public void run() {
        try {
            workbook.write(outputStream);
        } catch (IOException e) {
            log.error("Error saving excel data to registry, Error - " + e.getMessage(), e);
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                //ignore
            }
        }
    }
}
