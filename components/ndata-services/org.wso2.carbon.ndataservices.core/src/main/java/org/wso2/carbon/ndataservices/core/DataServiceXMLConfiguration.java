package org.wso2.carbon.ndataservices.core;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement(name = "dataservice")
@XmlAccessorType(XmlAccessType.FIELD)
public class DataServiceXMLConfiguration {

    @XmlAttribute(name = "name")
    protected String name;

    @XmlAttribute(name = "version")
    protected String version;

    @XmlElement(name = "resource")
    private List<Resource> resources;

    @XmlElement(name = "datasource")
    private List<Datasource> datasources;

    @XmlElement(name = "query")
    private List<Query> queries;

    @XmlElement(name = "sequence")
    private List<Sequence> sequences;

    @XmlElement(name = "operation")
    private List<Operation> operations;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<Datasource> getDatasources() {
        return datasources;
    }

    public void setDatasources(List<Datasource> datasources) {
        this.datasources = datasources;
    }

    public List<Query> getQueries() {
        return queries;
    }

    public void setQueries(List<Query> queries) {
        this.queries = queries;
    }

    public List<Sequence> getSequences() {
        return sequences;
    }

    public void setSequences(List<Sequence> sequences) {
        this.sequences = sequences;
    }

    public List<Operation> getOperations() {
        return operations;
    }

