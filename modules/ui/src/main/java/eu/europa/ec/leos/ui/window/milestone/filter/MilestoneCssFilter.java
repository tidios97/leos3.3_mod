package eu.europa.ec.leos.ui.window.milestone.filter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;

public class MilestoneCssFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(MilestoneCssFilter.class);
    
    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        LOG.info("Intercepted by MilestoneCssFilter...");
        String milestoneDir = FileUtils.getTempDirectoryPath() + "/milestone/";
        File folder = new File(milestoneDir);
        LOG.info("Found milestoneDir path " + folder.getPath());
        String[] extensions = {"css"};
        HttpServletRequest httpRequest = (HttpServletRequest)request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String requestURI = httpRequest.getRequestURI();
        String fileName = requestURI.substring(requestURI.lastIndexOf("/") + 1, requestURI.length());
        LOG.info("Request filename is - " + fileName);
        Collection<File> files = FileUtils.listFiles(folder, extensions, true);
        if (files.size() > 0) {
            LOG.info("Number of files found inside directory are - " + files.size());
            for (File file : files) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    if (file.getName().equalsIgnoreCase(fileName)) {
                        String mimeType = Files.probeContentType(file.toPath());
                        httpResponse.setContentType(mimeType);
                        IOUtils.copy(fis, httpResponse.getOutputStream());
                        httpResponse.flushBuffer();
                    }
                }
            }
        } else {
            chain.doFilter(request, response);
        }
        LOG.info("Exiting MilestoneCssFilter...");
    }

    @Override
    public void init(FilterConfig config) throws ServletException {
    }
}
