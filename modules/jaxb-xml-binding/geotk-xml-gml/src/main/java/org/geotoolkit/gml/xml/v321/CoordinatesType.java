/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2008 - 2012, Geomatys
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


package org.geotoolkit.gml.xml.v321;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;


/**
 * This type is deprecated for tuples with ordinate values that are numbers.
 * CoordinatesType is a text string, intended to be used to record an array of tuples or coordinates. 
 * While it is not possible to enforce the internal structure of the string through schema validation, some optional attributes have been provided in previous versions of GML to support a description of the internal structure. These attributes are deprecated. The attributes were intended to be used as follows:
 * Decimal	symbol used for a decimal point (default="." a stop or period)
 * cs        	symbol used to separate components within a tuple or coordinate string (default="," a comma)
 * ts        	symbol used to separate tuples or coordinate strings (default=" " a space)
 * Since it is based on the XML Schema string type, CoordinatesType may be used in the construction of tables of tuples or arrays of tuples, including ones that contain mixed text and numeric values.
 * 
 * <p>Java class for CoordinatesType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CoordinatesType">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *       &lt;attribute name="decimal" type="{http://www.w3.org/2001/XMLSchema}string" default="." />
 *       &lt;attribute name="cs" type="{http://www.w3.org/2001/XMLSchema}string" default="," />
 *       &lt;attribute name="ts" type="{http://www.w3.org/2001/XMLSchema}string" default=" " />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CoordinatesType", propOrder = {
    "value"
})
public class CoordinatesType {

    @XmlValue
    private String value;
    @XmlAttribute
    private String decimal;
    @XmlAttribute
    private String cs;
    @XmlAttribute
    private String ts;

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the value of the decimal property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDecimal() {
        if (decimal == null) {
            return ".";
        } else {
            return decimal;
        }
    }

    /**
     * Sets the value of the decimal property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDecimal(String value) {
        this.decimal = value;
    }

    /**
     * Gets the value of the cs property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCs() {
        if (cs == null) {
            return ",";
        } else {
            return cs;
        }
    }

    /**
     * Sets the value of the cs property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCs(String value) {
        this.cs = value;
    }

    /**
     * Gets the value of the ts property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTs() {
        if (ts == null) {
            return " ";
        } else {
            return ts;
        }
    }

    /**
     * Sets the value of the ts property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTs(String value) {
        this.ts = value;
    }

}
