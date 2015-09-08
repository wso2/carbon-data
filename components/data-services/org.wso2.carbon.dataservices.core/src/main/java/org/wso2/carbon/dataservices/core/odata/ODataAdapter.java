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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

	private static final Log log = LogFactory.getLog(ODataAdapter.class);

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

	public ODataAdapter(ODataDataHandler dataHandler, String namespace, String configID) throws ODataServiceFault {
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
	 * This method process the read requests.
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
				log.error(new ODataServiceFault("Singletons are not supported."));
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
						entitySet = getNavigableEntitySet(this.serviceMetadata, entity, nav);
					} else {
						parentEntity = entity;
						entity = getNavigableEntity(serviceMetadata, parentEntity, nav);
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
		} catch (ODataServiceFault dataServiceFault) {
			log.error("Error in processing the read request", dataServiceFault);
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

			@Override
			public void visit(StreamResponse response) throws ODataApplicationException {
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
					log.error("Entity couldn't find.", new ODataServiceFault("Entity couldn't find"));
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
			Entity created = createEntityInTable(edmEntitySet.getEntityType(), entity);
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
		List<UriParameter> keys = request.getKeyPredicates();
		EdmEntityType entityType = request.getEntitySet().getEntityType();
		/*checking for the E-Tag option, If E-Tag didn't specify in the request we don't need to check the E-Tag checksum,
		we can do the update operation directly */
		if ("*".equals(eTag)) {
			try {
				updateEntity(entityType, changes, keys, merge);
				response.writeUpdatedEntity();
			} catch (DataServiceFault e) {
				log.error("Error in updating entity", e);
			}
		} else {
			try {
				this.dataHandler.openTransaction();
				EntityCollection set = createEntityCollectionFromDataEntryList(entityType.getName(), this.dataHandler
						.readTableWithKeys(entityType.getName(), wrapKeyParamToDataEntry(keys), true));
				Entity entity = getEntity(entityType, set, keys, eTag);
				if (entity != null) {
					boolean result = updateEntityWithETagMatched(entityType, changes, entity, merge);
					if (result) {
						response.writeUpdatedEntity();
					} else {
						response.writeNotModified();
					}
				} else {
					response.writeNotFound(true);
				}
			} catch (DataServiceFault dataServiceFault) {
				response.writeNotModified();
			} finally {
				try {
					this.dataHandler.closeTransaction();
				} catch (DataServiceFault dataServiceFault) {
					//ignore
				}
			}
		}
	}

	@Override
	public void deleteEntity(DataRequest request, String eTag, EntityResponse response)
			throws ODataApplicationException {
		List<UriParameter> keys = request.getKeyPredicates();
		EdmEntityType entityType = request.getEntitySet().getEntityType();
		/*checking for the E-Tag option, If E-Tag didn't specify in the request we don't need to check the E-Tag checksum,
		we can do the update operation directly */
		if ("*".equals(eTag)) {
			try {
				this.dataHandler.deleteEntityInTable(entityType.getName(), wrapKeyParamToDataEntry(keys));
				response.writeDeletedEntityOrReference();
			} catch (ODataServiceFault e) {
				log.error("Error in deleting entity", e);
			}
		} else {
			try {
				this.dataHandler.openTransaction();
				EntityCollection set = createEntityCollectionFromDataEntryList(entityType.getName(), this.dataHandler
						.readTableWithKeys(entityType.getName(), wrapKeyParamToDataEntry(keys), true));
				Entity entity = getEntity(entityType, set, keys, eTag);
				if (entity != null) {
					boolean result = this.dataHandler.deleteEntityInTableTransactional(entityType.getName(),
					                                                                   wrapEntityToDataEntry(entityType,
					                                                                                         entity));
					if (result) {
						response.writeDeletedEntityOrReference();
					} else {
						response.writeNotModified();
					}
				} else {
					response.writeNotFound(true);
				}
			} catch (DataServiceFault e) {
				log.error("Error in deleting entity", e);
			} finally {
				try {
					this.dataHandler.closeTransaction();
				} catch (DataServiceFault dataServiceFault) {
					//ignore
				}
			}
		}

	}

	@Override
	public void updateProperty(DataRequest request, final Property property, boolean merge, String entityETag,
	                           PropertyResponse response) throws ODataApplicationException, ContentNegotiatorException {
		if (property.isPrimitive()) {
			EdmEntityType entityType = request.getEntitySet().getEntityType();
		/*checking for the E-Tag option, If E-Tag didn't specify in the request we don't need to check the E-Tag checksum,
		we can do the update operation directly */
			if ("*".equals(entityETag)) {
				try {
					this.dataHandler.updatePropertyInTable(entityType.getName(),
					                                       wrapPropertyToDataEntry(entityType, property), false);
					if (property.getValue() == null) {
						response.writePropertyDeleted();
					} else {
						response.writePropertyUpdated();
					}
				} catch (DataServiceFault e) {
					log.error("Error in updating property", e);
				}
			} else {
				List<UriParameter> keys = request.getKeyPredicates();
				try {
					this.dataHandler.openTransaction();
					EntityCollection set = createEntityCollectionFromDataEntryList(entityType.getName(), this.
							dataHandler.readTableWithKeys(entityType.getName(), wrapKeyParamToDataEntry(keys), true));
					Entity entity = getEntity(entityType, set, keys, entityETag);
					if (entity != null) {
						this.dataHandler.deleteEntityInTable(entityType.getName(),
						                                     wrapEntityToDataEntry(entityType, entity));
					} else {
						response.writeNotFound(true);
						log.error("Error in updating property, E-Tag checksum didn't match");
					}
				} catch (DataServiceFault e) {
					log.error("Error in updating property", e);
				} finally {
					try {
						this.dataHandler.closeTransaction();
					} catch (ODataServiceFault dataServiceFault) {
						//ignore
					}
				}
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
			if (entity != null) {
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
			} else {
				response.writeNotFound(true);
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
	 * @throws ODataServiceFault
	 * @see EntityCollection
	 */
	private EntityCollection createEntityCollectionFromDataEntryList(String tableName, List<ODataEntry> resultSet)
			throws ODataServiceFault {
		EntityCollection entitySet = new EntityCollection();
		int count = 0;
		for (ODataEntry entry : resultSet) {
			Entity entity = new Entity();
			for (DataColumn column : this.dataHandler.getTableMetadata().get(tableName).values()) {
				String columnName = column.getColumnName();
				entity.addProperty(createPrimitive(column.getColumnType(), columnName, entry.getValue(columnName)));
			}
			//Set ETag to the entity
			entity.setETag(entry.getValue("ETag"));
			entity.setType(new FullQualifiedName(this.namespace, tableName).getFullQualifiedNameAsString());
			entitySet.getEntities().add(entity);
			count++;
		}
		entitySet.setCount(count);
		return entitySet;
	}

	/**
	 * This method creates the entity in table by calling the insertEntityToTable method in ODataDataHandler.
	 * Entity object is wrapped to DataEntry before call the method.
	 *
	 * @param entityType Name of the table (Entity Type)
	 * @param entity     Entity to create
	 * @return Created entity
	 * @throws ODataServiceFault
	 * @see ODataDataHandler
	 * @see #wrapEntityToDataEntry(EdmEntityType, Entity)
	 */
	private Entity createEntityInTable(EdmEntityType entityType, Entity entity) throws ODataServiceFault {
		String eTag;
		try {
			eTag = this.dataHandler.insertEntityToTable(entityType.getName(), wrapEntityToDataEntry(entityType, entity));
			entity.setETag(eTag);
			return entity;
		} catch (ODataServiceFault | ODataApplicationException e) {
			log.error("Error in creating entity ", e);
			throw new ODataServiceFault(e.getMessage());
		}
	}

	/**
	 * This method wraps Entity object into DataEntry object.
	 *
	 * @param entity Entity
	 * @return DataEntry
	 * @see DataEntry
	 */
	private ODataEntry wrapEntityToDataEntry(EdmEntityType entityType, Entity entity) throws ODataApplicationException {
		ODataEntry entry = new ODataEntry();
		for (Property property : entity.getProperties()) {
			EdmProperty propertyType = (EdmProperty) entityType.getProperty(property.getName());
			entry.addValue(property.getName(), property.getValue() == null ? null :
			                                   readPrimitiveValueInString(propertyType, property.getValue()));
		}
		return entry;
	}

	/**
	 * This method wraps list of properties into single DataEntry object.
	 *
	 * @param entityType    Entity type
	 * @param propertyTypes Map od Property Types
	 * @param properties    list of properties
	 * @return DataEntry
	 * @see DataEntry
	 * @see Property
	 */
	private ODataEntry wrapPropertiesToDataEntry(EdmEntityType entityType, List<Property> properties,
	                                             Map<String, EdmProperty> propertyTypes)
			throws ODataApplicationException {
		ODataEntry entry = new ODataEntry();
		for (Property property : properties) {
			EdmProperty propertyType = propertyTypes.get(property.getName());
			entry.addValue(property.getName(), property.getValue() == null ? null :
			                                   readPrimitiveValueInString(propertyType, property.getValue()));
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
	private ODataEntry wrapKeyParamToDataEntry(List<UriParameter> keys) {
		ODataEntry entry = new ODataEntry();
		for (UriParameter key : keys) {
			String value = key.getText();
			if (value.startsWith("'") && value.endsWith("'")) {
				value = value.substring(1, value.length() - 1);
			}
			entry.addValue(key.getName(), value);
		}
		return entry;
	}

	public CsdlEdmProvider getEdmProvider() {
		return this.edmProvider;
	}

	private byte[] getBytesFromBase64String(String base64Str) throws ODataServiceFault {
		try {
			return Base64.decodeBase64(base64Str.getBytes(DBConstants.DEFAULT_CHAR_SET_TYPE));
		} catch (Exception e) {
			throw new ODataServiceFault(e.getMessage());
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
	 * @see ODataDataHandler#updateEntityInTable(String, ODataEntry)
	 */
	private boolean updateEntityWithETagMatched(EdmEntityType edmEntityType, Entity entity, Entity existingEntity,
	                                         boolean merge) throws ODataApplicationException, DataServiceFault {
		/* loop over all properties and replace the values with the values of the given payload
		   Note: ignoring ComplexType, as we don't have it in wso2dss oData model */
		List<Property> oldProperties = existingEntity.getProperties();
		ODataEntry newProperties = new ODataEntry();
		Map<String, EdmProperty> propertyMap = new HashMap<>();
		for (String property : edmEntityType.getPropertyNames()) {
			Property updateProperty = entity.getProperty(property);
			EdmProperty propertyType = (EdmProperty) edmEntityType.getProperty(property);
			if (isKey(edmEntityType, property)) {
				propertyMap.put(property, (EdmProperty) edmEntityType.getProperty(property));
				continue;
			}
			// the request payload might not consider ALL properties, so it can be null
			if (updateProperty == null) {
				// if a property has NOT been added to the request payload
				// depending on the HttpMethod, our behavior is different
				if (merge) {
					// as of the OData spec, in case of PATCH, the existing property is not touched
					propertyMap.put(property, (EdmProperty) edmEntityType.getProperty(property));
					continue;
				} else {
					// as of the OData spec, in case of PUT, the existing property is set to null (or to default value)
					propertyMap.put(property, (EdmProperty) edmEntityType.getProperty(property));
					newProperties.addValue(property, null);
					continue;
				}
			}
			propertyMap.put(property, (EdmProperty) edmEntityType.getProperty(property));
			newProperties.addValue(property, readPrimitiveValueInString(propertyType, updateProperty.getValue()));
		}
		return this.dataHandler.updateEntityInTableTransactional(edmEntityType.getName(),
		                                                  wrapPropertiesToDataEntry(edmEntityType, oldProperties,
		                                                                            propertyMap), newProperties);
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
	 * @throws ODataServiceFault
	 */
	private EntityCollection getEntityCollection(String tableName, int skip, int pageSize) throws ODataServiceFault {
		EntityCollection set = createEntityCollectionFromDataEntryList(tableName,
		                                                               this.dataHandler.readTable(tableName));
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
	 * @throws ODataServiceFault
	 * @see #getEntity(EdmEntityType, EntityCollection, List, String)
	 */
	private List<Entity> getMatch(EdmEntityType entityType, UriParameter param, List<Entity> entityList)
			throws ODataApplicationException, ODataServiceFault {
		ArrayList<Entity> list = new ArrayList<>();
		for (Entity entity : entityList) {
			EdmProperty property = (EdmProperty) entityType.getProperty(param.getName());
			EdmType type = property.getType();
			if (type.getKind() == EdmTypeKind.PRIMITIVE) {
				Object match = readPrimitiveValue(property, param.getText());
				Property entityValue = entity.getProperty(param.getName());
				if (match != null) {
					if (match.equals(entityValue.asPrimitive())) {
						list.add(entity);
					}
				} else {
					if (null == entityValue.asPrimitive()) {
						list.add(entity);
					}
				}
			} else {
				throw new ODataServiceFault("Complex elements are not supported, couldn't compare complex objects.");
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
	 * This method returns the object which is the value of the property.
	 *
	 * @param edmProperty EdmProperty
	 * @param value       String value
	 * @return Object
	 * @throws ODataApplicationException
	 */
	private String readPrimitiveValueInString(EdmProperty edmProperty, Object value) throws ODataApplicationException {
		if (value == null) {
			return null;
		}
		try {
			EdmPrimitiveType edmPrimitiveType = (EdmPrimitiveType) edmProperty.getType();
			return edmPrimitiveType.valueToString(value, edmProperty.isNullable(), edmProperty.getMaxLength(),
			                                      edmProperty.getPrecision(), edmProperty.getScale(),
			                                      edmProperty.isUnicode());
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
	 * @throws ODataServiceFault
	 */
	private Entity getEntity(EdmEntityType entityType, String eTag, List<UriParameter> keys)
			throws ODataApplicationException, ODataServiceFault {
		EntityCollection entityCollection = createEntityCollectionFromDataEntryList(entityType.getName(), dataHandler
				.readTableWithKeys(entityType.getName(), wrapKeyParamToDataEntry(keys), false));
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
	 * @throws ODataServiceFault
	 */
	private Entity getEntity(EdmEntityType entityType, EntityCollection entityCollection, List<UriParameter> keys,
	                         String eTag) throws ODataApplicationException, ODataServiceFault {
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
		if (finalEntity == null && search != null && !"*".equals(eTag)) {
			log.error(new ODataServiceFault("E-Tag doesn't matched with existing entity"));
		}
		return finalEntity;
	}

	/**
	 * This method wraps Property object into DataEntry object.
	 *
	 * @param entityType entity Type
	 * @param property   Property
	 * @return DataEntry
	 * @see DataEntry
	 * @see Property
	 */
	private ODataEntry wrapPropertyToDataEntry(EdmEntityType entityType, Property property)
			throws ODataApplicationException {
		ODataEntry entry = new ODataEntry();
		if (property.getValue() != null) {
			EdmProperty propertyType = (EdmProperty) entityType.getProperty(property.getName());
			entry.addValue(property.getName(), readPrimitiveValueInString(propertyType, property));
		} else {
			entry.addValue(property.getName(), null);
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
	 * @throws ODataServiceFault
	 */
	private EntityCollection getNavigableEntitySet(ServiceMetadata metadata, Entity parentEntity,
	                                               UriResourceNavigation navigation)
			throws ODataServiceFault, ODataApplicationException {
		EdmEntityType type = metadata.getEdm().getEntityType(new FullQualifiedName(parentEntity.getType()));
		String linkName = navigation.getProperty().getName();
		EntityCollection results;
		List<Property> properties = new ArrayList<>();
		Map<String, EdmProperty> propertyMap = new HashMap<>();
		for (NavigationKeys keys : this.dataHandler.getNavigationProperties().get(type.getName())
		                                           .getNavigationKeys(linkName)) {
			if (parentEntity.getProperty(keys.getPrimaryKey()) != null) {
				Property property = parentEntity.getProperty(keys.getPrimaryKey());
				propertyMap.put(keys.getForeignKey(), (EdmProperty) type.getProperty(property.getName()));
				property.setName(keys.getForeignKey());
				properties.add(property);

			}
		}
		results = createEntityCollectionFromDataEntryList(linkName, dataHandler
				.readTableWithKeys(linkName, wrapPropertiesToDataEntry(type, properties, propertyMap), false));
		if (results != null) {
			return results;
		} else {
			throw new ODataServiceFault("Unknown relation with "+ type.getName() + " and " + linkName +" .");
		}
	}

	/**
	 * This method return the entity which is able to navigate from the parent entity (source) using uri navigation properties.
	 * <p/>
	 * In this method we check the parent entities foreign keys and return the entity according to the values.
	 * we use ODataDataHandler, navigation properties to get particular foreign keys.
	 *
	 * @param metadata     Service Metadata
	 * @param parentEntity Entity (Source)
	 * @param navigation   UriResourceNavigation (Destination)
	 * @return Entity (Destination)
	 * @throws ODataApplicationException
	 * @throws ODataServiceFault
	 * @see ODataDataHandler#getNavigationProperties()
	 */
	private Entity getNavigableEntity(ServiceMetadata metadata, Entity parentEntity, UriResourceNavigation navigation)
			throws ODataApplicationException, ODataServiceFault {
		EdmEntityType type = metadata.getEdm().getEntityType(new FullQualifiedName(parentEntity.getType()));
		String linkName = navigation.getProperty().getName();
		List<Property> properties = new ArrayList<>();
		Map<String, EdmProperty> propertyMap = new HashMap<>();
		for (NavigationKeys keys : this.dataHandler.getNavigationProperties().get(linkName)
		                                           .getNavigationKeys(type.getName())) {
			if (parentEntity.getProperty(keys.getForeignKey()) != null) {
				Property property = parentEntity.getProperty(keys.getForeignKey());
				propertyMap.put(keys.getPrimaryKey(), (EdmProperty) type.getProperty(property.getName()));
				property.setName(keys.getPrimaryKey());
				properties.add(property);
			}
		}
		EntityCollection results;
		results = createEntityCollectionFromDataEntryList(linkName, dataHandler
				.readTableWithKeys(linkName, wrapPropertiesToDataEntry(type, properties, propertyMap), false));
		if (results != null) {
			return results.getEntities().get(0);
		} else {
			throw new RuntimeException("unknown relation");
		}
	}

	private Map<String, List<CsdlPropertyRef>> getKeysCsdlMap() throws ODataServiceFault {
		Map<String, List<CsdlPropertyRef>> keyMap = new HashMap<>();
		for (String tableName : this.dataHandler.getTableList()) {
			List<CsdlPropertyRef> propertyList = new ArrayList<>();
			for (String element : this.dataHandler.getPrimaryKeys().get(tableName)) {
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
			DataColumn.ODataDataType columnType = column.getColumnType();
			switch (columnType) {
				case INT32:
					property.setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
					property.setNullable(column.isNullable());
					property.setMaxLength(column.getMaxLength());
					break;
				case INT16:
					property.setType(EdmPrimitiveTypeKind.Int16.getFullQualifiedName());
					property.setNullable(column.isNullable());
					property.setMaxLength(column.getMaxLength());
					break;
				case DOUBLE:
					property.setType(EdmPrimitiveTypeKind.Double.getFullQualifiedName());
					property.setPrecision(column.getPrecision());
					property.setScale(column.getScale());
					property.setNullable(column.isNullable());
					break;
				case STRING:
					property.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
					property.setMaxLength(column.getMaxLength());
					property.setNullable(column.isNullable());
					break;
				case BOOLEAN:
					property.setType(EdmPrimitiveTypeKind.Boolean.getFullQualifiedName());
					property.setNullable(column.isNullable());
					property.setMaxLength(column.getMaxLength());
					break;
				case BINARY:
					property.setType(EdmPrimitiveTypeKind.Binary.getFullQualifiedName());
					property.setNullable(column.isNullable());
					property.setMaxLength(column.getMaxLength());
					break;
				case BYTE:
					property.setType(EdmPrimitiveTypeKind.Byte.getFullQualifiedName());
					property.setNullable(column.isNullable());
					property.setMaxLength(column.getMaxLength());
					break;
				case SBYTE:
					property.setType(EdmPrimitiveTypeKind.SByte.getFullQualifiedName());
					property.setNullable(column.isNullable());
					property.setMaxLength(column.getMaxLength());
					break;
				case DATE:
					property.setType(EdmPrimitiveTypeKind.Date.getFullQualifiedName());
					property.setNullable(column.isNullable());
					property.setMaxLength(column.getMaxLength());
					break;
				case DURATION:
					break;
				case DECIMAL:
					property.setType(EdmPrimitiveTypeKind.Decimal.getFullQualifiedName());
					property.setPrecision(column.getPrecision());
					property.setScale(column.getScale());
					property.setNullable(column.isNullable());
					property.setMaxLength(column.getMaxLength());
					break;
				case SINGLE:
					property.setType(EdmPrimitiveTypeKind.Single.getFullQualifiedName());
					property.setPrecision(column.getPrecision());
					property.setNullable(column.isNullable());
					property.setMaxLength(column.getMaxLength());
					property.setScale(column.getScale());
					break;
				case TIMEOFDAY:
					property.setType(EdmPrimitiveTypeKind.TimeOfDay.getFullQualifiedName());
					property.setNullable(column.isNullable());
					property.setMaxLength(column.getMaxLength());
					break;
				case INT64:
					property.setType(EdmPrimitiveTypeKind.Int64.getFullQualifiedName());
					property.setNullable(column.isNullable());
					property.setMaxLength(column.getMaxLength());
					break;
				case DATE_TIMEOFFSET:
					property.setType(EdmPrimitiveTypeKind.DateTimeOffset.getFullQualifiedName());
					property.setNullable(column.isNullable());
					property.setMaxLength(column.getMaxLength());
					break;
				case GUID:
					property.setType(EdmPrimitiveTypeKind.Guid.getFullQualifiedName());
					property.setNullable(column.isNullable());
					property.setMaxLength(column.getMaxLength());
					break;
				case STREAM:
					property.setType(EdmPrimitiveTypeKind.Stream.getFullQualifiedName());
					property.setNullable(column.isNullable());
					property.setMaxLength(column.getMaxLength());
					break;
				case GEOGRAPHY:
					property.setType(EdmPrimitiveTypeKind.Geography.getFullQualifiedName());
					property.setNullable(column.isNullable());
					property.setMaxLength(column.getMaxLength());
					break;
				case GEOGRAPHY_POINT:
					property.setType(EdmPrimitiveTypeKind.GeographyPoint.getFullQualifiedName());
					property.setNullable(column.isNullable());
					property.setMaxLength(column.getMaxLength());
					break;
				case GEOGRAPHY_LINE_STRING:
					property.setType(EdmPrimitiveTypeKind.GeographyLineString.getFullQualifiedName());
					property.setNullable(column.isNullable());
					property.setMaxLength(column.getMaxLength());
					break;
				case GEOGRAPHY_POLYGON:
					property.setType(EdmPrimitiveTypeKind.GeographyPolygon.getFullQualifiedName());
					property.setNullable(column.isNullable());
					property.setMaxLength(column.getMaxLength());
					break;
				case GEOGRAPHY_MULTIPOINT:
					property.setType(EdmPrimitiveTypeKind.GeographyMultiPoint.getFullQualifiedName());
					property.setNullable(column.isNullable());
					property.setMaxLength(column.getMaxLength());
					break;
				case GEOGRAPHY_MULTILINE_STRING:
					property.setType(EdmPrimitiveTypeKind.GeographyMultiLineString.getFullQualifiedName());
					property.setNullable(column.isNullable());
					property.setMaxLength(column.getMaxLength());
					break;
				case GEOGRAPHY_MULTIPOLYGON:
					property.setType(EdmPrimitiveTypeKind.GeographyMultiPolygon.getFullQualifiedName());
					property.setNullable(column.isNullable());
					property.setMaxLength(column.getMaxLength());
					break;
				case GEOGRAPHY_COLLECTION:
					property.setType(EdmPrimitiveTypeKind.GeographyCollection.getFullQualifiedName());
					property.setNullable(column.isNullable());
					property.setMaxLength(column.getMaxLength());
					break;
				case GEOMETRY:
					property.setType(EdmPrimitiveTypeKind.Geometry.getFullQualifiedName());
					property.setNullable(column.isNullable());
					property.setMaxLength(column.getMaxLength());
					break;
				case GEOMETRY_POINT:
					property.setType(EdmPrimitiveTypeKind.GeometryPoint.getFullQualifiedName());
					property.setNullable(column.isNullable());
					property.setMaxLength(column.getMaxLength());
					break;
				case GEOMETRY_LINE_STRING:
					property.setType(EdmPrimitiveTypeKind.GeometryLineString.getFullQualifiedName());
					property.setNullable(column.isNullable());
					property.setMaxLength(column.getMaxLength());
					break;
				case GEOMETRY_POLYGON:
					property.setType(EdmPrimitiveTypeKind.GeometryPolygon.getFullQualifiedName());
					property.setNullable(column.isNullable());
					property.setMaxLength(column.getMaxLength());
					break;
				case GEOMETRY_MULTIPOINT:
					property.setType(EdmPrimitiveTypeKind.GeometryMultiPoint.getFullQualifiedName());
					property.setNullable(column.isNullable());
					property.setMaxLength(column.getMaxLength());
					break;
				case GEOMETRY_MULTILINE_STRING:
					property.setType(EdmPrimitiveTypeKind.GeometryMultiLineString.getFullQualifiedName());
					property.setNullable(column.isNullable());
					property.setMaxLength(column.getMaxLength());
					break;
				case GEOMETRY_MULTIPOLYGON:
					property.setType(EdmPrimitiveTypeKind.GeometryMultiPolygon.getFullQualifiedName());
					property.setNullable(column.isNullable());
					property.setMaxLength(column.getMaxLength());
					break;
				case GEOMETRY_COLLECTION:
					property.setType(EdmPrimitiveTypeKind.GeometryMultiPolygon.getFullQualifiedName());
					property.setNullable(column.isNullable());
					property.setMaxLength(column.getMaxLength());
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
		for (String tableName : this.dataHandler.getTableList()) {
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
	 * @throws ODataServiceFault
	 * @see Types
	 * @see Property
	 */
	private Property createPrimitive(final DataColumn.ODataDataType columnType, final String name,
	                                 final String paramValue) throws ODataServiceFault {
		String propertyType;
		Object value;
		switch (columnType) {
			case INT32:
				propertyType = EdmPrimitiveTypeKind.Int32.getFullQualifiedName().getFullQualifiedNameAsString();
				value = paramValue == null ? null : ConverterUtil.convertToInt(paramValue);
				break;
			case INT16:
				propertyType = EdmPrimitiveTypeKind.Int16.getFullQualifiedName().getFullQualifiedNameAsString();
				value = paramValue == null ? null : ConverterUtil.convertToByte(paramValue);
				break;
			case DOUBLE:
				propertyType = EdmPrimitiveTypeKind.Double.getFullQualifiedName().getFullQualifiedNameAsString();
				value = paramValue == null ? null : ConverterUtil.convertToDouble(paramValue);
				break;
			case STRING:
				propertyType = EdmPrimitiveTypeKind.String.getFullQualifiedName().getFullQualifiedNameAsString();
				value = paramValue;
				break;
			case BOOLEAN:
				propertyType = EdmPrimitiveTypeKind.Boolean.getFullQualifiedName().getFullQualifiedNameAsString();
				value = paramValue == null ? null : ConverterUtil.convertToBoolean(paramValue);
				break;
			case BINARY:
				propertyType = EdmPrimitiveTypeKind.Binary.getFullQualifiedName().getFullQualifiedNameAsString();
				value = getBytesFromBase64String(paramValue);
				break;
			case BYTE:
				propertyType = EdmPrimitiveTypeKind.Byte.getFullQualifiedName().getFullQualifiedNameAsString();
				value = paramValue;
				break;
			case SBYTE:
				propertyType = EdmPrimitiveTypeKind.SByte.getFullQualifiedName().getFullQualifiedNameAsString();
				value = paramValue;
				break;
			case DATE:
				propertyType = EdmPrimitiveTypeKind.Date.getFullQualifiedName().getFullQualifiedNameAsString();
				value = ConverterUtil.convertToDate(paramValue);
				break;
			case DURATION:
				propertyType = EdmPrimitiveTypeKind.Duration.getFullQualifiedName().getFullQualifiedNameAsString();
				value = paramValue;
				break;
			case DECIMAL:
				propertyType = EdmPrimitiveTypeKind.Decimal.getFullQualifiedName().getFullQualifiedNameAsString();
				value = paramValue == null ? null : ConverterUtil.convertToBigDecimal(paramValue);
				break;
			case SINGLE:
				propertyType = EdmPrimitiveTypeKind.Single.getFullQualifiedName().getFullQualifiedNameAsString();
				value = paramValue == null ? null : ConverterUtil.convertToFloat(paramValue);
				break;
			case TIMEOFDAY:
				propertyType = EdmPrimitiveTypeKind.TimeOfDay.getFullQualifiedName().getFullQualifiedNameAsString();
				value = ConverterUtil.convertToDateTime(paramValue);
				break;
			case INT64:
				propertyType = EdmPrimitiveTypeKind.Int64.getFullQualifiedName().getFullQualifiedNameAsString();
				value = paramValue == null ? null : ConverterUtil.convertToLong(paramValue);
				break;
			case DATE_TIMEOFFSET:
				propertyType =
						EdmPrimitiveTypeKind.DateTimeOffset.getFullQualifiedName().getFullQualifiedNameAsString();
				value = ConverterUtil.convertToTime(paramValue);
				break;
			case GUID:
				propertyType = EdmPrimitiveTypeKind.Guid.getFullQualifiedName().getFullQualifiedNameAsString();
				value = paramValue;
				break;
			case STREAM:
				propertyType = EdmPrimitiveTypeKind.Stream.getFullQualifiedName().getFullQualifiedNameAsString();
				value = paramValue;
				break;
			case GEOGRAPHY:
				propertyType = EdmPrimitiveTypeKind.Geography.getFullQualifiedName().getFullQualifiedNameAsString();
				value = paramValue;
				break;
			case GEOGRAPHY_POINT:
				propertyType = EdmPrimitiveTypeKind.GeographyPoint.getFullQualifiedName()
				                                                  .getFullQualifiedNameAsString();
				value = paramValue;
				break;
			case GEOGRAPHY_LINE_STRING:
				propertyType = EdmPrimitiveTypeKind.GeographyLineString.getFullQualifiedName()
				                                                       .getFullQualifiedNameAsString();
				value = paramValue;
				break;
			case GEOGRAPHY_POLYGON:
				propertyType = EdmPrimitiveTypeKind.GeographyPolygon.getFullQualifiedName()
				                                                    .getFullQualifiedNameAsString();
				value = paramValue;
				break;
			case GEOGRAPHY_MULTIPOINT:
				propertyType = EdmPrimitiveTypeKind.GeographyMultiPoint.getFullQualifiedName()
				                                                       .getFullQualifiedNameAsString();
				value = paramValue;
				break;
			case GEOGRAPHY_MULTILINE_STRING:
				propertyType = EdmPrimitiveTypeKind.GeographyMultiLineString.getFullQualifiedName()
				                                                            .getFullQualifiedNameAsString();
				value = paramValue;
				break;
			case GEOGRAPHY_MULTIPOLYGON:
				propertyType = EdmPrimitiveTypeKind.GeographyMultiPolygon.getFullQualifiedName()
				                                                         .getFullQualifiedNameAsString();
				value = paramValue;
				break;
			case GEOGRAPHY_COLLECTION:
				propertyType = EdmPrimitiveTypeKind.GeographyCollection.getFullQualifiedName()
				                                                       .getFullQualifiedNameAsString();
				value = paramValue;
				break;
			case GEOMETRY:
				propertyType = EdmPrimitiveTypeKind.Geometry.getFullQualifiedName().getFullQualifiedNameAsString();
				value = paramValue;
				break;
			case GEOMETRY_POINT:
				propertyType = EdmPrimitiveTypeKind.GeometryPoint.getFullQualifiedName().getFullQualifiedNameAsString();
				value = paramValue;
				break;
			case GEOMETRY_LINE_STRING:
				propertyType = EdmPrimitiveTypeKind.GeometryLineString.getFullQualifiedName()
				                                                      .getFullQualifiedNameAsString();
				value = paramValue;
				break;
			case GEOMETRY_POLYGON:
				propertyType = EdmPrimitiveTypeKind.GeometryPolygon.getFullQualifiedName()
				                                                   .getFullQualifiedNameAsString();
				value = paramValue;
				break;
			case GEOMETRY_MULTIPOINT:
				propertyType = EdmPrimitiveTypeKind.GeometryMultiPoint.getFullQualifiedName()
				                                                      .getFullQualifiedNameAsString();
				value = paramValue;
				break;
			case GEOMETRY_MULTILINE_STRING:
				propertyType = EdmPrimitiveTypeKind.GeographyMultiLineString.getFullQualifiedName()
				                                                            .getFullQualifiedNameAsString();
				value = paramValue;
				break;
			case GEOMETRY_MULTIPOLYGON:
				propertyType = EdmPrimitiveTypeKind.GeometryMultiPolygon.getFullQualifiedName()
				                                                        .getFullQualifiedNameAsString();
				value = paramValue;
				break;
			case GEOMETRY_COLLECTION:
				propertyType = EdmPrimitiveTypeKind.GeometryCollection.getFullQualifiedName()
				                                                      .getFullQualifiedNameAsString();
				value = paramValue;
				break;
			default:
				propertyType = EdmPrimitiveTypeKind.String.getFullQualifiedName().getFullQualifiedNameAsString();
				value = paramValue;
				break;
		}
		return new Property(propertyType, name, ValueType.PRIMITIVE, value);
	}

	/**
	 * This method updates entity in tables where the request doesn't specify the odata e-tag.
	 *
	 * @param edmEntityType Entity type
	 * @param entity        Entity
	 * @param keys          Keys
	 * @param merge         Merge
	 * @throws DataServiceFault
	 * @throws ODataApplicationException
	 */
	private void updateEntity(EdmEntityType edmEntityType, Entity entity, List<UriParameter> keys, boolean merge)
			throws DataServiceFault, ODataApplicationException {
		ODataEntry entry = new ODataEntry();
		for (UriParameter key : keys) {
			String value = key.getText();
			if (value.startsWith("'") && value.endsWith("'")) {
				value = value.substring(1, value.length() - 1);
			}
			entry.addValue(key.getName(), value);
		}
		for (String property : edmEntityType.getPropertyNames()) {
			Property updateProperty = entity.getProperty(property);
			if (isKey(edmEntityType, property)) {
				continue;
			}
			// the request payload might not consider ALL properties, so it can be null
			if (updateProperty == null) {
				// if a property has NOT been added to the request payload
				// depending on the HttpMethod, our behavior is different
				if (merge) {
					// as of the OData spec, in case of PATCH, the existing property is not touched
					continue;
				} else {
					// as of the OData spec, in case of PUT, the existing property is set to null (or to default value)
					entry.addValue(property, null);
					continue;
				}
			}
			EdmProperty propertyType = (EdmProperty) edmEntityType.getProperty(property);
			entry.addValue(property, readPrimitiveValueInString(propertyType, updateProperty.getValue()));
		}
		this.dataHandler.updateEntityInTable(edmEntityType.getName(), entry);
	}

	/**
	 * This method initialize the EDM Provider.
	 *
	 * @param configID id of the config
	 * @return CsdlEdmProvider
	 * @throws ODataServiceFault
	 * @see EDMProvider
	 */
	private CsdlEdmProvider initializeEdmProvider(String configID) throws ODataServiceFault {
		return new EDMProvider(this.dataHandler.getTableList(), configID, this.namespace, getPropertiesMap(),
		                       getKeysCsdlMap(), this.dataHandler.getTableList(),
		                       this.dataHandler.getNavigationProperties());

	}
}
