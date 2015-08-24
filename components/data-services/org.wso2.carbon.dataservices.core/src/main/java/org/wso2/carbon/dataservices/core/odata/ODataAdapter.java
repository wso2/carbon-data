/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.dataservices.core.odata;

import org.apache.axis2.databinding.utils.ConverterUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmKeyPropertyRef;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.constants.EdmTypeKind;
import org.apache.olingo.commons.api.edm.provider.CsdlEdmProvider;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ODataTranslatedException;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.core.ContentNegotiatorException;
import org.apache.olingo.server.core.ServiceHandler;
import org.apache.olingo.server.core.requests.ActionRequest;
import org.apache.olingo.server.core.requests.DataRequest;
import org.apache.olingo.server.core.requests.FunctionRequest;
import org.apache.olingo.server.core.requests.MediaRequest;
import org.apache.olingo.server.core.requests.MetadataRequest;
import org.apache.olingo.server.core.requests.ServiceDocumentRequest;
import org.apache.olingo.server.core.responses.CountResponse;
import org.apache.olingo.server.core.responses.EntityResponse;
import org.apache.olingo.server.core.responses.EntitySetResponse;
import org.apache.olingo.server.core.responses.MetadataResponse;
import org.apache.olingo.server.core.responses.NoContentResponse;
import org.apache.olingo.server.core.responses.PrimitiveValueResponse;
import org.apache.olingo.server.core.responses.PropertyResponse;
import org.apache.olingo.server.core.responses.ServiceDocumentResponse;
import org.apache.olingo.server.core.responses.ServiceResponse;
import org.apache.olingo.server.core.responses.ServiceResponseVisior;
import org.apache.olingo.server.core.responses.StreamResponse;
import org.apache.olingo.server.core.uri.parser.UriParserException;
import org.wso2.carbon.dataservices.common.DBConstants;
import org.wso2.carbon.dataservices.core.DataServiceFault;
import org.wso2.carbon.dataservices.core.engine.DataEntry;
import org.wso2.carbon.dataservices.core.engine.ParamValue;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * This class implements the olingo serviceHandler to process requests and response.
 *
 * @see ServiceHandler
 */
public class ODataAdapter implements ServiceHandler {

	/**
	 * Service metadata of the odata service.
	 */
	private ServiceMetadata serviceMetadata;

	/**
	 * OData handler of the odata service.
	 */
	private final ODataDataHandler dataHandler;

	/**
	 * EDM provider of the odata service.
	 */
	private CsdlEdmProvider edmProvider;

	/**
	 * Namespace of the data service.
	 */
	private String namespace;

	public ODataAdapter(ODataDataHandler dataHandler, String namespace, String configID) throws DataServiceFault {
		this.dataHandler = dataHandler;
		this.namespace = namespace;
		this.edmProvider = initializeEdmProvider(configID);
	}

	@Override
	public void init(OData odata, ServiceMetadata serviceMetadata) {
		this.serviceMetadata = serviceMetadata;
	}

	@Override
	public void readMetadata(MetadataRequest request, MetadataResponse response)
			throws ODataApplicationException, ODataTranslatedException {
		response.writeMetadata();
	}

	@Override
	public void readServiceDocument(ServiceDocumentRequest request, ServiceDocumentResponse response)
			throws ODataApplicationException, ODataTranslatedException {
		response.writeServiceDocument(request.getODataRequest().getRawBaseUri());
	}

	private static class EntityDetails {
		EntityCollection entitySet = null;
		Entity entity = null;
		EdmEntityType entityType;
		String navigationProperty;
		Entity parentEntity = null;
	}

	/**
	 * This method process the request.
	 *
	 * @param request DataRequest
	 * @return EntityDetails
	 * @throws ODataApplicationException
	 */
	private EntityDetails process(final DataRequest request) throws ODataApplicationException {
		EntityCollection entitySet = null;
		Entity entity = null;
		EdmEntityType entityType;
		Entity parentEntity = null;
		EntityDetails details = new EntityDetails();

		try {
			if (request.isSingleton()) {
				throw new ODataApplicationException("Singletons are not supported", 501, Locale.ENGLISH);
			} else {
				final EdmEntitySet edmEntitySet = request.getEntitySet();
				entityType = edmEntitySet.getEntityType();
				List<UriParameter> keys = request.getKeyPredicates();
				String eTag = request.getETag();
				if (keys != null && !keys.isEmpty()) {
					entity = getEntity(entityType, eTag, keys);
				} else {
					int skip = 0;
					if (request.getUriInfo().getSkipTokenOption() != null) {
						skip = Integer.parseInt(request.getUriInfo().getSkipTokenOption().getValue());
					}
					int pageSize = getPageSize(request);
					entitySet = getEntityCollection(edmEntitySet.getName(), skip, pageSize);
					if (entitySet != null) {
						if (entitySet.getEntities().size() == pageSize) {
							try {
								entitySet
										.setNext(new URI(request.getODataRequest().getRawRequestUri() + "?$skiptoken=" +
										                 (skip + pageSize)));
							} catch (URISyntaxException e) {
								throw new ODataApplicationException(e.getMessage(), 500, Locale.ENGLISH);
							}
						}
					}
				}
			}
			if (!request.getNavigations().isEmpty() && entity != null) {
				UriResourceNavigation lastNavigation = request.getNavigations().getLast();
				for (UriResourceNavigation nav : request.getNavigations()) {
					if (nav.isCollection()) {
						entitySet = getNavigableEntitySet(serviceMetadata, entity, nav);
					} else {
						parentEntity = entity;
						entity = getNavigableEntity(serviceMetadata, entityType, parentEntity, nav);
					}
					entityType = nav.getProperty().getType();
				}
				details.navigationProperty = lastNavigation.getProperty().getName();
			}
			details.entity = entity;
			details.entitySet = entitySet;
			details.entityType = entityType;
			details.parentEntity = parentEntity;
			return details;
		} catch (DataServiceFault dataServiceFault) {
			throw new ODataApplicationException(dataServiceFault.getMessage(), 500, Locale.ENGLISH);
		}
	}

