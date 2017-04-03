<%--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
--%>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.dataservices.ui.beans.*" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName" %>
<%@ page import="org.wso2.carbon.user.mgt.ui.UserAdminClient" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.dataservices.ui.DataServiceAdminClient" %>
<jsp:useBean id="dataService" class="org.wso2.carbon.dataservices.ui.beans.Data" scope="session"/>
<%!

    private List<String> getSubComplexElementList(String path) {
        String children[] = path.split("/");
        List<String> pathTokens = new ArrayList<String>();
        for (int i = 0; i < children.length - 1; i++) {
            if (children[i].length() > 0) {
                pathTokens.add(children[i]);
            }
        }
        return pathTokens;
    }

    private String getSubComplexPath(String path) {
        String children[] = path.split("/");
        String subPath = "";
        for (int i = 0; i < children.length; i++) {
            if (i < children.length - 1 && !children[i].equals("")) {
                subPath = subPath + "/" + children[i];
            }
        }
        return subPath;

    }

    public void setElementValues(List<Element> elList, String elName,
                                 String dsType, String dsValue, String elementNs,
                                 String reqRoles, String exportName, String expType, String xsdType,
                                 String dataServiceOMElementName, String arrayName,
                                 HttpServletRequest request, String optional) {
        for (Element element : elList) {
            if (element.getName().equals(elName)) {
                element.setName(dataServiceOMElementName);
                element.setDataSourceType(dsType);
                element.setDataSourceValue(dsValue);
                element.setNamespace(elementNs);
                element.setArrayName(arrayName);
                element.setOptional(optional);
                //if (!reqRoles.equals("")) {
                    element.setRequiredRoles(reqRoles);
                //}
                if (!exportName.equals("")) {
                    element.setExport(exportName);
                }
                if (!expType.equals("")) {
                    element.setExportType(expType);
                }
                element.setxsdType(xsdType);
                break;
            }
        }
    }

    public void setResourceValues(List<RDFResource> resList, String resName, String rdfRefURI,
                                  String dataServiceResourceName, String requiredRoles, String xsdType ) {
        for (RDFResource rdfResource : resList) {
            if (rdfResource.getName().equals(resName)) {

                rdfResource.setRdfRefURI(rdfRefURI);
                rdfResource.setName(dataServiceResourceName);
                //if (!requiredRoles.equals("")) {
                rdfResource.setRequiredRoles(requiredRoles);
                //}
                rdfResource.setxsdType(xsdType);
                break;
            }
        }
    }

    public void setAttributeValues(List<Attribute> attributeList, String attributeName,
                                   String dsType, String dsValue, String reqRoles, String exportName,
                                   String expType, String xsdType, String dataServiceOMElementName,
                                   String arrayName, HttpServletRequest request, String optional) {
        boolean isAttributeNameExist = false;
        if (!dataServiceOMElementName.equals(attributeName)) {
            //check new attribute name already exist in the attribute list
            for (Attribute attribute : attributeList) {
               if (attribute.getName().equals(dataServiceOMElementName)) {
                   isAttributeNameExist = true;
                   break;
               }
            }
        }
        if (!isAttributeNameExist) {
            for (Attribute attribute : attributeList) {
                if (attribute.getName().equals(attributeName) &&
                        (dataServiceOMElementName.equals(attributeName)
                                || !attribute.getName().equals(dataServiceOMElementName))) {
                    attribute.setName(dataServiceOMElementName);
                    attribute.setDataSourceType(dsType);
                    attribute.setDataSourceValue(dsValue);
                    attribute.setOptional(optional);
                    //if (!reqRoles.equals("")) {
                        attribute.setRequiredRoles(reqRoles);
                    //}
                    if (!exportName.equals("")) {
                        attribute.setExport(exportName);
                    }
                    if (!expType.equals("")) {
                        attribute.setExportType(expType);
                    }
                    attribute.setxsdType(xsdType);
                    if (!"".equals(arrayName)) {
                        attribute.setArrayName(arrayName);
                    }
                    break;
                }
            }
        } else {
            String message = "Attribute name "+ dataServiceOMElementName + " already exist.";
            CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        }
    }

    public void setQueryValues(List<CallQuery> callQueryList, String href, String selectedQuery,
                               Query selectedQueryObj, String dataServiceOMElementName,
                               HttpServletRequest request, String requiredRoles) {
        for (CallQuery callQuery : callQueryList) {
            if (callQuery.getHref().equals(href)) {
                callQuery.setHref(selectedQuery);

                Param[] params = selectedQueryObj.getParams();
                List<Param> paramList = new ArrayList<Param>();
                List<WithParam> withParamsList = new ArrayList<WithParam>();
                if (params != null && params.length != 0) {
                    for (int a = 0; a < params.length; a++) {
                        Param param = params[a];
                        paramList.add(param);
                        String paramName = request.getParameter(param.getName());
                        String paramType = request.getParameter("MappingType"
                                + param.getName());
                        paramName = (paramName == null) ? param.getName()
                                : paramName;
                        paramType = (paramType == null) ? "query-param"
                                : paramType;
                        WithParam withParam = new WithParam(
                                dataServiceOMElementName, paramName,
                                paramType);
                        withParam.setName(param.getName());
                        withParam.setParamValue(paramName);
                        withParam.setParamType(paramType);
                        withParamsList.add(withParam);
                    }
                    callQuery.setWithParams(withParamsList);
                }
                //if (!requiredRoles.equals("")) {
                callQuery.setRequiredRoles(requiredRoles);
                //}
                break;
            }
        }
    }

     
