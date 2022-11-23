package eu.europa.ec.digit.leos.pilot.export.model;

import java.util.List;

public class ApplyMetadataResponse {
    public final String responseId;
    public final String version = "1.0";
    public final String xmlns = "http://ec.europa.eu/sg/egreffe/legiswrite/v1/types";
    private final DocumentNode document;
    private final List<TaskNode> tasks;
    private final StatusNode status;

    public ApplyMetadataResponse(String responseId,
                          DocumentNode document,
                          List<TaskNode> tasks,
                          StatusNode status){
        this.responseId = responseId;
        this.status = status;
        this.tasks = tasks;
        this.document = document;
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

    public StatusNode getStatus() {
        return status;
    }

    public String getResponseId() {
        return responseId;
    }

    public String getVersion() {
        return version;
    }

    public String toString(){
        String result = String.format("ApplyMetadataRequest(xmlns: %s / version: %s / responseId: %s / Task count: %s)\n",
                xmlns, version, responseId, (tasks != null) ? tasks.size() : 0);
        if (document != null){
            result += document.toString() + "\n";
        }
        if (status != null){
            result += status.toString() + "\n";
        }
        if (tasks != null){
            for (TaskNode task : tasks){ result += (task.toString()) + "\n"; }
        }
        return result;
    }

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

        public String toString(){
            return String.format("Document(sourceURL: %s / filename: %s / mimeType: %s / documentId %s)",
                    sourceURL, filename, mimeType, documentId);
        }
    }

    public static class FieldNode {
        private final String key;
        private final String statusCode;
        private final String value;

        public FieldNode(String key,
                         String statusCode,
                         String value){
            this.key = key;
            this.value = value;
            this.statusCode = statusCode;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        public String getStatusCode() {
            return statusCode;
        }

        public String toString(){
            return String.format("Field(key: %s / statusCode: %s / value: %s)", key, statusCode, value);
        }
    }
    public static class ActionNode {
        private final String name;
        private final List<FieldNode> fields;

        public ActionNode(String name,
                          List<FieldNode> fields){
            this.name = name;
            this.fields = fields;
        }

        public String getName() {
            return name;
        }

        public List<FieldNode> getFields() {
            return fields;
        }

        public String toString(){
            String result = String.format("Action(name: %s / Field count: %s)\n",
                    name, (fields != null) ? fields.size() : 0);
            if (fields != null){
                for (FieldNode field : fields){ result += (field.toString() + "\n"); }
            }
            return result;
        }
    }

    public static class ValidationResultNode {
        private final String key;
        private final String statusCode;

        public ValidationResultNode(String key,
                                    String statusCode){
            this.key = key;
            this.statusCode = statusCode;
        }

        public String getKey() {
            return key;
        }

        public String getStatusCode() {
            return statusCode;
        }

        public String toString(){
            return String.format("ValidationResult(key: %s / statusCode: %s)", key, statusCode);
        }
    }

    public static class TaskNode {
        private final String taskId;
        private final String statusCode;
        private final ValidationResultNode validationResult;
        private final List<ActionNode> actions;

        public TaskNode(String taskId,
                        String statusCode,
                        List<ActionNode> actions,
                        ValidationResultNode validationResult){
            this.taskId = taskId;
            this.statusCode = statusCode;
            this.actions = actions;
            this.validationResult = validationResult;
        }

        public String getTaskId() {
            return taskId;
        }

        public String getStatusCode() {
            return statusCode;
        }

        public List<ActionNode> getActions() {
            return actions;
        }

        public ValidationResultNode getValidationResult() {
            return validationResult;
        }

        public String toString(){
            String result = String.format("Task(taskId: %s / statusCode: %s / Action count: %s)\n",
                    taskId, statusCode, (actions != null) ? actions.size() : 0);
            if (actions != null){
                for (ActionNode action : actions){ result += action.toString() + "\n"; }
            }
            return result;
        }
    }

    public static class StatusNode {
        private final String code;
        private final String value;

        public StatusNode(String code,
                          String value){
            this.code = code;
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public String getCode() {
            return code;
        }

        public String toString() {
            return String.format("Status(code: %s / value: %s)", code, value);
        }
    }
}