	@Override
	public <T extends ServiceResponse> void read(final DataRequest request, final T response)
			throws ODataApplicationException, ODataTranslatedException {

		final EntityDetails details = process(request);

		response.accepts(new ServiceResponseVisior() {
			@Override
			public void visit(CountResponse response) throws ODataApplicationException, SerializerException {
				response.writeCount(details.entitySet.getCount());
			}

			@Override
			public void visit(PrimitiveValueResponse response) throws ODataApplicationException, SerializerException {
				EdmProperty edmProperty = request.getUriResourceProperty().getProperty();
				Property property = details.entity.getProperty(edmProperty.getName());
				response.write(property.getValue());
			}

			@Override
			public void visit(PropertyResponse response) throws ODataApplicationException, SerializerException {
				EdmProperty edmProperty = request.getUriResourceProperty().getProperty();
				Property property = details.entity.getProperty(edmProperty.getName());
				response.writeProperty(edmProperty.getType(), property);
			}

			@Override public void visit(StreamResponse response) throws ODataApplicationException {
				EdmProperty edmProperty = request.getUriResourceProperty().getProperty();
				Property property = details.entity.getProperty(edmProperty.getName());
				response.writeStreamResponse(new ByteArrayInputStream((byte[]) property.getValue()),
				                             ContentType.APPLICATION_OCTET_STREAM);
			}

			@Override
			public void visit(EntitySetResponse response) throws ODataApplicationException, SerializerException {
				if (request.getPreference("odata.maxpagesize") != null) {
					response.writeHeader("Preference-Applied",
					                     "odata.maxpagesize=" + request.getPreference("odata.maxpagesize"));
				}
				if (details.entity == null && !request.getNavigations().isEmpty()) {
					response.writeReadEntitySet(details.entityType, new EntityCollection());
				} else {
					response.writeReadEntitySet(details.entityType, details.entitySet);
				}
			}

			@Override
			public void visit(EntityResponse response) throws ODataApplicationException, SerializerException {
				if (details.entity == null && !request.getNavigations().isEmpty()) {
					response.writeNoContent(true);
				} else {
					response.writeReadEntity(details.entityType, details.entity);
				}
			}
		});
	}

	/**
	 * This method returns page size. if the page size is not specified in the request, method will return default size 8 as page size.
	 *
	 * @param request DataRequest
	 * @return int page size
	 */
	private int getPageSize(DataRequest request) {
		String size = request.getPreference("odata.maxpagesize");
		if (size == null) {
			return 8;
		}
		return Integer.parseInt(size);
	}

	@Override
	public void createEntity(DataRequest request, Entity entity, EntityResponse response)
			throws ODataApplicationException, UriParserException {
		EdmEntitySet edmEntitySet = request.getEntitySet();
		String baseURL = request.getODataRequest().getRawBaseUri();
		try {
			Entity created = createEntityInTable(edmEntitySet.getEntityType().getName(), entity);
			created.setId(new URI(EntityResponse.buildLocation(baseURL, entity, edmEntitySet.getName(),
			                                                   edmEntitySet.getEntityType())));
			response.writeCreatedEntity(edmEntitySet, created);
		} catch (DataServiceFault | SerializerException | URISyntaxException e) {
			throw new ODataApplicationException(e.getMessage(), 500, Locale.ENGLISH);
		}
	}

	@Override
	public void updateEntity(DataRequest request, Entity changes, boolean merge, String eTag, EntityResponse response)
			throws ODataApplicationException {
		try {
			List<UriParameter> key = request.getKeyPredicates();
			EdmEntityType entityType = request.getEntitySet().getEntityType();
			EntityCollection set = createEntityCollectionFromDataEntryList(entityType.getName(), dataHandler
					.readTableWithKeys(entityType.getName(), wrapKeyParamToDataEntry(key)));
			Entity entity = getEntity(entityType, set, key, eTag);
			if (entity != null) {
				updateEntity(entityType, changes, entity, merge);
				response.writeUpdatedEntity();
			} else {
				response.writeNotFound(true);
			}
		} catch (DataServiceFault dataServiceFault) {
			response.writeNotModified();
		}
	}

