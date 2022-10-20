/*
 * Copyright 2021 European Commission
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package eu.europa.ec.leos.services.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.CollectionUtils;

import eu.europa.ec.leos.services.compare.ContentComparatorContext;
import eu.europa.ec.leos.services.compare.vo.Element;

/**
 * @author bogdan.mladinescu
 */
public class XMLContentComparatorDebugLogger {

    /**
     *  Service used to debug the element transitions with respect to their node index
     *
     *
     * How to use (only in JUnit tests as the logging context is simple - tied to the current Thread ID):
     *
     *                 DebugLoggerService.addOperationToLog(String.format("Operation 23 - oldContent id = [%s], index = [%s] ; newContent id = [%s], index = [%s]",
     *                         DebugLoggerService.getNonNullOldId(context, oldContentChildIndex), oldContentChildIndex,
     *                         DebugLoggerService.getNonNullNewTagId(context, newContentChildIndex), newContentChildIndex));
     *
     *  An output file will be created in the specified directory which will contain relevant information to pinpointing where the Elements have their indexes increased ( changing positions )
     *
     */



    private final static String OUTPUT_DIRECTORY = "c:/work";

    private static Map<String, List<String>> sessionLoggingContent = new HashMap<>();

    private static final String threadId = "1";

    static {
        sessionLoggingContent.put(threadId(), new ArrayList<>());
    }

    public static void startLoggingSession() {
        sessionLoggingContent.put(threadId(), new ArrayList<>());
    }

    public static void endLoggingSession() {
        sessionLoggingContent.remove(threadId());
    }

    public static void addOperationToLog(String content) {
        if(1==1)
        {
            return;
        }
        sessionLoggingContent.get(threadId()).add(content);
    }

    public static String getNonNullTagId(Element element, int index) {

        if(element == null) {
            return "null-element";
        }

        if(CollectionUtils.isEmpty(element.getChildren())) {
            return "no-children-" + element.getTagId();
        }

        if(element.getChildren().size() <= index) {

            if(index != 0 && index == element.getChildren().size())
            {
                return String.format("index-array-out-of-bounds tagId = [%s] ; size = [%s], index [%s]",element.getChildren().get(index - 1).getTagId(), element.getChildren().size(), index);
            }
            return String.format("index-array-out-of-bounds ; size = [%s], index [%s]",element.getChildren().size(), index);
        }

        if(element.getChildren().get(index) == null) {
            return "null-child";
        }

        return element.getChildren().get(index).getTagId();
    }

    public static String getNonNullOldId(ContentComparatorContext context, int index) {
        return getNonNullTagId(context.getOldElement(), index);

    }

    public static String getNonNullNewTagId(ContentComparatorContext context, int index) {
        return getNonNullTagId(context.getNewElement(), index);
    }

    public static void outputContentToLogFile() {
        List<String> loggingOperations = sessionLoggingContent.get(threadId());

        File outputFile = null;
        FileWriter fw = null;

        try {

            outputFile = new File(OUTPUT_DIRECTORY + File.separator + threadId() + ".log");

            if(!outputFile.exists()) {
                outputFile.createNewFile();
            }

            fw = new FileWriter(outputFile, false);

            for(String loggingOp : loggingOperations) {
                fw.write(loggingOp);
                fw.write("\n");
            }

        } catch(Exception e) {
            e.printStackTrace();
        }
        finally {

            try {
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            endLoggingSession();
        }
    }

    private static String threadId() {
        return threadId;
        //return "" + Thread.currentThread().getId();
    }

}
