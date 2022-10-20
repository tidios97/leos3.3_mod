package europa.edit.util;

import com.google.common.io.Files;
import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2CreateOptions;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.testng.Assert.*;

/* 	Author: Satyabrata Das
 * 	Functionality: Utility functions needed for the script execution
 */
public class E2eUtil {
    static Cryptor td = new Cryptor();
    static Configuration config = new Configuration();
    private static final Logger logger = LoggerFactory.getLogger(E2eUtil.class);

    public static String getDateandTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static void scrollandClick(WebDriver driver, By by) {
        WebElement webElement = driver.findElement(by);
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        executor.executeScript("arguments[0].scrollIntoView(true);", webElement);
        executor.executeScript("arguments[0].click();", webElement);
        logger.info("Element {} clicked", webElement);
    }

    public static void scrollAndClick(WebDriver driver, WebElement webElement) {
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        executor.executeScript("arguments[0].scrollIntoView(true);", webElement);
        executor.executeScript("arguments[0].click();", webElement);
        logger.info("Element {} clicked", webElement);
    }

    public static void takeSnapShot(WebDriver driver, String status) {
        if (config.getProperty("takeScreenshots.pass").contains("TRUE") && status.contains("PASS")) {
            copyScreenshot(driver);
        }
        if (config.getProperty("takeScreenshots.fail").contains("TRUE") && status.contains("FAIL")) {
            copyScreenshot(driver);
        }
    }