%>
<%
    //retrieve form values set in addInputMapping.jsp page

    String queryId = request.getParameter("queryId");
    String dataServiceOMType = request.getParameter("cmbDataServiceOMType");
    String dataServiceOMElementName = request.getParameter("txtDataServiceOMElementName");
    String complexElementName = request.getParameter("txtDataServiceComplexElementName");
    String complexElementNamespace = request
            .getParameter("txtDataServiceComplexElementNamespace");
    String dataServiceResourceName = request.getParameter("txtDataServiceResourceName");
    String datasourceType = request.getParameter("datasourceType");
    String dataSourceValue = request.getParameter("datasourceValue1");
    /* this is to handle both column and query-param values */
    if (dataSourceValue == null || "".equals(dataSourceValue)) {
    	dataSourceValue = request.getParameter("datasourceValue2");
    }
    /* this is to handle rdf element datasourceValue */
    if (dataSourceValue == null || "".equals(dataSourceValue)) {
        dataSourceValue = request.getParameter("datasourceValue");
    }
    String elementNamespace = request.getParameter("txtDataServiceElementNamespace");
    String rdfRefURI = request.getParameter("txtrdfRefURI");
    String selectedQuery = request.getParameter("cmbDataServiceQueryId");
    String xsdType = request.getParameter("xsdType");
    String exportName = request.getParameter("exportName");
    String exportType = request.getParameter("exportType");
    String edit = request.getParameter("edit");
    String action = request.getParameter("action");
    String flag = request.getParameter("flag");
    String complexElementId = request.getParameter("complexElementId");
    String complexPath = request.getParameter("complexPath");
    complexPath = (complexPath == null) ? "" : complexPath;
    String editMappingType = request.getParameter("mappingType");
    String useColumnNumbers = request.getParameter("useColumnNumbers");
    String optional = request.getParameter("optional");
    optional = (optional == null) ? "false" : "true";
    String forwardTo = "";
    String parameterType = request.getParameter("paramType");
    String arrayName = "";
    if (request.getParameter("arrayName") != null) {
        arrayName = request.getParameter("arrayName");
    } else if (request.getParameter("arrayName1") != null) {
        arrayName = request.getParameter("arrayName1");
    }

    boolean add = true;
    String requiredRoles = "";
    String allowedRoles = "";
    flag = (flag == null) ? "" : flag;
    action = (action == null) ? "" : action;
    elementNamespace = (elementNamespace == null) ? "" : elementNamespace;
    //complexPath = (complexPath == null) ? "" : complexPath;
    complexElementId = (complexElementId == null) ? "" : complexElementId;
    edit = (edit == null) ? "" : edit;
    complexElementNamespace = (complexElementNamespace == null) ? "" : complexElementNamespace;
    selectedQuery = (selectedQuery == null) ? "" : selectedQuery;
    editMappingType = (editMappingType == null) ? "" : editMappingType;
    dataServiceOMElementName = (dataServiceOMElementName == null) ? ""
            : dataServiceOMElementName;
    dataServiceResourceName = (dataServiceResourceName == null) ? ""
            : dataServiceResourceName;
    parameterType = (parameterType == null) ? "" : parameterType;
    arrayName = (arrayName == null) ? "" : arrayName;
    String[] userRoles;
    String backendServerURL = CarbonUIUtil
            .getServerURL(config.getServletContext(), session);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
            .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    DataServiceAdminClient client = new DataServiceAdminClient(cookie, backendServerURL, configContext);
    userRoles = client.getAllRoles(dataService.getAuthProvider());



    //for(String name : userRoles){
    for (String roleName : userRoles) {
        allowedRoles = request.getParameter(roleName);
        if (allowedRoles != null) {
            requiredRoles += roleName;
            if (userRoles.length != 0) {
                requiredRoles += ",";
            }

        }
    }
    if (complexElementName != null && !complexElementName.equals("")) {
        if (!(complexPath.equals("") && edit.equals("delete"))) {
            complexPath = complexPath + "/" + complexElementName;
        }
    }
    if (requiredRoles.endsWith(",")) {
        requiredRoles = requiredRoles.substring(0, requiredRoles.length() - 1);
    }

    if (queryId != null) {
        if (!dataServiceOMType.equals("")) {
            Query query = dataService.getQuery(queryId);
            Query selectedQueryObj = dataService.getQuery(selectedQuery);
            Result res = query.getResult();
            Element element = new Element();
            ComplexElement complexElement;
            Attribute attr = new Attribute();
            RDFResource rdfResource = new RDFResource();
            CallQuery callQuery = new CallQuery();
            String[] dsOMElementName;
            String[] dsOMElementColumnName;
            if (res != null) {
                if (res.getDisplayColumnNames() == null
                        || res.getElementLocalNames() == null) {
                    dsOMElementName = new String[1];
                    dsOMElementColumnName = new String[1];
                } else {
                    dsOMElementName = new String[res.getDisplayColumnNames().length + 1];
                    dsOMElementColumnName = new String[res.getElementLocalNames().length + 1];
                }

                if (dataServiceOMType.equals("element")) {
                    if (res.getElements().size() != 0) {
                        List<Element> elementsList = res.getElements();
                        if (edit.equals("delete")) {
                            if (!dataServiceOMElementName.equals("")) {
                                if (complexPath != null && !complexPath.equals("")) {
                                    res.getChild(complexPath).removeElement(
                                            dataServiceOMElementName);
                                } else {
                                    res.removeElement(dataServiceOMElementName);
                                }
                            }
                        } else {
                            boolean editFlag = true;
                            if (editMappingType != null && (!editMappingType.equals(""))) {
                                editFlag = false;
                                if (!complexPath.equals("")) {
                                    ComplexElement rootLevel = res.getChild(complexPath);
                                    setElementValues(rootLevel.getElements(), edit, datasourceType,
                                            dataSourceValue, elementNamespace, requiredRoles, exportName,
                                            exportType, xsdType, dataServiceOMElementName, arrayName, request, optional);
                                } else {
                                    setElementValues(res.getElements(), edit, datasourceType,
                                            dataSourceValue, elementNamespace, requiredRoles, exportName,
                                            exportType, xsdType, dataServiceOMElementName, arrayName, request, optional);
                                }
                            }
                            if (editFlag) {
                                element.setDataSourceType(datasourceType);
                                element.setDataSourceValue(dataSourceValue);
                                element.setNamespace(elementNamespace);
                                element.setName(dataServiceOMElementName);
                                element.setOptional(optional);

                                if (!"".equals(arrayName)) {
                                    element.setArrayName(arrayName);
                                }
                                //if (!requiredRoles.equals("")) {
                                    element.setRequiredRoles(requiredRoles);
                                //}
                                if (!exportName.equals("")) {
                                    element.setExport(exportName);
                                }
                                if (!exportType.equals("")) {
                                    element.setExportType(exportType);
                                }
                                element.setxsdType(xsdType);
                                if (!complexPath.equals("")) {
                                    ComplexElement rootLevel = res.getChild(complexPath);
                                    rootLevel.addElement(element);
                                } else {
                                    elementsList.add(element);
                                    res.setElements(elementsList);
                                }
                            }
                            res.setElementLocalNames(dsOMElementName);
                            res.setDisplayColumnNames(dsOMElementColumnName);
                            res.setResultSetColumnNames(dsOMElementColumnName);
                        }
                    } else if (edit.equals("delete")){ //delete nested element of complex element when dataservice hasn't saved yet.
                    	if (!dataServiceOMElementName.equals("")) {
                            if (complexPath != null && !complexPath.equals("")) {
                                res.getChild(complexPath).removeElement(
                                            dataServiceOMElementName);
                        } else {
                                res.removeElement(dataServiceOMElementName);
                            }
                        }
                    } else {
                        List<Element> elementsList = new ArrayList<Element>();
                        dsOMElementName[0] = dataServiceOMElementName;
                        dsOMElementColumnName[0] = dataSourceValue;
                        if (dataServiceOMType.equals("element")) {
                            element.setDataSourceType(datasourceType);
                            element.setDataSourceValue(dataSourceValue);
                            element.setNamespace(elementNamespace);
                            element.setName(dataServiceOMElementName);
                            element.setOptional(optional);
                            //if (!requiredRoles.equals("")) {
                                element.setRequiredRoles(requiredRoles);
                            //}
                            element.setxsdType(xsdType);
                            if (exportName != null && !exportName.equals("")) {
                                element.setExport(exportName);
                            }
                            if (exportType != null && !exportType.equals("")) {
                                element.setExportType(exportType);
                            }
                            if (!"".equals(arrayName)) {
                                element.setArrayName(arrayName);
                            }
                            if (!complexPath.equals("")) {
                                ComplexElement rootLevel = res.getChild(complexPath);
                                rootLevel.addElement(element);

                            } else {
                                if (!elementsList.contains(element)) {
                                  elementsList.add(element);
                                }
                                res.setElements(elementsList);
                            }
                            res.setElementLocalNames(dsOMElementName);
                            res.setDisplayColumnNames(dsOMElementColumnName);
                            res.setResultSetColumnNames(dsOMElementColumnName);
                        }
                    }
                } else if (dataServiceOMType.equals("complexType")) {
                    if (res.getComplexElements().size() != 0) {
                        if (edit.equals("delete")) {
                            if (!complexElementName.equals("")) {
                                if (complexPath != null && !complexPath.equals("")) {
                                    res.getChild(res.getComplexElements(),
                                            getSubComplexElementList(complexPath))
                                            .removeComplexElement(complexElementName);
                                    // once a complex element is deleted it will go to its parent level
                                    complexPath = getSubComplexPath(complexPath);
                                } else {
                                    res.removeComplexElement(complexElementName);
                                }
                            }
                        } else {
                            // when editing a complex type in non root level
                            if (editMappingType.equals("complexType")) {
                                ComplexElement editComplexEle;
                                if (!complexPath.equals("")
                                        && res.getTokens(complexPath).size() > 1) {
                                    editComplexEle = res.getChild(res.getComplexElements(),
                                            getSubComplexElementList(complexPath))
                                            .getComplexElement(edit);
                                } else {
                                    editComplexEle = res.getComplexElement(edit);
                                }
                                editComplexEle.setNamespace(complexElementNamespace);
                                editComplexEle.setName(complexElementName);
                                if (!"".equals(arrayName)) {
                                    editComplexEle.setArrayName(arrayName);
                                }
                            } else {
                                // when adding to the already exsisting list
                                List<ComplexElement> complexElementsList = res
                                        .getComplexElements();
                                if (action.equals("addbutton")) {
                                    String message = "Cannot add Complex Element to the result without child elements";
                                    CarbonUIMessage.sendCarbonUIMessage(message,
                                            CarbonUIMessage.ERROR, request);
                                    forwardTo = "addOutputMapping.jsp?queryId=" + queryId
                                            + "&txtDataServiceComplexElementName="
                                            + complexElementName
                                            + "&txtDataServiceComplexElementNamespace="
                                            + complexElementNamespace + "&complexPath="
                                            + getSubComplexPath(complexPath)
                                            + "&mappingType=complexType" + "&flag=complexError";
                                    flag = "error";
                                    add = false;
                                }
                                complexElement = new ComplexElement();
                                complexElement.setName(complexElementName);
                                complexElement.setNamespace(complexElementNamespace);
                                if (!"".equals(arrayName)) {
                                    complexElement.setArrayName(arrayName);
                                }

                                if (!complexPath.equals("")
                                        && res.getTokens(complexPath).size() > 1) {
                                    ComplexElement parentEle = res.getChild(
                                            res.getComplexElements(),
                                            getSubComplexElementList(complexPath));
                                    //res.removeComplexElement(parentEle.getName());
                                    if (add) {
                                        parentEle.addComplexElement(complexElement);
                                    }
                                } else {
                                    //res.addComplexElement(complexElement);
                                    if (add) {
                                        complexElementsList.add(complexElement);
                                    }
                                }
                            }

                        }
                    } else {
                        List<ComplexElement> complexElementsList = res.getComplexElements();
                        dsOMElementName[0] = dataServiceOMElementName;
                        dsOMElementColumnName[0] = dataSourceValue;
                        if (dataServiceOMType.equals("complexType")) {
                            if (editMappingType.equals("complexType")) {
                                ComplexElement editComplexEle;
                                if (!complexPath.equals("")
                                        && res.getTokens(complexPath).size() > 1) {
                                    editComplexEle = res.getChild(res.getComplexElements(),
                                            getSubComplexElementList(complexPath))
                                            .getComplexElement(edit);
                                } else {
                                    editComplexEle = res.getComplexElement(edit);
                                }
                                editComplexEle.setNamespace(complexElementNamespace);
                                editComplexEle.setName(complexElementName);
                                if (!"".equals(arrayName)) {
                                    editComplexEle.setArrayName(arrayName);
                                }
                            } else {
                                // when adding as the first Complex Element to the result
                                if (action.equals("addbutton")) {
                                    String message = "Cannot add Complex Element to the result without child elements";
                                    CarbonUIMessage.sendCarbonUIMessage(message,
                                            CarbonUIMessage.ERROR, request);
                                    forwardTo = "addOutputMapping.jsp?queryId=" + queryId
                                            + "&txtDataServiceComplexElementName="
                                            + complexElementName
                                            + "&txtDataServiceComplexElementNamespace="
                                            + complexElementNamespace + "&complexPath="
                                            + getSubComplexPath(complexPath)
                                            + "&mappingType=complexType" + "&flag=complexError";
                                    flag = "error";
                                    add = false;
                                }
                                complexElement = new ComplexElement();
                                complexElement.setName(complexElementName);
                                complexElement.setNamespace(complexElementNamespace);
                                if (!"".equals(arrayName)) {
                                    complexElement.setArrayName(arrayName);
                                }
                                if (!complexPath.equals("")
                                        && res.getTokens(complexPath).size() > 1) {
                                    ComplexElement parentEle = res.getChild(
                                            res.getComplexElements(),
                                            getSubComplexElementList(complexPath));
                                    // res.removeComplexElement(parentEle.getName());
                                    if (add) {
                                        parentEle.addComplexElement(complexElement);
                                    }
                                } else {
                                    //res.addComplexElement(complexElement);
                                    if (add) {
                                        complexElementsList.add(complexElement);
                                    }
                                }
                                res.setDisplayColumnNames(dsOMElementColumnName);
                                res.setResultSetColumnNames(dsOMElementColumnName);
                            }

                        }
                    }
                } else if (dataServiceOMType.equals("resource")) {
                    if (res.getResources().size() != 0) {
                        List<RDFResource> resources = res.getResources();
                        if (edit.equals("delete")) {
                            if (!dataServiceResourceName.equals("")) {
                                res.removeResource(dataServiceResourceName);
                            }
                        } else {
                            if (editMappingType != null && (!editMappingType.equals(""))) {
                                setResourceValues(resources,edit,rdfRefURI,dataServiceResourceName,requiredRoles,xsdType);
                            } else {
                                rdfResource.setRdfRefURI(rdfRefURI);
                                rdfResource.setName(dataServiceResourceName);
                                //if (!requiredRoles.equals("")) {
                                rdfResource.setRequiredRoles(requiredRoles);
                                //}
                                rdfResource.setxsdType(xsdType);
                                resources.add(rdfResource);
                                res.setResources(resources);
                                res.setElementLocalNames(dsOMElementName);
                                res.setDisplayColumnNames(dsOMElementColumnName);
                                res.setResultSetColumnNames(dsOMElementColumnName);
                            }
                        }
                    } else {
                        // Adding first rdf-resource
                        List<RDFResource> resources = new ArrayList<RDFResource>();
                        dsOMElementName[0] = dataServiceResourceName;
                        dsOMElementColumnName[0] = dataSourceValue;
                        if (dataServiceOMType.equals("resource")) {
                            rdfResource.setRdfRefURI(rdfRefURI);
                            rdfResource.setName(dataServiceResourceName);
                            //if (!requiredRoles.equals("")) {
                                rdfResource.setRequiredRoles(requiredRoles);
                            //}
                            rdfResource.setxsdType(xsdType);
                            resources.add(rdfResource);
                            res.setResources(resources);
                            res.setElementLocalNames(dsOMElementName);
                            res.setDisplayColumnNames(dsOMElementColumnName);
                            res.setResultSetColumnNames(dsOMElementColumnName);
                        }
                    }
                } else if (dataServiceOMType.equals("attribute")) {
                    if (res.getAttributes().size() != 0) {
                        List<Attribute> attributes = res.getAttributes();
                        if (edit.equals("delete")) {
                            if (!dataServiceOMElementName.equals("")) {
                                if (complexPath != null && !complexPath.equals("")) {
                                    res.getChild(complexPath).removeComplexElement(
                                            dataServiceOMElementName);
                                } else {
                                    res.removeAttribute(dataServiceOMElementName);
                                }
                            }
                        } else {
                            boolean editFlag = true;
                            if (editMappingType != null && (!editMappingType.equals(""))) {
                                editFlag = false;

                                if (!complexPath.equals("")) {
                                    ComplexElement rootLevel = res.getChild(complexPath);
                                    setAttributeValues(rootLevel.getAttributes(), edit, datasourceType,
                                            dataSourceValue, requiredRoles, exportName,
                                            exportType, xsdType, dataServiceOMElementName, arrayName, request, optional);
                                } else {
                                    setAttributeValues(res.getAttributes(), edit, datasourceType,
                                            dataSourceValue, requiredRoles, exportName,
                                            exportType, xsdType, dataServiceOMElementName, arrayName, request, optional);
                                }
                            }
                            if (editMappingType != null && (!editMappingType.equals("")) && !editMappingType.equals(dataServiceOMType)) {

                            }


                            if (editFlag) {
                                attr.setDataSourceType(datasourceType);
                                attr.setDataSourceValue(dataSourceValue);
                                attr.setName(dataServiceOMElementName);
                                //if (!requiredRoles.equals("")) {
                                    attr.setRequiredRoles(requiredRoles);
                                //}
                                attr.setxsdType(xsdType);
                                attr.setOptional(optional);
                                if (!exportName.equals("")) {
                                    attr.setExport(exportName);
                                }
                                if (!exportType.equals("")) {
                                    attr.setExportType(exportType);
                                }
                                if (!"".equals(arrayName)) {
                                    attr.setArrayName(arrayName);
                                }
                                if (!complexPath.equals("")) {
                                    ComplexElement rootLevel = res.getChild(complexPath);
                                    if (!rootLevel.getAttributes().contains(attr)) {
                                        rootLevel.addAttribute(attr);
                                    } else {
                                         String message = "Attribute name "+ dataServiceOMElementName +
                                                 " already exist.";
                                         CarbonUIMessage.sendCarbonUIMessage(
                                                 message, CarbonUIMessage.ERROR, request);
                                    }
                                } else {
                                    if (!attributes.contains(attr)) {
                                        attributes.add(attr);
                                        res.setAttributes(attributes);
                                    } else {
                                        String message = "Attribute name "+ dataServiceOMElementName +
                                                " already exist.";
                                        CarbonUIMessage.sendCarbonUIMessage(
                                                message, CarbonUIMessage.ERROR, request);
                                    }

                                }
                                
                                res.setElementLocalNames(dsOMElementName);
                                res.setDisplayColumnNames(dsOMElementColumnName);
                                res.setResultSetColumnNames(dsOMElementColumnName);
                            }
                        }
                    } else {
                        // Adding first attribute
                        List<Attribute> attributes = new ArrayList<Attribute>();

                        dsOMElementName[0] = dataServiceOMElementName;
                        dsOMElementColumnName[0] = dataSourceValue;
                        if (dataServiceOMType.equals("attribute")) {
                            attr.setDataSourceType(datasourceType);
                            attr.setDataSourceValue(dataSourceValue);
                            attr.setName(dataServiceOMElementName);
                            //if (!requiredRoles.equals("")) {
                                attr.setRequiredRoles(requiredRoles);
                            //}
                            attr.setxsdType(xsdType);
                            attr.setOptional(optional);
                            if (!exportName.equals("")) {
                                attr.setExport(exportName);
                            }
                            if (!exportType.equals("")) {
                                attr.setExportType(exportType);
                            }
                            if (!"".equals(arrayName)) {
                                    attr.setArrayName(arrayName);
                            }
                            if (!complexPath.equals("")) {
                                ComplexElement rootLevel = res.getChild(complexPath);
                                rootLevel.addAttribute(attr);
                            } else {
                                attributes.add(attr);
                                res.setAttributes(attributes);
                            }

                            res.setElementLocalNames(dsOMElementName);
                            res.setDisplayColumnNames(dsOMElementColumnName);
                            res.setResultSetColumnNames(dsOMElementColumnName);
                        }
                    }
                } else if (dataServiceOMType.equals("query")) {
                    if (res.getCallQueries().size() != 0) {
                        List<CallQuery> callqueryList = res.getCallQueries();
                        if (edit.equals("delete")) {
                            if (complexPath != null && !complexPath.equals("")) {
                                res.getChild(complexPath).removeComplexElement(
                                        selectedQuery);
                            } else {
                                res.removeCallQuery(selectedQuery);
                            }

                        } else {
                            boolean editFlag = true;
                            if (!edit.equals("")) {
                                if (editMappingType.equals("query") && !edit.equals("delete")) {
                                    editFlag = false;
                                    if (!complexPath.equals("") && complexPath != null) {
                                        ComplexElement rootLevel = res.getChild(complexPath);
                                        setQueryValues(rootLevel.getCallQueries(), edit, selectedQuery,
                                                selectedQueryObj, dataServiceOMElementName, request, requiredRoles);
                                    } else {
                                        setQueryValues(res.getCallQueries(), edit, selectedQuery,
                                                selectedQueryObj, dataServiceOMElementName, request, requiredRoles);
                                    }
                                }
                            }

                            if (editFlag) {
                                callQuery.setHref(selectedQuery);
                                Param[] params = selectedQueryObj.getParams();
                                List<Param> paramList = new ArrayList<Param>();
                                List<WithParam> withParamsList = new ArrayList<WithParam>();
                                if (params != null && params.length != 0) {
                                    for (int a = 0; a < params.length; a++) {
                                        Param param = params[a];
                                        paramList.add(param);
                                        String paramName = request.getParameter(param
                                                .getName());
                                        String paramType = request
                                                .getParameter("MappingType"
                                                        + param.getName());
                                        paramName = (paramName == null) ? param.getName()
                                                : paramName;
                                        paramType = (paramType == null) ? "query-param"
                                                : paramType;
                                        WithParam withParam = new WithParam(
                                                dataServiceOMElementName, paramName,
                                                paramType);
                                        withParam.setName(param.getName());
                                        withParam.setParamValue(paramName);
                                        withParam.setParamType(paramType);
                                        withParamsList.add(withParam);
                                    }
                                    callQuery.setWithParams(withParamsList);
                                }
                                //if (!requiredRoles.equals("")) {
                                    callQuery.setRequiredRoles(requiredRoles);
                                //}
                                if (!complexPath.equals("")) {
                                    ComplexElement rootLevel = res.getChild(complexPath);
                                    rootLevel.addCallQuery(callQuery);
                                } else {
                                    callqueryList.add(callQuery);
                                    res.setCallQueries(callqueryList);
                                }
                                res.setElementLocalNames(dsOMElementName);
                                res.setDisplayColumnNames(dsOMElementColumnName);
                            }
                        }
                    }

                    //res.setResultSetColumnNames(dsOMElementColumnName);
                    else {
                        // Adding the first query
                        List<CallQuery> callqueryList = new ArrayList<CallQuery>();
                        dsOMElementName[0] = dataServiceOMElementName;
                        dsOMElementColumnName[0] = dataSourceValue;
                        if (dataServiceOMType.equals("query")) {
                            callQuery.setHref(selectedQuery);
                            //Param param = query.getParam(queryId);
                            //Param[] params = query.getParams();
                            Param[] params = selectedQueryObj.getParams();
                            List<Param> paramList = new ArrayList<Param>();
                            List<WithParam> withParamsList = new ArrayList<WithParam>();
                            if (params != null) {
                                for (int a = 0; a < params.length; a++) {
                                    Param param = params[a];
                                    paramList.add(param);
                                    String paramName = request
                                            .getParameter(param.getName());
                                    String paramType = request.getParameter("MappingType"
                                            + param.getName());
                                    paramName = (paramName == null) ? param.getName()
                                            : paramName;
                                    paramType = (paramType == null) ? "query-param"
                                            : paramType;
                                    WithParam withParam = new WithParam(
                                            dataServiceOMElementName, paramName, paramType);
                                    withParam.setName(param.getName());
                                    withParam.setParamValue(paramName);
                                    withParam.setParamType(paramType);
                                    withParamsList.add(withParam);
                                }
                                callQuery.setWithParams(withParamsList);
                            }
                            //if (!requiredRoles.equals("")) {
                                callQuery.setRequiredRoles(requiredRoles);
                            //}
                            if (!complexPath.equals("")) {
                                ComplexElement rootLevel = res.getChild(complexPath);
                                rootLevel.addCallQuery(callQuery);
                            } else {
                                callqueryList.add(callQuery);
                                res.setCallQueries(callqueryList);

                            }
                            res.setElementLocalNames(dsOMElementName);
                            res.setDisplayColumnNames(dsOMElementColumnName);
                            //res.setResultSetColumnNames(dsOMElementColumnName);
                        }
                    }
                }
            }
        }
    }
