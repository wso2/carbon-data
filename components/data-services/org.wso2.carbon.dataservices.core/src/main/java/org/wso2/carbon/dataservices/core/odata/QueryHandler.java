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

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmBindingTarget;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.CountOption;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByItem;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.SkipOption;
import org.apache.olingo.server.api.uri.queryoption.SkipTokenOption;
import org.apache.olingo.server.api.uri.queryoption.SystemQueryOptionKind;
import org.apache.olingo.server.api.uri.queryoption.TopOption;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.wso2.carbon.dataservices.core.odata.expression.ExpressionVisitorImpl;
import org.wso2.carbon.dataservices.core.odata.expression.operand.TypedOperand;
import org.wso2.carbon.dataservices.core.odata.expression.operand.VisitorOperand;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;

public class QueryHandler {
	protected static final OData oData;
	protected static final EdmPrimitiveType primBoolean;

	static {
		oData = OData.newInstance();
		primBoolean = oData.createPrimitiveTypeInstance(EdmPrimitiveTypeKind.Boolean);
	}

	private static final int MAX_PAGE_SIZE = 10;
	private static final String ES_SERVER_SIDE_PAGING = "ESServerSidePaging";

	public static void applyCountSystemQueryOption(final CountOption countOption, final EntityCollection entitySet) {
		if (countOption.getValue()) {
			entitySet.setCount(entitySet.getEntities().size());
		}
	}

	public static void applyFilterSystemQuery(final FilterOption filterOption, final EntityCollection entitySet,
	                                          final EdmBindingTarget edmEntitySet) throws ODataApplicationException {
		try {
			final Iterator<Entity> iter = entitySet.getEntities().iterator();

			while (iter.hasNext()) {
				final VisitorOperand operand = filterOption.getExpression()
				                                           .accept(new ExpressionVisitorImpl(iter.next(),
				                                                                             edmEntitySet));
				final TypedOperand typedOperand = operand.asTypedOperand();

				if (typedOperand.is(primBoolean)) {
					if (Boolean.FALSE.equals(typedOperand.getTypedValue(Boolean.class))) {
						iter.remove();
					}
				} else {
					throw new ODataApplicationException(
							"Invalid filter expression. Filter expressions must return a value of " +
							"type Edm.Boolean", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ROOT);
				}
			}

		} catch (ExpressionVisitException e) {
			throw new ODataApplicationException("Exception in filter evaluation",
			                                    HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ROOT);
		}
	}

	public static void applyTopSystemQueryOption(final TopOption topOption, final EntityCollection entitySet)
			throws ODataApplicationException {
		if (topOption.getValue() >= 0) {
			reduceToSize(entitySet, topOption.getValue());
		} else {
			throw new ODataApplicationException("Top value must be positive",
			                                    HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ROOT);
		}
	}

	private static void reduceToSize(final EntityCollection entitySet, final int n) {
		while (entitySet.getEntities().size() > n) {
			entitySet.getEntities().remove(entitySet.getEntities().size() - 1);
		}
	}

	public static void applySkipSystemQueryHandler(final SkipOption skipOption, final EntityCollection entitySet)
			throws ODataApplicationException {
		if (skipOption.getValue() >= 0) {
			popAtMost(entitySet, skipOption.getValue());
		} else {
			throw new ODataApplicationException("Skip value must be positive",
			                                    HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ROOT);
		}
	}

	private static void popAtMost(final EntityCollection entitySet, final int n) {
		final Iterator<Entity> iter = entitySet.getEntities().iterator();
		int i = 0;
		while (iter.hasNext() && i < n) {
			iter.next();
			iter.remove();
			i++;
		}
	}

	/**
	 * <p>Applies server-side paging to the given entity collection.</p>
	 * <p>The next link is constructed and set in the data. It must support client-specified
	 * page sizes. Therefore, the format <code>page*pageSize</code> (with a literal asterisk)
	 * has been chosen for the skiptoken.</p>
	 *
	 * @param skipTokenOption   the current skiptoken option (from a previous response's next link)
	 * @param entityCollection  the data
	 * @param edmEntitySet      the EDM entity set to decide whether paging must be done
	 * @param rawRequestUri     the request URI (used to construct the next link)
	 * @param preferredPageSize the client's preference for page size
	 * @return the chosen page size (or <code>null</code> if no paging has been done);
	 * could be used in the Preference-Applied HTTP header
	 * @throws ODataApplicationException
	 */
	public static Integer applyServerSidePaging(final SkipTokenOption skipTokenOption,
	                                            EntityCollection entityCollection, final EdmEntitySet edmEntitySet,
	                                            final String rawRequestUri, final Integer preferredPageSize)
			throws ODataApplicationException {

		if (edmEntitySet != null && shouldApplyServerSidePaging(edmEntitySet)) {
			final int pageSize = getPageSize(getPageSize(skipTokenOption), preferredPageSize);
			final int page = getPage(skipTokenOption);
			final int itemsToSkip = pageSize * page;

			if (itemsToSkip <= entityCollection.getEntities().size()) {
				popAtMost(entityCollection, itemsToSkip);
				final int remainingItems = entityCollection.getEntities().size();
				reduceToSize(entityCollection, pageSize);

				// Determine if a new next Link has to be provided.
				if (remainingItems > pageSize) {
					entityCollection.setNext(createNextLink(rawRequestUri, page + 1, pageSize));
				}
			} else {
				throw new ODataApplicationException("Nothing found.", HttpStatusCode.NOT_FOUND.getStatusCode(),
				                                    Locale.ROOT);
			}
			return pageSize;
		}
		return null;
	}

