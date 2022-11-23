package eu.europa.ec.digit.leos.pilot.export.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class DocumentApiUtil {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentApiUtil.class);

    public static ResponseEntity<Object> buildErrorResponse(String message, Exception e, HttpStatus httpStatus) {
        return buildErrorResponse(message, e, httpStatus, false);
    }

    public static ResponseEntity<Object> buildErrorResponse(String message, Exception e, HttpStatus httpStatus, boolean isSevere) {
        if (isSevere) {
            LOG.error(message, e);
        } else {
            LOG.info(message, e);
        }
        return new ResponseEntity<>(message + ": " + e.getMessage(), httpStatus);
    }

    public static ResponseEntity<Object> buildValidZipResponse(byte[] outputFile) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(
                ContentDisposition
                        .builder("attachment")
                        .filename("result_" + System.currentTimeMillis() + ".zip")
                        .build());
        headers.setContentType(MediaType.valueOf(ZipUtil.APPLICATION_ZIP_VALUE));
        headers.setContentLength(outputFile.length);
        return new ResponseEntity<>(outputFile, headers, HttpStatus.OK);
    }

}
