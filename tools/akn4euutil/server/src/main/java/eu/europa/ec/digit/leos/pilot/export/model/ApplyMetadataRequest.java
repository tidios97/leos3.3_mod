package eu.europa.ec.digit.leos.pilot.export.model;

import java.util.List;
import eu.europa.ec.digit.leos.pilot.export.model.ApplyMetadataRequest.DocumentNode;

public class ApplyMetadataRequest {
    private final String xmlns;
    private final String version;
    private final String date;
    private final String requestId;
    private DocumentNode document;
    private List<TaskNode> tasks;

    public static class DocumentNode {
        private final String sourceURL;
        private final String filename;
        private final String mimeType;
        private final String documentId;

        public DocumentNode(String sourceURL,
                            String filename,
                            String mimeType,
                            String documentId){
            this.sourceURL = sourceURL;
            this.filename = filename;
            this.mimeType = mimeType;
            this.documentId = documentId;
        }

        public String getSourceURL() {
            return sourceURL;
        }

        public String getFilename() {
            return filename;
        }

        public String getMimeType() {
            return mimeType;
        }

        public String getDocumentId() {
            return documentId;
        }

        public int getDocumentIdAsInt() {
            return Integer.parseInt(documentId);
        }

        public String toString(){
            return String.format("Document(sourceURL: %s / filename: %s / mimeType: %s / documentId %s)",
                    sourceURL, filename, mimeType, documentId);
        }
    }

    public static class FieldNode {
        private final String key;
        private final String value;

        public FieldNode(String key,
                         String value){
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        public String toString(){
            return String.format("Field(key: %s / value: %s)", key, value);
        }
    }

    public static class ActionNode {
        private final String name;
        private final String cleanUp;
        private List<FieldNode> fields;

        public ActionNode(String name,
                          String cleanUp){
            this.name = name;
            this.cleanUp = cleanUp;
            this.fields = null;
        }

        public String getName() {
            return name;
        }

        public String getCleanUp() {
            return cleanUp;
        }

        public boolean isCleanUp() {
            return Boolean.parseBoolean(cleanUp);
        }

        public List<FieldNode> getFields() {
            return fields;
        }

        public void setFields(List<FieldNode> fields) {
            this.fields = fields;
        }

        public String toString(){
            String result = String.format("Action(name: %s / cleanUp: %s / Field count: %s)\n",
                    name, cleanUp, (fields != null) ? fields.size() : 0);
            if (fields != null){
                for (FieldNode field : fields){ result += (field.toString() + "\n"); }
            }
            return result;
        }
    }

    public static class TaskNode {
        private final String taskId;
        private List<ActionNode> actions;

        public TaskNode(String taskId){
            this.taskId = taskId;
            this.actions = null;
        }

        public String getTaskId() {
            return taskId;
        }

        public int getTaskIdAsInt() {
            return Integer.parseInt(taskId);
        }

        public List<ActionNode> getActions() {
            return actions;
        }

        public void setActions(List<ActionNode> actions){
            this.actions = actions;
        }

        public String toString(){
            String result = String.format("Task(taskId: %s / Action count: %s)\n",
                    taskId, (actions != null) ? actions.size() : 0);
            if (actions != null){
                for (ActionNode action : actions){ result += action.toString() + "\n"; }
            }
            return result;
        }
    }

    public ApplyMetadataRequest(String xmlns,
                         String version,
                         String date,
                         String requestId){
        this.xmlns = xmlns;
        this.version = version;
        this.date = date;
        this.requestId = requestId;
        this.document = null;
        this.tasks = null;
    }

    public String getRequestId() {
        return requestId;
    }

    public int getRequestIdAsInt() {
        return Integer.parseInt(requestId);
    }

    public String getDate() {
        return date;
    }

    public String getVersion() {
        return version;
    }

    public int getVersionAsInt() {
        return Integer.parseInt(version);
    }

    public String getXmlns() {
        return xmlns;
    }

    public DocumentNode getDocument() {
        return document;
    }

    public List<TaskNode> getTasks() {
        return tasks;
    }

    public void setDocument(DocumentNode document){
        this.document = document;
    }

    public void setTasks(List<TaskNode> tasks){
        this.tasks = tasks;
    }

    public String toString(){
        String result = String.format("ApplyMetadataRequest(xmlns: %s / version: %s / date: %s / requestId: %s / Task count: %s)\n",
                xmlns, version, date, requestId, (tasks != null) ? tasks.size() : 0);
        if (document != null){
            result += document.toString() + "\n";
        }
        if (tasks != null){
            for (TaskNode task : tasks){ result += (task.toString()) + "\n"; }
        }
        return result;
    }
}