	@Override
	public void deleteEntity(DataRequest request, String eTag, EntityResponse response)
			throws ODataApplicationException {
		try {
			List<UriParameter> key = request.getKeyPredicates();
			EdmEntityType entityType = request.getEntitySet().getEntityType();
			EntityCollection set = createEntityCollectionFromDataEntryList(entityType.getName(), dataHandler
					.readTableWithKeys(entityType.getName(), wrapKeyParamToDataEntry(key)));
			Entity entity = getEntity(entityType, set, key, eTag);
			if (entity != null) {
				dataHandler.deleteEntityInTable(entityType.getName(), wrapEntityToDataEntry(entity));
				response.writeDeletedEntityOrReference();
			} else {
				response.writeNotFound(true);
			}
		} catch (DataServiceFault dataServiceFault) {
			dataServiceFault.printStackTrace();
		}
	}

	@Override
	public void updateProperty(DataRequest request, final Property property, boolean merge, String entityETag,
	                           PropertyResponse response) throws ODataApplicationException, ContentNegotiatorException {
		if (property.isPrimitive()) {
			EdmEntityType type = request.getEntitySet().getEntityType();
			try {
				dataHandler.updatePropertyInTable(type.getName(), wrapPropertyToDataEntry(property));
				if (property.getValue() == null) {
					response.writePropertyDeleted();
				} else {
					response.writePropertyUpdated();
				}
			} catch (DataServiceFault dataServiceFault) {
				dataServiceFault.printStackTrace();
			}
		}
	}

	@Override
	public <T extends ServiceResponse> void invoke(FunctionRequest request, HttpMethod method, T response)
			throws ODataApplicationException {
		response.getODataResponse().setStatusCode(501);
	}

	@Override
	public <T extends ServiceResponse> void invoke(ActionRequest request, String eTag, T response)
			throws ODataApplicationException {
		response.getODataResponse().setStatusCode(501);
	}

	@Override
	public void readMediaStream(MediaRequest request, StreamResponse response)
			throws ODataApplicationException, ContentNegotiatorException {
		try {
			final EdmEntitySet edmEntitySet = request.getEntitySet();
			List<UriParameter> keys = request.getKeyPredicates();
			String eTag = request.getETag();
			InputStream contents = null;
			Entity entity = getEntity(edmEntitySet.getEntityType(), eTag, keys);
			for (Property property : entity.getProperties()) {
				if (property.getType().equals(EdmPrimitiveTypeKind.Stream.getFullQualifiedName()
				                                                         .getFullQualifiedNameAsString())) {
					String data = property.getValue().toString();
					contents = new FileInputStream(data);
					break;
				}
			}
			if (contents != null) {
				response.writeStreamResponse(contents, request.getResponseContentType());
			}
		} catch (DataServiceFault e) {
			throw new ODataApplicationException(e.getMessage(), 500, Locale.ENGLISH);
		} catch (FileNotFoundException e) {
			throw new ODataApplicationException("File not found", 500, Locale.ENGLISH);
		}
	}

	@Override
	public void upsertMediaStream(MediaRequest request, String entityETag, InputStream mediaContent,
	                              NoContentResponse response) throws ODataApplicationException {
		response.writeNotImplemented();
	}

	@Override
	public void upsertStreamProperty(DataRequest request, String entityETag, InputStream streamContent,
	                                 NoContentResponse response) throws ODataApplicationException {
		response.writeNotImplemented();
	}

	@Override
	public void addReference(DataRequest request, String entityETag, URI referenceId, NoContentResponse response)
			throws ODataApplicationException {
		response.writeNotImplemented();
	}

	@Override
	public void updateReference(DataRequest request, String entityETag, URI updateId, NoContentResponse response)
			throws ODataApplicationException {
		response.writeNotImplemented();
	}

	@Override
	public void deleteReference(DataRequest request, URI deleteId, String entityETag, NoContentResponse response)
			throws ODataApplicationException, UriParserException {
		response.writeNotImplemented();
	}

	@Override
	public void anyUnsupported(ODataRequest request, ODataResponse response) throws ODataApplicationException {
		response.setStatusCode(500);
	}

	@Override
	public String startTransaction() {
		return null;
	}

	@Override
	public void commit(String txnId) {
	}

	@Override
	public void rollback(String txnId) {
	}

	@Override
	public void crossJoin(DataRequest dataRequest, List<String> entitySetNames, ODataResponse response) {
		response.setStatusCode(200);
	}

