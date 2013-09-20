/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2008 - 2009, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotoolkit.csw.xml.v202;

import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.geotoolkit.csw.xml.RecordProperty;

/**
 * <p>Java class for RecordPropertyType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RecordPropertyType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Value" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 * @module pending
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RecordPropertyType", propOrder = {
    "name",
    "value"
})
public class RecordPropertyType implements RecordProperty{

    @XmlElement(name = "Name", required = true)
    private String name;
    @XmlElement(name = "Value")
    private Object value;

    public RecordPropertyType() {

    }

    public RecordPropertyType(final String name, final Object value) {
        this.name  = name;
        this.value = value;
    }

    /**
     * Gets the value of the name property.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Gets the value of the value property.
     */
    @Override
    public Object getValue() {
        return value;
    }

    /**
     * Verify if this entry is identical to the specified object.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof RecordPropertyType) {
            final RecordPropertyType that = (RecordPropertyType) object;
            return Objects.equals(this.name,  that.name) &&
                   Objects.equals(this.value, that.value);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 59 * hash + (this.value != null ? this.value.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("[RecordPropertyType]").append('\n');

        if (name != null) {
            s.append("name: ").append(name).append('\n');
        }
        if (value != null) {
            s.append("value: ").append(value).append('\n');
        }
        return s.toString();
    }
}