	private static URI createNextLink(final String rawRequestUri, final int page, final int pageSize)
			throws ODataApplicationException {
		// Remove a maybe existing skiptoken, making sure that the query part is not empty.
		String nextlink = rawRequestUri.contains("?") ?
		                  rawRequestUri.replaceAll("(\\$|%24)skiptoken=.+&?", "").replaceAll("(\\?|&)$", "") :
		                  rawRequestUri;
		// Add a question mark or an ampersand, depending on the current query part.
		nextlink += nextlink.contains("?") ? '&' : '?';
		// Append the new skiptoken.
		nextlink += SystemQueryOptionKind.SKIPTOKEN.toString().replace("$", "%24") + '=' + page + "%2A" +
		            pageSize;  // "%2A" is a percent-encoded asterisk

		try {
			return new URI(nextlink);
		} catch (final URISyntaxException e) {
			throw new ODataApplicationException("Exception while constructing next link",
			                                    HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ROOT, e);
		}
	}

	private static boolean shouldApplyServerSidePaging(final EdmEntitySet edmEntitySet) {
		return ES_SERVER_SIDE_PAGING.equals(edmEntitySet.getName());
	}

	private static int getPageSize(final int skipTokenPageSize, final Integer preferredPageSize) {
		return skipTokenPageSize > 0 ? skipTokenPageSize :
		       preferredPageSize == null || preferredPageSize >= MAX_PAGE_SIZE ? MAX_PAGE_SIZE : preferredPageSize;
	}

	private static int getPageSize(final SkipTokenOption skipTokenOption) throws ODataApplicationException {
		if (skipTokenOption != null && skipTokenOption.getValue().length() >= 3 &&
		    skipTokenOption.getValue().contains("*")) {
			final String value = skipTokenOption.getValue();
			try {
				return Integer.parseInt(value.substring(value.indexOf('*') + 1));
			} catch (final NumberFormatException e) {
				throw new ODataApplicationException("Invalid skip token", HttpStatusCode.BAD_REQUEST.getStatusCode(),
				                                    Locale.ROOT, e);
			}
		} else {
			return 0;
		}
	}

	private static int getPage(final SkipTokenOption skipTokenOption) throws ODataApplicationException {
		if (skipTokenOption != null && skipTokenOption.getValue().length() >= 3 &&
		    skipTokenOption.getValue().contains("*")) {
			final String value = skipTokenOption.getValue();
			try {
				return Integer.parseInt(value.substring(0, value.indexOf('*')));
			} catch (final NumberFormatException e) {
				throw new ODataApplicationException("Invalid skip token", HttpStatusCode.BAD_REQUEST.getStatusCode(),
				                                    Locale.ROOT, e);
			}
		} else {
			return 0;
		}
	}

	public static void applyOrderByOption(final OrderByOption orderByOption, final EntityCollection entitySet,
	                                       final EdmBindingTarget edmBindingTarget) {
		Collections.sort(entitySet.getEntities(), new Comparator<Entity>() {
			@Override
			@SuppressWarnings({ "unchecked", "rawtypes" })
			public int compare(final Entity e1, final Entity e2) {
				// Evaluate the first order option for both entity
				// If and only if the result of the previous order option is equals to 0
				// evaluate the next order option until all options are evaluated or they are not equals
				int result = 0;

				for (int i = 0; i < orderByOption.getOrders().size() && result == 0; i++) {
					try {
						final OrderByItem item = orderByOption.getOrders().get(i);
						final TypedOperand op1 =
								item.getExpression().accept(new ExpressionVisitorImpl(e1, edmBindingTarget))
								    .asTypedOperand();
						final TypedOperand op2 =
								item.getExpression().accept(new ExpressionVisitorImpl(e2, edmBindingTarget))
								    .asTypedOperand();

						if (op1.isNull() || op2.isNull()) {
							if (op1.isNull() && op2.isNull()) {
								result = 0; // null is equals to null
							} else {
								result = op1.isNull() ? -1 : 1;
							}
						} else {
							Object o1 = op1.getValue();
							Object o2 = op2.getValue();

							if (o1.getClass() == o2.getClass() && o1 instanceof Comparable) {
								result = ((Comparable) o1).compareTo(o2);
							} else {
								result = 0;
							}
						}
						result = item.isDescending() ? result * -1 : result;
					} catch (ExpressionVisitException | ODataApplicationException e) {
						throw new RuntimeException(e);
					}
				}
				return result;
			}
		});
	}

}