	/**
	 * Returns entity collection from the data entry list to use in olingo.  .
	 *
	 * @param tableName Name of the table
	 * @param resultSet List of Data Entry
	 * @return Entity Collection
	 * @throws DataServiceFault
	 * @see EntityCollection
	 */
	private EntityCollection createEntityCollectionFromDataEntryList(String tableName, List<DataEntry> resultSet)
			throws DataServiceFault {
		EntityCollection entitySet = new EntityCollection();
		int count = 0;
		for (DataEntry entry : resultSet) {
			Entity entity = new Entity();
			for (DataColumn column : dataHandler.getTableMetadata().get(tableName).values()) {
				String columnName = column.getColumnName();
				entity.addProperty(createPrimitive(column.getColumnType(), columnName,
				                                   entry.getValue(columnName).getScalarValue()));
			}
			//Set Etag to the entity
			entity.setETag(entry.getValue("ETag").getScalarValue());
			entity.setType(new FullQualifiedName(namespace, tableName).getFullQualifiedNameAsString());
			entitySet.getEntities().add(entity);
			count++;
		}
		entitySet.setCount(count);
		return entitySet;
	}

	/**
	 * This method creates the entity in table by calling the insertEntityInTable method in ODataDataHandler.
	 * Entity object is wrapped to DataEntry before call the method.
	 *
	 * @param tableName Name of the table
	 * @param entity    Entity to create
	 * @return Created entity
	 * @throws DataServiceFault
	 * @see ODataDataHandler
	 * @see #wrapEntityToDataEntry(Entity)
	 */
	private Entity createEntityInTable(String tableName, Entity entity) throws DataServiceFault {
		String eTag = dataHandler.insertEntityInTable(tableName, wrapEntityToDataEntry(entity));
		entity.setETag(eTag);
		return entity;
	}

	/**
	 * This method wraps Entity object into DataEntry object.
	 *
	 * @param entity Entity
	 * @return DataEntry
	 * @see DataEntry
	 */
	private DataEntry wrapEntityToDataEntry(Entity entity) {
		DataEntry entry = new DataEntry();
		for (Property property : entity.getProperties()) {
			entry.addValue(property.getName(), new ParamValue(property.getValue().toString()));
		}
		return entry;
	}

	/**
	 * This method wraps list of properties into single DataEntry object.
	 *
	 * @param properties list of properties
	 * @return DataEntry
	 * @see DataEntry
	 * @see Property
	 */
	private DataEntry wrapPropertiesToDataEntry(List<Property> properties) {
		DataEntry entry = new DataEntry();
		for (Property property : properties) {
			entry.addValue(property.getName(), new ParamValue(property.getValue().toString()));
		}
		return entry;
	}

	/**
	 * This method wraps list of eir parameters into single Data Entry object.
	 *
	 * @param keys list of URI parameters
	 * @return DataEntry
	 * @see UriParameter
	 * @see DataEntry
	 */
	private DataEntry wrapKeyParamToDataEntry(List<UriParameter> keys) {
		DataEntry entry = new DataEntry();
		for (UriParameter key : keys) {
			String value = key.getText();
			if (value.startsWith("'") && value.endsWith("'")) {
				value = value.substring(1, value.length() - 1);
			}
			entry.addValue(key.getName(), new ParamValue(value));
		}
		return entry;
	}

	public CsdlEdmProvider getEdmProvider() {
		return edmProvider;
	}

	private byte[] getBytesFromBase64String(String base64Str) throws DataServiceFault {
		try {
			return Base64.decodeBase64(base64Str.getBytes(DBConstants.DEFAULT_CHAR_SET_TYPE));
		} catch (Exception e) {
			throw new DataServiceFault(e.getMessage());
		}
	}

	/**
	 * This method updates the entity to the table by invoking ODataDataHandler updateEntityInTable method.
	 *
	 * @param edmEntityType  EdmEntityType
	 * @param entity         entity with changes
	 * @param existingEntity existing entity
	 * @param merge          PUT/PATCH
	 * @throws ODataApplicationException
	 * @throws DataServiceFault
	 * @see ODataDataHandler#updateEntityInTable(String, DataEntry, DataEntry)
	 */
	private void updateEntity(EdmEntityType edmEntityType, Entity entity, Entity existingEntity, boolean merge)
			throws ODataApplicationException, DataServiceFault {
		/* loop over all properties and replace the values with the values of the given payload
		   Note: ignoring ComplexType, as we don't have it in wso2dss oData model */
		List<Property> existingProperties = existingEntity.getProperties();
		for (Property existingProp : existingProperties) {
			String propName = existingProp.getName();
			// ignore the key properties, they aren't updatable
			if (isKey(edmEntityType, propName)) {
				continue;
			}
			Property updateProperty = entity.getProperty(propName);
			// the request payload might not consider ALL properties, so it can be null
			if (updateProperty == null) {
				// if a property has NOT been added to the request payload
				// depending on the HttpMethod, our behavior is different
				if (merge) {
					// as of the OData spec, in case of PATCH, the existing property is not touched
					continue; // do nothing
				} else {
					// as of the OData spec, in case of PUT, the existing property is set to null (or to default value)
					existingProp.setValue(existingProp.getValueType(), null);
					continue;
				}
			}
			// change the value of the properties
			existingProp.setValue(existingProp.getValueType(), updateProperty.getValue());
		}
		// write to the DB
		dataHandler.updateEntityInTable(edmEntityType.getName(), wrapEntityToDataEntry(entity),
		                                wrapPropertiesToDataEntry(existingProperties));
	}