    public void setOperations(List<Operation> operations) {
        this.operations = operations;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Datasource {

        @XmlAttribute(name = "id")
        protected String id;

        @XmlAttribute(name = "type")
        protected String type;

        @XmlElement(name = "driverClassName")
        private String driverClassName;

        @XmlElement(name = "url")
        private String url;

        @XmlElement(name = "username")
        private String username;

        @XmlElement(name = "password")
        private String password;

        @XmlElement(name = "host")
        private String host;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDriverClassName() {
            return driverClassName;
        }

        public void setDriverClassName(String driverClassName) {
            this.driverClassName = driverClassName;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Query {

        @XmlAttribute(name = "id")
        protected String id;

        @XmlAttribute(name = "datasource")
        protected String datasource;

        @XmlElement(name = "sql")
        private String sql;

        @XmlElement(name = "param")
        private List<Param> params;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getDatasource() {
            return datasource;
        }

        public void setDatasource(String datasource) {
            this.datasource = datasource;
        }

        public String getSql() {
            return sql;
        }

        public void setSql(String sql) {
            this.sql = sql;
        }

        public List<Param> getParams() {
            return params;
        }

        public void setParams(List<Param> params) {
            this.params = params;
        }

        public static class Param {

            protected String name;

            @XmlAttribute(name = "name")
            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            protected String ordinal;

            @XmlAttribute(name = "ordinal")
            public String getOrdinal() {
                return ordinal;
            }

            public void setOrdinal(String ordinal) {
                this.ordinal = ordinal;
            }

            protected String type;

            @XmlAttribute(name = "type")
            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            protected String array;

            @XmlAttribute(name = "array")
            public String getArray() {
                return array;
            }

            public void setArray(String array) {
                this.array = array;
            }

        }

        @XmlElement(name = "queryResult", required=false)
        private QueryResult queryResult;

        public QueryResult getQueryResult() {
            return queryResult;
        }

        public void setQueryResult(QueryResult queryResult) {
            this.queryResult = queryResult;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        public static class QueryResult {

            @XmlElement(name = "object")
            private List<Object> objects;

            public List<Object> getObjects() {
                return objects;
            }

            public void setObjects(List<Object> objects) {
                this.objects = objects;
            }

        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Sequence {

        @XmlAttribute(name = "id")
        protected String id;

        @XmlElement(name = "call-query", required=false)
        private List<CallQuery> callQueries;

        @XmlElement(name = "condition", required=false)
        private List<Condition> conditions;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public List<CallQuery> getCallQueries() {
            return callQueries;
        }

        public void setCallQueries(List<CallQuery> callQueries) {
            this.callQueries = callQueries;
        }

        public List<Condition> getConditions() {
            return conditions;
        }

        public void setConditions(List<Condition> conditions) {
            this.conditions = conditions;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Operation {

        @XmlAttribute(name = "id")
        protected String id;

        @XmlElement(name = "transaction")
        private Transaction transaction;

        @XmlElement(name = "call-query", required=false)
        private List<CallQuery> callQueries;

        @XmlElement(name = "condition", required=false)
        private List<Condition> conditions;

        @XmlElement(name = "input", required=false)
        private Input input;

        @XmlElement(name = "sequence")
        private SequenceRef sequenceRef;

        @XmlElement(name = "result")
        private Result result;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Transaction getTransaction() {
            return transaction;
        }

        public void setTransaction(Transaction transaction) {
            this.transaction = transaction;
        }

        public List<CallQuery> getCallQueries() {
            return callQueries;
        }

        public void setCallQueries(List<CallQuery> callQueries) {
            this.callQueries = callQueries;
        }

        public List<Condition> getConditions() {
            return conditions;
        }

        public void setConditions(List<Condition> conditions) {
            this.conditions = conditions;
        }

        public Input getInput() {
            return input;
        }

        public void setInput(Input input) {
            this.input = input;
        }

        public Result getResult() {
            return result;
        }

        public void setResult(Result result) {
            this.result = result;
        }

        public SequenceRef getSequenceRef() {
            return sequenceRef;
        }

        public void setSequenceRef(SequenceRef sequenceRef) {
            this.sequenceRef = sequenceRef;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Resource {

        @XmlAttribute(name = "id")
        protected String id;

        @XmlElement(name = "transaction")
        private Transaction transaction;

        @XmlElement(name = "call-query", required=false)
        private List<CallQuery> callQueries;

        @XmlElement(name = "condition", required=false)
        private List<Condition> conditions;

        @XmlElement(name = "input", required=false)
        private Input input;

        @XmlElement(name = "sequence")
        private SequenceRef sequenceRef;

        @XmlElement(name = "result")
        private Result result;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Transaction getTransaction() {
            return transaction;
        }

        public void setTransaction(Transaction transaction) {
            this.transaction = transaction;
        }

        public List<CallQuery> getCallQueries() {
            return callQueries;
        }

        public void setCallQueries(List<CallQuery> callQueries) {
            this.callQueries = callQueries;
        }

        public List<Condition> getConditions() {
            return conditions;
        }

        public void setConditions(List<Condition> conditions) {
            this.conditions = conditions;
        }

        public Input getInput() {
            return input;
        }

        public void setInput(Input input) {
            this.input = input;
        }

        public Result getResult() {
            return result;
        }

        public void setResult(Result result) {
            this.result = result;
        }

        public SequenceRef getSequenceRef() {
            return sequenceRef;
        }

        public void setSequenceRef(SequenceRef sequenceRef) {
            this.sequenceRef = sequenceRef;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Transaction {

        @XmlAttribute(name = "model")
        protected String model;

        @XmlElement(name = "call-query", required=false)
        private List<CallQuery> callQueries;

        @XmlElement(name = "condition", required=false)
        private List<Condition> conditions;

        @XmlElement(name = "sequence")
        private SequenceRef sequenceRef;

        @XmlElement(name = "result")
        private Result result;

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public List<CallQuery> getCallQueries() {
            return callQueries;
        }

        public void setCallQueries(List<CallQuery> callQueries) {
            this.callQueries = callQueries;
        }

        public List<Condition> getConditions() {
            return conditions;
        }

        public void setConditions(List<Condition> conditions) {
            this.conditions = conditions;
        }

        public Result getResult() {
            return result;
        }

        public void setResult(Result result) {
            this.result = result;
        }

        public SequenceRef getSequenceRef() {
            return sequenceRef;
        }

        public void setSequenceRef(SequenceRef sequenceRef) {
            this.sequenceRef = sequenceRef;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Input {

        @XmlElement(name = "model")
        protected Model model;

        @XmlElement(name = "mapping")
        protected Mapping mapping;

        public Model getModel () {
            return model;
        }

        public void setModel(Model model) {
            this.model = model;
        }

        public Mapping getMapping() {
            return mapping;
        }

        public void setMapping(Mapping mapping) {
            this.mapping = mapping;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class SequenceRef {

        @XmlAttribute(name = "name")
        protected String name;

        public String getName () {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Model {

        @XmlElement(name = "attribute")
        private List<Attribute> attributes;

        @XmlElement(name = "object")
        private List<Object> objects;

        public List<Attribute> getAttributes() {
            return attributes;
        }

        public void setAttributes(List<Attribute> attributes) {
            this.attributes = attributes;
        }

        public List<Object> getObjects() {
            return objects;
        }

        public void setObjects(List<Object> objects) {
            this.objects = objects;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Mapping {

        @XmlAttribute(name = "type")
        protected String type;

        @XmlElement(name = "data", type = String.class)
        protected String data;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Object {

        @XmlAttribute (name = "name")
        private String name;

        @XmlAttribute (name = "array")
        private String array;

        @XmlAttribute (name = "source")
        private String source;

        @XmlElement(name = "attribute")
        private List<Attribute> attributes;

        @XmlElement(name = "object")
        private List<Object> objects;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getArray() {
            return array;
        }

        public void setArray(String array) {
            this.array = array;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public List<Attribute> getAttributes() {
            return attributes;
        }

        public void setAttributes(List<Attribute> attributes) {
            this.attributes = attributes;
        }

        public List<Object> getObjects() {
            return objects;
        }

        public void setObjects(List<Object> objects) {
            this.objects = objects;
        }

    }

    public static class Attribute {

        private String type;

        private String name;

        private String array;

        private String column;

        @XmlAttribute (name = "type")
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }


        @XmlAttribute (name = "name")
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }


        @XmlAttribute (name = "array")
        public String getArray() {
            return array;
        }

        public void setArray(String array) {
            this.array = array;
        }

        @XmlAttribute (name = "column")
        public String getColumn() {
            return column;
        }

        public void setColumn(String column) {
            this.column = column;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class CallQuery {

        @XmlAttribute(name = "href")
        protected String href;

        @XmlAttribute(name = "target")
        protected String target;

        @XmlElement(name = "with-param", required=false)
        private List<WithParam> withParams;

        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }

        public List<WithParam> getWithParams() {
            return withParams;
        }

        public void setWithParams(List<WithParam> withParams) {
            this.withParams = withParams;
        }

        public static class WithParam {

            protected String name;
            protected String queryParam;

            @XmlAttribute(name = "name")
            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            @XmlAttribute(name = "query-param")
            public String getQueryParam() {
                return queryParam;
            }

            public void setQueryParam(String queryParam) {
                this.queryParam = queryParam;
            }

        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Condition {

        @XmlAttribute(name = "source")
        protected String source;

        @XmlAttribute(name = "expression")
        protected String expression;

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Set {

        @XmlAttribute(name = "source")
        protected String source;

        @XmlAttribute(name = "target")
        protected String target;


        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Result {

        @XmlAttribute(name = "source")
        protected String source;

        @XmlElement(name = "mapping")
        protected Mapping mapping;

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public Mapping getMapping() {
            return mapping;
        }

        public void setMapping(Mapping mapping) {
            this.mapping = mapping;
        }

    }

}
