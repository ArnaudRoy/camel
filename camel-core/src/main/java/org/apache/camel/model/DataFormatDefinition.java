/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.camel.CamelContext;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.RouteContext;
import org.apache.camel.util.IntrospectionSupport;
import org.apache.camel.util.ObjectHelper;

import static org.apache.camel.util.EndpointHelper.isReferenceParameter;

/**
 * Represents a Camel data format
 */
@Metadata(label = "dataformat,transformation")
@XmlType(name = "dataFormat")
@XmlAccessorType(XmlAccessType.FIELD)
public class DataFormatDefinition extends IdentifiedType {
    @XmlTransient
    private DataFormat dataFormat;
    @XmlTransient
    private String dataFormatName;

    public DataFormatDefinition() {
    }

    public DataFormatDefinition(DataFormat dataFormat) {
        this.dataFormat = dataFormat;
    }

    protected DataFormatDefinition(String dataFormatName) {
        this.dataFormatName = dataFormatName;
    }

    /**
     * Factory method to create the data format
     *
     * @param routeContext route context
     * @param type         the data format type
     * @param ref          reference to lookup for a data format
     * @return the data format or null if not possible to create
     */
    public static DataFormat getDataFormat(RouteContext routeContext, DataFormatDefinition type, String ref) {
        if (type == null) {
            ObjectHelper.notNull(ref, "ref or type");

            // try to let resolver see if it can resolve it, its not always possible
            type = ((ModelCamelContext) routeContext.getCamelContext()).resolveDataFormatDefinition(ref);

            if (type != null) {
                return type.getDataFormat(routeContext);
            }

            DataFormat dataFormat = routeContext.getCamelContext().resolveDataFormat(ref);
            if (dataFormat == null) {
                throw new IllegalArgumentException("Cannot find data format in registry with ref: " + ref);
            }

            return dataFormat;
        } else {
            return type.getDataFormat(routeContext);
        }
    }

    public DataFormat getDataFormat(RouteContext routeContext) {
        if (dataFormat == null) {

            // resolve properties before we create the data format
            try {
                ProcessorDefinitionHelper.resolvePropertyPlaceholders(routeContext, this);
            } catch (Exception e) {
                throw new IllegalArgumentException("Error resolving property placeholders on data format: " + this, e);
            }

            dataFormat = createDataFormat(routeContext);
            if (dataFormat != null) {
                configureDataFormat(dataFormat, routeContext.getCamelContext());
            } else {
                throw new IllegalArgumentException(
                        "Data format '" + (dataFormatName != null ? dataFormatName : "<null>") + "' could not be created. "
                                + "Ensure that the data format is valid and the associated Camel component is present on the classpath");
            }
        }
        return dataFormat;
    }

    /**
     * Factory method to create the data format instance
     */
    protected DataFormat createDataFormat(RouteContext routeContext) {
        // must use getDataFormatName() as we need special logic in json dataformat
        if (getDataFormatName() != null) {
            return routeContext.getCamelContext().resolveDataFormat(getDataFormatName());
        }
        return null;
    }

    /**
     * Allows derived classes to customize the data format
     *
     * @deprecated use {@link #configureDataFormat(org.apache.camel.spi.DataFormat, org.apache.camel.CamelContext)}
     */
    @Deprecated
    protected void configureDataFormat(DataFormat dataFormat) {
    }

    /**
     * Allows derived classes to customize the data format
     */
    protected void configureDataFormat(DataFormat dataFormat, CamelContext camelContext) {
    }

    /**
     * Sets a named property on the data format instance using introspection
     *
     * @deprecated use {@link #setProperty(org.apache.camel.CamelContext, Object, String, Object)}
     */
    @Deprecated
    protected void setProperty(Object bean, String name, Object value) {
        setProperty(null, bean, name, value);
    }

    /**
     * Sets a named property on the data format instance using introspection
     */
    protected void setProperty(CamelContext camelContext, Object bean, String name, Object value) {
        try {
            String ref = value instanceof String ? value.toString() : null;
            if (isReferenceParameter(ref) && camelContext != null) {
                IntrospectionSupport.setProperty(camelContext, camelContext.getTypeConverter(), bean, name, null, ref, true);
            } else {
                IntrospectionSupport.setProperty(bean, name, value);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to set property: " + name + " on: " + bean + ". Reason: " + e, e);
        }
    }

    public String getDataFormatName() {
        return dataFormatName;
    }

    public void setDataFormatName(String dataFormatName) {
        this.dataFormatName = dataFormatName;
    }

    public DataFormat getDataFormat() {
        return dataFormat;
    }

    public void setDataFormat(DataFormat dataFormat) {
        this.dataFormat = dataFormat;
    }

    public String getShortName() {
        String name = getClass().getSimpleName();
        if (name.endsWith("DataFormat")) {
            name = name.substring(0, name.indexOf("DataFormat"));
        }
        return name;
    }

}