	/**
	 * This method check whether propertyName is a keyProperty or not.
	 *
	 * @param edmEntityType EdmEntityType
	 * @param propertyName  PropertyName
	 * @return iskey
	 */
	private boolean isKey(EdmEntityType edmEntityType, String propertyName) {
		List<EdmKeyPropertyRef> keyPropertyRefs = edmEntityType.getKeyPropertyRefs();
		for (EdmKeyPropertyRef propRef : keyPropertyRefs) {
			if (propRef.getName().equals(propertyName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * This method returns the entity collection from the ODataDataHandler for the given parameters.
	 *
	 * @param tableName Name of the table
	 * @param skip      Number of skip page
	 * @param pageSize  page size
	 * @return EntityCollection
	 * @throws DataServiceFault
	 */
	private EntityCollection getEntityCollection(String tableName, int skip, int pageSize) throws DataServiceFault {
		EntityCollection set = createEntityCollectionFromDataEntryList(tableName, dataHandler.readTable(tableName));
		if (set == null) {
			return null;
		}
		EntityCollection modifiedES = new EntityCollection();
		int i = 0;
		for (Entity entity : set.getEntities()) {
			if (skip >= 0 && i >= skip && modifiedES.getEntities().size() < pageSize) {
				modifiedES.getEntities().add(entity);
			}
			i++;
		}
		modifiedES.setCount(i);
		set.setCount(i);
		if (skip == -1 && pageSize == -1) {
			return set;
		}
		return modifiedES;
	}

	/**
	 * This method returns matched entity list to the getEntity method to get the matched entity.
	 *
	 * @param entityType EdmEntityType
	 * @param param      UriParameter
	 * @param entityList List of entities
	 * @return list of entities
	 * @throws ODataApplicationException
	 * @throws DataServiceFault
	 * @see #getEntity(EdmEntityType, EntityCollection, List, String)
	 */
	private List<Entity> getMatch(EdmEntityType entityType, UriParameter param, List<Entity> entityList)
			throws ODataApplicationException, DataServiceFault {
		ArrayList<Entity> list = new ArrayList<>();
		for (Entity entity : entityList) {
			EdmProperty property = (EdmProperty) entityType.getProperty(param.getName());
			EdmType type = property.getType();
			if (type.getKind() == EdmTypeKind.PRIMITIVE) {
				Object match = readPrimitiveValue(property, param.getText());
				Property entityValue = entity.getProperty(param.getName());
				assert match != null;
				if (match.equals(entityValue.asPrimitive())) {
					list.add(entity);
				}
			} else {
				throw new DataServiceFault("Can not compare complex objects");
			}
		}
		return list;
	}

	/**
	 * This method returns the object which is the value of the property.
	 *
	 * @param edmProperty EdmProperty
	 * @param value       String value
	 * @return Object
	 * @throws ODataApplicationException
	 */
	private Object readPrimitiveValue(EdmProperty edmProperty, String value) throws ODataApplicationException {
		if (value == null) {
			return null;
		}
		try {
			if (value.startsWith("'") && value.endsWith("'")) {
				value = value.substring(1, value.length() - 1);
			}
			EdmPrimitiveType edmPrimitiveType = (EdmPrimitiveType) edmProperty.getType();
			Class<?> javaClass = getJavaClassForPrimitiveType(edmProperty, edmPrimitiveType);
			return edmPrimitiveType.valueOfString(value, edmProperty.isNullable(), edmProperty.getMaxLength(),
			                                      edmProperty.getPrecision(), edmProperty.getScale(),
			                                      edmProperty.isUnicode(), javaClass);
		} catch (EdmPrimitiveTypeException e) {
			throw new ODataApplicationException("Invalid value: " + value + " for property: " + edmProperty.getName(),
			                                    500, Locale.getDefault());
		}
	}

	/**
	 * This method returns java class to read primitive values.
	 *
	 * @param edmProperty      EdmProperty
	 * @param edmPrimitiveType EdmPrimitiveType
	 * @return javaClass
	 * @see EdmPrimitiveType#valueOfString(String, Boolean, Integer, Integer, Integer, Boolean, Class)
	 */
	private Class<?> getJavaClassForPrimitiveType(EdmProperty edmProperty, EdmPrimitiveType edmPrimitiveType) {
		Class<?> javaClass;
		if (edmProperty.getMapping() != null && edmProperty.getMapping().getMappedJavaClass() != null) {
			javaClass = edmProperty.getMapping().getMappedJavaClass();
		} else {
			javaClass = edmPrimitiveType.getDefaultType();
		}
		edmPrimitiveType.getDefaultType();
		return javaClass;
	}

	/**
	 * This method returns entity by retrieving the entity collection according to keys and etag.
	 *
	 * @param entityType EdmEntityType
	 * @param eTag       E-Tag
	 * @param keys       keys
	 * @return Entity
	 * @throws ODataApplicationException
	 * @throws DataServiceFault
	 */
	private Entity getEntity(EdmEntityType entityType, String eTag, List<UriParameter> keys)
			throws ODataApplicationException, DataServiceFault {
		EntityCollection entityCollection = createEntityCollectionFromDataEntryList(entityType.getName(), dataHandler
				.readTableWithKeys(entityType.getName(), wrapKeyParamToDataEntry(keys)));
		return getEntity(entityType, entityCollection, keys, eTag);
	}

	/**
	 * This method return entity by searching from the entity collection according to keys and etag.
	 *
	 * @param entityType       EdmEntityType
	 * @param entityCollection EntityCollection
	 * @param keys             keys
	 * @param eTag             etag
	 * @return Entity
	 * @throws ODataApplicationException
	 * @throws DataServiceFault
	 */
	private Entity getEntity(EdmEntityType entityType, EntityCollection entityCollection, List<UriParameter> keys,
	                         String eTag) throws ODataApplicationException, DataServiceFault {
		List<Entity> search = null;
		for (UriParameter param : keys) {
			search = getMatch(entityType, param, entityCollection.getEntities());
		}
		Entity finalEntity = null;
		if (search != null) {
			for (Entity entity : search) {
				if (entity.getETag().equals(eTag) || "*".equals(eTag)) {
					finalEntity = entity;
					break;
				}
			}
		}
		if (finalEntity == null) {
			throw new ODataApplicationException("Entity for requested key doesn't exist",
			                                    HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
		}
		return finalEntity;
	}

	/**
	 * This method wraps Property object into DataEntry object.
	 *
	 * @param property Property
	 * @return DataEntry
	 * @see DataEntry
	 * @see Property
	 */
	private DataEntry wrapPropertyToDataEntry(Property property) {
		DataEntry entry = new DataEntry();
		entry.addValue("propertyName", new ParamValue(property.getName()));
		if (property.getValue() != null) {
			entry.addValue("propertyValue", new ParamValue(property.getValue().toString()));
		} else {
			entry.addValue("propertyValue", new ParamValue((String) null));
		}
		return entry;
	}

	/**
	 * This method return the entity collection which are able to navigate from the parent entity (source) using uri navigation properties.
	 * <p/>
	 * In this method we check the parent entities primary keys and return the entity according to the values.
	 * we use ODataDataHandler, navigation properties to get particular foreign keys.
	 *
	 * @param metadata     Service Metadata
	 * @param parentEntity parentEntity
	 * @param navigation   UriResourceNavigation
	 * @return EntityCollection
	 * @throws DataServiceFault
	 */
	private EntityCollection getNavigableEntitySet(ServiceMetadata metadata, Entity parentEntity,
	                                               UriResourceNavigation navigation) throws DataServiceFault {
		EdmEntityType type = metadata.getEdm().getEntityType(new FullQualifiedName(parentEntity.getType()));
		List<String> keys = type.getKeyPredicateNames();
		String linkName = navigation.getProperty().getName();
		EntityCollection results;
		List<Property> properties = new ArrayList<>();
		for (String key : keys) {
			if (parentEntity.getProperty(key) != null) {
				properties.add(parentEntity.getProperty(key));
			}
		}
		results = createEntityCollectionFromDataEntryList(linkName, dataHandler
				.readTableWithKeys(linkName, wrapPropertiesToDataEntry(properties)));
		if (results != null) {
			return results;
		} else {
			throw new DataServiceFault("unknown relation");
		}
	}

	/**
	 * This method return the entity which is able to navigate from the parent entity (source) using uri navigation properties.
	 * <p/>
	 * In this method we check the parent entities foreign keys and return the entity according to the values.
	 * we use ODataDataHandler, navigation properties to get particular foreign keys.
	 *
	 * @param metadata         Service Metadata
	 * @param parentEntityType EdmEntityType (Source)
	 * @param parentEntity     Entity (Source)
	 * @param navigation       UriResourceNavigation (Destination)
	 * @return Entity (Destination)
	 * @throws ODataApplicationException
	 * @throws DataServiceFault
	 * @see ODataDataHandler#getNavigationProperties()
	 */
	private Entity getNavigableEntity(ServiceMetadata metadata, EdmEntityType parentEntityType, Entity parentEntity,
	                                  UriResourceNavigation navigation)
			throws ODataApplicationException, DataServiceFault {
		String linkName = navigation.getProperty().getName();
		List<String> keyProperties =
				dataHandler.getNavigationProperties().get(linkName).get(parentEntityType.getName());
		List<Property> properties = new ArrayList<>();
		EntityCollection results;
		for (String key : keyProperties) {
			if (parentEntity.getProperty(key) != null) {
				properties.add(parentEntity.getProperty(key));
			}
		}
		results = createEntityCollectionFromDataEntryList(linkName, dataHandler
				.readTableWithKeys(linkName, wrapPropertiesToDataEntry(properties)));
		if (results != null) {
			return results.getEntities().get(0);
		} else {
			throw new RuntimeException("unknown relation");
		}
	}

	private Map<String, List<CsdlPropertyRef>> getKeysCsdlMap() throws DataServiceFault {
		Map<String, List<CsdlPropertyRef>> keyMap = new HashMap<>();
		for (String tableName : dataHandler.getTableList()) {
			List<CsdlPropertyRef> propertyList = new ArrayList<>();
			for (String element : dataHandler.getPrimaryKeys().get(tableName)) {
				propertyList.add(new CsdlPropertyRef().setName(element));
			}
			keyMap.put(tableName, propertyList);
		}
		return keyMap;
	}

	/**
	 * This method returns a list of CsdlProperty for the given tableName.
	 *
	 * @param tableName Name of the table
	 * @return list of CsdlProperty
	 */
	private List<CsdlProperty> getProperties(String tableName) {
		List<CsdlProperty> properties = new ArrayList<>();
		for (DataColumn column : this.dataHandler.getTableMetadata().get(tableName).values()) {
			CsdlProperty property = new CsdlProperty();
			property.setName(column.getColumnName());
			int columnType = column.getColumnType();
			switch (columnType) {
				case Types.INTEGER:
					property.setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
					property.setNullable(column.isNullable());
					break;
				case Types.TINYINT:
					property.setType(EdmPrimitiveTypeKind.Int16.getFullQualifiedName());
					property.setNullable(column.isNullable());
				case Types.SMALLINT:
					property.setType(EdmPrimitiveTypeKind.Int16.getFullQualifiedName());
					property.setNullable(column.isNullable());
					break;
				case Types.DOUBLE:
					property.setType(EdmPrimitiveTypeKind.Double.getFullQualifiedName());
					property.setPrecision(column.getPrecision());
					property.setScale(column.getScale());
					property.setNullable(column.isNullable());
					break;
				case Types.VARCHAR:
				case Types.CHAR:
				case Types.LONGVARCHAR:
				case Types.CLOB:
					property.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
					property.setMaxLength(column.getMaxLength());
					property.setNullable(column.isNullable());
					property.setUnicode(false);
					break;
				case Types.BOOLEAN:
				case Types.BIT:
					property.setType(EdmPrimitiveTypeKind.Boolean.getFullQualifiedName());
					property.setNullable(column.isNullable());
					break;
				case Types.BLOB:
					property.setType(EdmPrimitiveTypeKind.Binary.getFullQualifiedName());
					property.setNullable(column.isNullable());
					break;
				case Types.BINARY:
					property.setType(EdmPrimitiveTypeKind.Binary.getFullQualifiedName());
					property.setNullable(column.isNullable());
					break;
				case Types.LONGVARBINARY:
				case Types.VARBINARY:
					property.setType(EdmPrimitiveTypeKind.Binary.getFullQualifiedName());
					property.setNullable(column.isNullable());
					break;
				case Types.DATE:
					property.setType(EdmPrimitiveTypeKind.Date.getFullQualifiedName());
					property.setNullable(column.isNullable());
					break;
				case Types.DECIMAL:
				case Types.NUMERIC:
					property.setType(EdmPrimitiveTypeKind.Decimal.getFullQualifiedName());
					property.setPrecision(column.getPrecision());
					property.setScale(column.getScale());
					property.setNullable(column.isNullable());
					break;
				case Types.FLOAT:
				case Types.REAL:
					property.setType(EdmPrimitiveTypeKind.Single.getFullQualifiedName());
					property.setPrecision(column.getPrecision());
					property.setNullable(column.isNullable());
					property.setScale(column.getScale());
					break;
				case Types.TIME:
					property.setType(EdmPrimitiveTypeKind.TimeOfDay.getFullQualifiedName());
					property.setNullable(column.isNullable());
					break;
				case Types.LONGNVARCHAR:
				case Types.NCHAR:
				case Types.NVARCHAR:
				case Types.NCLOB:
					property.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
					property.setMaxLength(column.getMaxLength());
					property.setNullable(column.isNullable());
					property.setUnicode(true);
					break;
				case Types.BIGINT:
					property.setType(EdmPrimitiveTypeKind.Int64.getFullQualifiedName());
					property.setNullable(column.isNullable());
					break;
				case Types.TIMESTAMP:
					property.setType(EdmPrimitiveTypeKind.DateTimeOffset.getFullQualifiedName());
					property.setNullable(column.isNullable());
					break;
				case Types.SQLXML:
					property.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
					property.setMaxLength(column.getMaxLength());
					property.setNullable(column.isNullable());
					property.setUnicode(false);
					break;
				default:
					property.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
					property.setMaxLength(column.getMaxLength());
					property.setNullable(column.isNullable());
					property.setUnicode(false);
					break;
			}
			properties.add(property);
		}
		return properties;
	}

	/**
	 * This method returns Map with table names as key, and contains list of CsdlProperty of tables.
	 * This map is used to initialize the EDMProvider.
	 *
	 * @return Map
	 * @see #initializeEdmProvider(String)
	 */
	private Map<String, List<CsdlProperty>> getPropertiesMap() {
		Map<String, List<CsdlProperty>> propertiesMap = new HashMap<>();
		for (String tableName : dataHandler.getTableList()) {
			propertiesMap.put(tableName, getProperties(tableName));
		}
		return propertiesMap;
	}

	/**
	 * This method creates primitive type property.
	 *
	 * @param columnType Data type of the column - java.sql.Types
	 * @param name       Name of the column
	 * @param paramValue String value
	 * @return Property
	 * @throws DataServiceFault
	 * @see Types
	 * @see Property
	 */
	private Property createPrimitive(final int columnType, final String name, final String paramValue)
			throws DataServiceFault {
		String propertyType;
		Object value;
		switch (columnType) {
			case Types.INTEGER:
				propertyType = EdmPrimitiveTypeKind.Int32.getFullQualifiedName().getFullQualifiedNameAsString();
				value = paramValue == null ? null : ConverterUtil.convertToInt(paramValue);
				break;
			case Types.TINYINT:
				propertyType = EdmPrimitiveTypeKind.Int16.getFullQualifiedName().getFullQualifiedNameAsString();
				value = paramValue == null ? null : ConverterUtil.convertToByte(paramValue);
				break;
			case Types.SMALLINT:
				propertyType = EdmPrimitiveTypeKind.Int16.getFullQualifiedName().getFullQualifiedNameAsString();
				value = paramValue == null ? null : ConverterUtil.convertToShort(paramValue);
				break;
			case Types.DOUBLE:
				propertyType = EdmPrimitiveTypeKind.Double.getFullQualifiedName().getFullQualifiedNameAsString();
				value = paramValue == null ? null : ConverterUtil.convertToDouble(paramValue);
				break;
			case Types.VARCHAR:
			case Types.CHAR:
			case Types.LONGVARCHAR:
			case Types.CLOB:
				propertyType = EdmPrimitiveTypeKind.String.getFullQualifiedName().getFullQualifiedNameAsString();
				value = paramValue;
				break;
			case Types.BOOLEAN:
			case Types.BIT:
				propertyType = EdmPrimitiveTypeKind.Boolean.getFullQualifiedName().getFullQualifiedNameAsString();
				value = paramValue == null ? null : ConverterUtil.convertToBoolean(paramValue);
				break;
			case Types.BLOB:
				propertyType = EdmPrimitiveTypeKind.Binary.getFullQualifiedName().getFullQualifiedNameAsString();
				value = getBytesFromBase64String(paramValue);
				break;
			case Types.BINARY:
			case Types.LONGVARBINARY:
			case Types.VARBINARY:
				propertyType = EdmPrimitiveTypeKind.Binary.getFullQualifiedName().getFullQualifiedNameAsString();
				value = getBytesFromBase64String(paramValue);
				break;
			case Types.DATE:
				propertyType = EdmPrimitiveTypeKind.Date.getFullQualifiedName().getFullQualifiedNameAsString();
				value = ConverterUtil.convertToDate(paramValue);
				break;
			case Types.DECIMAL:
			case Types.NUMERIC:
				propertyType = EdmPrimitiveTypeKind.Decimal.getFullQualifiedName().getFullQualifiedNameAsString();
				value = paramValue == null ? null : ConverterUtil.convertToBigDecimal(paramValue);
				break;
			case Types.FLOAT:
			case Types.REAL:
				propertyType = EdmPrimitiveTypeKind.Single.getFullQualifiedName().getFullQualifiedNameAsString();
				value = paramValue == null ? null : ConverterUtil.convertToFloat(paramValue);
				break;
			case Types.TIME:
				propertyType = EdmPrimitiveTypeKind.TimeOfDay.getFullQualifiedName().getFullQualifiedNameAsString();
				value = ConverterUtil.convertToDateTime(paramValue);
				break;
			case Types.LONGNVARCHAR:
			case Types.NCHAR:
			case Types.NVARCHAR:
			case Types.NCLOB:
				propertyType = EdmPrimitiveTypeKind.String.getFullQualifiedName().getFullQualifiedNameAsString();
				value = paramValue;
				break;
			case Types.BIGINT:
				propertyType = EdmPrimitiveTypeKind.Int64.getFullQualifiedName().getFullQualifiedNameAsString();
				value = paramValue == null ? null : ConverterUtil.convertToLong(paramValue);
				break;
			case Types.TIMESTAMP:
				propertyType =
						EdmPrimitiveTypeKind.DateTimeOffset.getFullQualifiedName().getFullQualifiedNameAsString();
				value = ConverterUtil.convertToTime(paramValue);
				break;
			default:
				propertyType = EdmPrimitiveTypeKind.String.getFullQualifiedName().getFullQualifiedNameAsString();
				value = paramValue;
				break;
		}
		return new Property(propertyType, name, ValueType.PRIMITIVE, value);
	}

	/**
	 * This method initialize the EDM Provider.
	 *
	 * @param configID id of the config
	 * @return CsdlEdmProvider
	 * @throws DataServiceFault
	 * @see EDMProvider
	 */
	private CsdlEdmProvider initializeEdmProvider(String configID) throws DataServiceFault {
		return new EDMProvider(dataHandler.getTableList(), configID, namespace, getPropertiesMap(), getKeysCsdlMap(),
		                       dataHandler.getTableList(), dataHandler.getNavigationProperties());

	}
}