    // support function to take screenshot
    private static void copyScreenshot(WebDriver driver) {
        TakesScreenshot scrShot = ((TakesScreenshot) driver);
        File srcFile = scrShot.getScreenshotAs(OutputType.FILE);
        String fileName = Constants.RESULTS_LOCATION + File.separator + "Screenshots" + File.separator + "Screenshot_" + getDateandTime() + ".PNG";
        File destFile = new File(fileName);
        boolean copyScreenshots = Boolean.parseBoolean(System.getProperty("copyScreenshots"));
        if (copyScreenshots) {
            TestParameters.getInstance().setScreenshotPath(fileName);
        } else {
            TestParameters.getInstance().setScreenshotPath(destFile.getAbsolutePath());
        }
        try {
            FileUtils.copyFile(srcFile, destFile);
            logger.info("Screenshot captured at " + destFile);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    // Definite wait needed at multiple place, used a function instead Thread.sleep method
    public static void wait(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void scrollandDoubleClick(WebDriver driver, By element) {
        WebElement webElement = driver.findElement(element);
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        executor.executeScript("arguments[0].scrollIntoView(true);", webElement);
        executor.executeScript("var evt = document.createEvent('MouseEvents'); evt.initMouseEvent('dblclick',true, true, window, 0, 0, 0, 0, 0, false, false, false, false, 0,null);arguments[0].dispatchEvent(evt);", element);
        logger.info("Element {} double clicked", webElement);
    }

    public static DiskShare smbConnect() {
        try {
            DiskShare share;
            SmbConfig sconfig = SmbConfig.builder()
                    .withTimeout(120, TimeUnit.SECONDS) // Timeout sets Read, Write, and Transact timeouts (default is 60 seconds)
                    .withSoTimeout(180, TimeUnit.SECONDS) // Socket Timeout (default is 0 seconds, blocks forever)
                    .build();
            @SuppressWarnings("resource")
            SMBClient client = new SMBClient(sconfig);
            Connection connection = client.connect(config.getProperty("remote.machine.name"));
            AuthenticationContext ac = new AuthenticationContext(config.getProperty("user.remote.1.name"), td.decrypt(config.getProperty("user.remote.1.pwd")).toCharArray(), config.getProperty("domain"));
            Session session = connection.authenticate(ac);
            share = (DiskShare) session.connectShare(config.getProperty("remote.drive.name"));
            return share;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public static boolean unzip(String zipFilePath, String destDirectory, DiskShare share) {
        try {
            ZipInputStream zipIn = null;
            logger.info("before share.folderExists(destDirectory)");
            if (TestParameters.getInstance().getMode().equalsIgnoreCase("remote")) {
                if (!share.folderExists(destDirectory)) {
                    share.mkdir(destDirectory);
                    logger.info("destDir is created");
                }
                com.hierynomus.smbj.share.File file = share.openFile(zipFilePath,
                        EnumSet.of(AccessMask.FILE_READ_DATA),
                        null,
                        SMB2ShareAccess.ALL,
                        SMB2CreateDisposition.FILE_OPEN,
                        null);
                InputStream inputStream = file.getInputStream();
                zipIn = new ZipInputStream(inputStream);
                logger.info("zipIn is assigned");
            }
            if (TestParameters.getInstance().getMode().equalsIgnoreCase("local")) {
                zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
                logger.info("zipIn is assigned");
            }
            assertNotNull(zipIn);
            ZipEntry entry = zipIn.getNextEntry();
            logger.info("entry is assigned");
            String filePath = null;
            while (entry != null) {
                logger.info("entry is not null");
                if (TestParameters.getInstance().getMode().equalsIgnoreCase("remote")) {
                    logger.info("entry :" + entry.getName());
                    filePath = destDirectory + File.separator + entry.getName();
                }
                if (TestParameters.getInstance().getMode().equalsIgnoreCase("local")) {
                    filePath = destDirectory + File.separator + entry.getName();
                }
                logger.info("filePath " + filePath);
                if (!entry.isDirectory()) {
                    logger.info("entry.isDirectory()");
                    extractFile(zipIn, filePath, share);
                } else {
                    if (TestParameters.getInstance().getMode().equalsIgnoreCase("remote")) {
                        share.mkdir(filePath);
                    }
                    if (TestParameters.getInstance().getMode().equalsIgnoreCase("local")) {
                        assertNotNull(filePath);
                        File dir = new File(filePath);
                        boolean bool = dir.mkdirs();
                        logger.info(String.valueOf(bool));
                    }
                }
                zipIn.closeEntry();
                logger.info("zipIn closeEntry");
                entry = zipIn.getNextEntry();
                logger.info("zipIn getNextEntry");
            }
            zipIn.close();
            logger.info("zipIn close");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static void extractFile(ZipInputStream zipIn, String filePath, DiskShare share) throws IOException {
        logger.info("entry extractFile");
        BufferedOutputStream bos = null;
        if (TestParameters.getInstance().getMode().equalsIgnoreCase("remote")) {
            com.hierynomus.smbj.share.File file = share.openFile(filePath
                    , EnumSet.of(AccessMask.GENERIC_ALL)
                    , null, SMB2ShareAccess.ALL
                    , SMB2CreateDisposition.FILE_OVERWRITE_IF
                    , null);
            OutputStream outStream = file.getOutputStream();
            bos = new BufferedOutputStream(outStream);
        }
        if (TestParameters.getInstance().getMode().equalsIgnoreCase("local")) {
            bos = new BufferedOutputStream(new FileOutputStream(filePath));
        }
        if (null != bos) {
            logger.info("bos BufferedOutputStream");
            byte[] bytesIn = new byte[Constants.BUFFER_SIZE];
            logger.info("bytesIn BufferedOutputStream");
            int read;
            while ((read = zipIn.read(bytesIn)) != -1) {
                logger.info("zipIn.read");
                bos.write(bytesIn, 0, read);
                logger.info("bos bytesIn");
            }
            logger.info("while completed");
            bos.close();
            logger.info("bos.close()");
        }
    }

    public static HashMap<String, String> findLatestFile(String fileType, String relativePath, DiskShare diskShare) throws IOException {
        long lastModifiedTime = Long.MIN_VALUE;
        String chosenFileName = null;
        HashMap<String, String> map = new HashMap<>();
        //String relativeRemotePath= "";
        //String relativeLocalPath= "";
        String relativePathLocation = "";
        if (null != relativePath && !"".equals(relativePath)) {
            //relativeRemotePath = Constants.SLASH + relativePath;
            //relativeLocalPath = Constants.FILE_SEPARATOR + relativePath;
            relativePathLocation = File.separator + relativePath;
        }
        try {
            if (TestParameters.getInstance().getMode().equalsIgnoreCase("remote")) {
                if (null != diskShare) {
                    if (null != diskShare.list(config.getProperty("path.remote.download.relative") + relativePathLocation, "*." + fileType)) {
                        for (FileIdBothDirectoryInformation f : diskShare.list(config.getProperty("path.remote.download.relative") + relativePathLocation, "*." + fileType)) {
                            if (f.getLastAccessTime().getWindowsTimeStamp() > lastModifiedTime) {
                                chosenFileName = f.getFileName();
                                lastModifiedTime = f.getLastAccessTime().getWindowsTimeStamp();
                            }
                        }
                    } else {
                        fail("Issue while finding files from a directory in remote location");
                    }
                } else {
                    try {
                        DiskShare share = smbConnect();
                        if (null != share) {
                            if (null != share.list(config.getProperty("path.remote.download.relative") + relativePathLocation, "*." + fileType)) {
                                for (FileIdBothDirectoryInformation f : share.list(config.getProperty("path.remote.download.relative") + relativePathLocation, "*." + fileType)) {
                                    if (f.getLastAccessTime().getWindowsTimeStamp() > lastModifiedTime) {
                                        chosenFileName = f.getFileName();
                                        lastModifiedTime = f.getLastAccessTime().getWindowsTimeStamp();
                                    }
                                }
                            } else {
                                fail("Issue while finding files from a directory in remote location");
                            }
                        } else {
                            fail("DiskShare is coming null");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw e;
                    }
                }
            }
            if (TestParameters.getInstance().getMode().equalsIgnoreCase("local")) {
                File directory = new File(System.getProperty("user.dir") + config.getProperty("relative.download.path.local") + relativePathLocation);
                chosenFileName = getRecentFileName(fileType, lastModifiedTime, chosenFileName, directory);
            }
            if (null != chosenFileName) {
                map.put(Constants.FILE_NAME, chosenFileName);
                String FilePath = "";
                if (TestParameters.getInstance().getMode().equalsIgnoreCase("remote")) {
                    FilePath = config.getProperty("path.remote.download") + relativePathLocation + File.separator + chosenFileName;
                }
                if (TestParameters.getInstance().getMode().equalsIgnoreCase("local")) {
                    FilePath = System.getProperty("user.dir") + config.getProperty("relative.download.path.local") + relativePathLocation + File.separator + chosenFileName;
                }
                logger.info("FilePath " + FilePath);
                map.put(Constants.FILE_FULL_PATH, FilePath);
                return map;
            }
        } catch (Exception | AssertionError e) {
            e.printStackTrace();
            throw e;
        }
        if (null != diskShare) {
            diskShare.close();
        }
        return map;
    }

    private static String getRecentFileName(String fileType, long lastModifiedTime, String chosenFileName, File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                String extension = Files.getFileExtension(file.getName());
                if (extension.equalsIgnoreCase(fileType)) {
                    if (file.lastModified() > lastModifiedTime) {
                        chosenFileName = file.getName();
                        lastModifiedTime = file.lastModified();
                    }
                }
            }
        } else {
            fail("Issue while finding files from a directory in local path");
        }
        return chosenFileName;
    }

    public static void findAndUnzipFile(String fileType, String relativePath, String searchFileType) throws IOException {
        long lastModifiedTime = Long.MIN_VALUE;
        String chosenFileName = null;
        DiskShare share = null;
        try {
            if (TestParameters.getInstance().getMode().equalsIgnoreCase("remote")) {
                try {
                    share = smbConnect();
                    if (null != share) {
                        logger.info("share is not null");
                        if (null != share.list(config.getProperty("path.remote.download.relative"), "*." + fileType)) {
                            logger.info("share.list is not null");
                            for (FileIdBothDirectoryInformation f : share.list(config.getProperty("path.remote.download.relative"), "*." + fileType)) {
                                logger.info("FileIdBothDirectoryInformation has value");
                                if (f.getLastAccessTime().getWindowsTimeStamp() > lastModifiedTime) {
                                    logger.info("lastModifiedTime is less");
                                    chosenFileName = f.getFileName();
                                    lastModifiedTime = f.getLastAccessTime().getWindowsTimeStamp();
                                }
                            }
                        } else {
                            fail("Issue while finding files from a directory in remote location");
                        }
                    } else {
                        fail("DiskShare is coming null");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    throw e;
                }
            }
            if (TestParameters.getInstance().getMode().equalsIgnoreCase("local")) {
                File directory = new File(System.getProperty("user.dir") + config.getProperty("relative.download.path.local"));
                chosenFileName = getRecentFileName(fileType, lastModifiedTime, chosenFileName, directory);
            }
        } catch (Exception | AssertionError e) {
            e.printStackTrace();
            throw e;
        }
        if (null != chosenFileName) {
            logger.info("chosenFileName is not null");
            String zipFilePath = "";
            String destDirectory = "";
            if (TestParameters.getInstance().getMode().equalsIgnoreCase("remote")) {
                zipFilePath = config.getProperty("path.remote.download") + Constants.FILE_SEPARATOR + chosenFileName;
                destDirectory = config.getProperty("path.remote.download") + Constants.FILE_SEPARATOR + relativePath;
            }
            if (TestParameters.getInstance().getMode().equalsIgnoreCase("local")) {
                zipFilePath = System.getProperty("user.dir") + config.getProperty("relative.download.path.local") + Constants.FILE_SEPARATOR + chosenFileName;
                destDirectory = System.getProperty("user.dir") + config.getProperty("relative.download.path.local") + Constants.FILE_SEPARATOR + relativePath;
            }
            logger.info("zipFilePath " + zipFilePath);
            logger.info("destDirectory " + destDirectory);
            try {
                boolean bool = false;
                if (TestParameters.getInstance().getMode().equalsIgnoreCase("remote")) {
                    bool = unzip(config.getProperty("path.remote.download.relative") + Constants.FILE_SEPARATOR + chosenFileName, config.getProperty("path.remote.download.relative") + Constants.FILE_SEPARATOR + relativePath, share);
                }
                if (TestParameters.getInstance().getMode().equalsIgnoreCase("local")) {
                    bool = unzip(zipFilePath, destDirectory, null);
                }
                if (!bool) {
                    fail("Error while unzipping file");
                } else {
                    HashMap<String, String> map = findLatestFile(searchFileType, relativePath, share);
                    String FileNamePath = map.get(Constants.FILE_FULL_PATH);
                    if (null != FileNamePath) {
                        System.out.println("legFileNamePath " + FileNamePath);
                    } else {
                        fail("Unable to find the " + fileType + " file");
                    }
                }
            } catch (Exception | AssertionError ex) {
                ex.printStackTrace();
                throw ex;
            }
        }
        if (null != share) {
            share.close();
        }
    }

    public static void highlightElement(WebDriver driver, WebElement element) {
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
        jsExecutor.executeScript("arguments[0].setAttribute('style', 'border:3px solid yellow;')", element);
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        jsExecutor.executeScript("arguments[0].setAttribute('style', 'border:;')", element);
    }

    public static InputStream readFile(String relativeLocation, String sourceFileFullPath, String fileName) throws FileNotFoundException {
        InputStream in = null;
        if (TestParameters.getInstance().getMode().equalsIgnoreCase("remote")) {
            DiskShare share = smbConnect();
            assertNotNull(share);
            sourceFileFullPath = config.getProperty("path.remote.download.relative") + File.separator + relativeLocation + File.separator + fileName;
            final com.hierynomus.smbj.share.File file = getFile(share, sourceFileFullPath, AccessMask.GENERIC_READ);
            in = file.getInputStream();
        }
        if (TestParameters.getInstance().getMode().equalsIgnoreCase("local")) {
            File initialFile = new File(sourceFileFullPath);
            in = new FileInputStream(initialFile);
        }
        return in;
    }

    private static com.hierynomus.smbj.share.File getFile(DiskShare diskShare, String sourceFilePath, AccessMask accessMask) {
        Set<SMB2ShareAccess> shareAccess = new HashSet<>(SMB2ShareAccess.ALL);

        Set<SMB2CreateOptions> createOptions = new HashSet<>();
        createOptions.add(SMB2CreateOptions.FILE_WRITE_THROUGH);

        Set<AccessMask> accessMaskSet = new HashSet<>();
        accessMaskSet.add(accessMask);
        return diskShare.openFile(sourceFilePath, accessMaskSet, null, shareAccess, SMB2CreateDisposition.FILE_OPEN, createOptions);
    }

    public static boolean moveFile(String relativeLocation, String sourceFileFullPath, String fileName) throws IOException {
        if (TestParameters.getInstance().getMode().equalsIgnoreCase("remote")) {
            String sourceFilePath = config.getProperty("path.remote.download.relative") + File.separator + fileName;
            String destinationPath = config.getProperty("path.remote.download.relative") + File.separator + relativeLocation + File.separator + fileName;
            DiskShare share = smbConnect();
            assertNotNull(share);
            try (com.hierynomus.smbj.share.File file = getFile(share, sourceFilePath, AccessMask.GENERIC_ALL)) {
                file.rename(destinationPath, true);
                return true;
            }
        }
        if (TestParameters.getInstance().getMode().equalsIgnoreCase("local")) {
            String destinationPath = System.getProperty("user.dir") + config.getProperty("relative.upload.path.local") + File.separator + relativeLocation + File.separator + fileName;
            Path sourcePath = Paths.get(sourceFileFullPath);
            Path targetPath = Paths.get(destinationPath);
            File file = targetPath.toFile();
            if (file.isFile()) {
                java.nio.file.Files.delete(targetPath);
            }
            java.nio.file.Files.move(sourcePath, targetPath);
            return true;
        }
        return false;
    }
}