%>
<input type="hidden" id="complexElementName" name="complexElementName"
       value="<%=complexElementName%>"/>
<input value="<%=complexPath%>" name="complexPath" id="complexPath" size="30" type="hidden"/>
<input type="hidden" id="complexElementId" name="complexElementId" value="<%=complexElementId%>"/>
<input type="hidden" id="editFlag" name="editFlag" value="<%=edit%>"/>
<input type="hidden" id="flag" name="flag" value="<%=flag%>"/>
<input type="hidden" id="complexMappingId" name="complexMappingId" value="<%=dataServiceOMType%>"/>
<input type="hidden" id="useColumnNumbersId" name="useColumnNumbersId" value="<%=useColumnNumbers%>"/>
<script type="text/javascript">
    if ((document.getElementById('flag').value == 'save') || (document.getElementById('flag').value == 'saverdf')) {
        location.href = "addQuery.jsp?queryId=<%=queryId%>&useColumnNumbers=<%=useColumnNumbers%>";
    } else if (document.getElementById('flag').value == 'add' || document.getElementById('flag').value == 'edit') {
        location.href = "addOutputMapping.jsp?complexPath=<%=complexPath%>&queryId=<%=queryId%>&complexMappingId=<%=dataServiceOMType%>&useColumnNumbers=<%=useColumnNumbers%>&arrayName=<%=arrayName%>&optional=<%=optional%>";
    } else if (document.getElementById('flag').value == 'addrdf' || document.getElementById('flag').value == 'editrdf') {
        location.href = "addRDFOutputMapping.jsp?queryId=<%=queryId%>&useColumnNumbers=<%=useColumnNumbers%>";
    } else if (document.getElementById('flag').value == 'error') {
        location.href = '<%=forwardTo%>';
    } else if (document.getElementById('complexElementName').value != null) {
        location.href = "addOutputMapping.jsp?isComplexElement=true&complexPath=<%=complexPath%>&queryId=<%=queryId%>&complexElementId=<%=complexElementName%>&useColumnNumbers=<%=useColumnNumbers%>&arrayName=<%=arrayName%>&optional=<%=optional%>%>";
    }
</script>