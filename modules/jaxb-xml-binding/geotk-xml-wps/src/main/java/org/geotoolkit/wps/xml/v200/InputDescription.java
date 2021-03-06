/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2016, Geomatys
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

package org.geotoolkit.wps.xml.v200;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import org.geotoolkit.ows.xml.v200.AdditionalParametersType;
import org.geotoolkit.ows.xml.v200.AllowedValues;
import org.geotoolkit.ows.xml.v200.CodeType;
import org.geotoolkit.ows.xml.v200.DomainMetadataType;
import org.geotoolkit.ows.xml.v200.KeywordsType;
import org.geotoolkit.ows.xml.v200.LanguageStringType;
import org.geotoolkit.ows.xml.v200.ValueType;
import org.geotoolkit.ows.xml.v200.ValuesReference;
import org.geotoolkit.wps.json.FormatDescription;


/**
 * Description of an input to a process.
 *
 *
 * In this use, the Description shall describe a process input.


 <p>Java class for InputDescription complex type.

 <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="InputDescription">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/wps/2.0}Description">
 *       &lt;choice>
 *         &lt;element ref="{http://www.opengis.net/wps/2.0}DataDescription"/>
 *         &lt;element name="Input" type="{http://www.opengis.net/wps/2.0}InputDescription" maxOccurs="unbounded"/>
 *       &lt;/choice>
 *       &lt;attGroup ref="{http://www.w3.org/2001/XMLSchema}occurs"/>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlType(name = "InputDescriptionType", propOrder = {
    "dataDescription",
    "input"
})
public class InputDescription extends Description {

    @XmlElementRef(required = false)
    protected DataDescription dataDescription;
    @XmlElement(name = "Input")
    protected List<InputDescription> input;
    @XmlAttribute
    @XmlSchemaType(name = "nonNegativeInteger")
    protected int minOccurs = 1;
    @XmlAttribute
    @XmlSchemaType(name = "allNNI")
    protected String maxOccurs;

    public InputDescription() {

    }

    public InputDescription(CodeType identifier, LanguageStringType title, List<LanguageStringType> _abstract,
           List<KeywordsType>keywords, Integer minOccur, String maxOccur, DataDescription dataDescription) {
        super(identifier, title, _abstract, keywords);
        this.minOccurs = minOccur;
        this.maxOccurs = maxOccur;
        this.dataDescription = dataDescription;
    }

    public InputDescription(CodeType identifier, LanguageStringType title, LanguageStringType _abstract,
            KeywordsType keywords, Integer minOccur, String maxOccur, DataDescription dataDescription) {
        super(identifier, title, _abstract, keywords);
        this.minOccurs = minOccur;
        this.maxOccurs = maxOccur;
        this.dataDescription = dataDescription;
    }

    public InputDescription(CodeType identifier, LanguageStringType title, LanguageStringType _abstract,
            KeywordsType keywords, List<AdditionalParametersType> additionalParams, Integer minOccur, String maxOccur, DataDescription dataDescription) {
        super(identifier, title, _abstract, keywords, additionalParams);
        this.minOccurs = minOccur;
        this.maxOccurs = maxOccur;
        this.dataDescription = dataDescription;
    }

    public InputDescription(org.geotoolkit.wps.json.InputType input) {
        super(input);

        final List<Format> formats = new ArrayList<>();
        for (FormatDescription f : input.getFormats()) {
            formats.add(new Format(f.getEncoding(), f.getMimeType(), f.getSchema(), f.getMaximumMegabytes(), f.isDefault()));
        }

        if (input.getMinOccurs() != null) {
            this.minOccurs = Integer.parseInt(input.getMinOccurs());
        }
        if (input.getMaxOccurs() != null) {
            this.maxOccurs = input.getMaxOccurs();
        }

        if (input.getLiteralDataDomain() != null && !input.getLiteralDataDomain().isEmpty()) {
            final List<LiteralDataDomain> lits = new ArrayList<>();
            for (org.geotoolkit.wps.json.LiteralDataDomain jsonLit : input.getLiteralDataDomain()) {
                LiteralDataDomain lit = new LiteralDataDomain();
                if (jsonLit.getDataType() != null) {
                    String value = jsonLit.getDataType().getName();
                    String reference = jsonLit.getDataType().getReference();
                    lit.setDataType(new DomainMetadataType(value, reference));
                }
                if (jsonLit.getAllowedValues()!= null) {
                    lit.setAllowedValues(new AllowedValues(jsonLit.getAllowedValues().getAllowedValues()));
                }
                if (jsonLit.getAllowedRanges()!= null) {
                    lit.setAllowedRanges(new AllowedValues(jsonLit.getAllowedRanges().getAllowedRanges()));
                }
                if (jsonLit.getDefaultValue()!= null) {
                    lit.setDefaultValue(new ValueType(jsonLit.getDefaultValue()));
                }
                if (jsonLit.getValuesReference()!= null) {
                    lit.setValuesReference(new ValuesReference(jsonLit.getValuesReference(), null));
                }
                lits.add(lit);
            }
            this.dataDescription = new LiteralData(formats, lits);
        } else if (input.getSupportedCRS() != null) {
            List<SupportedCRS> suportedCrs = new ArrayList<>();
            for (String crs : input.getSupportedCRS()) {
                suportedCrs.add(new SupportedCRS(crs));
            }
            this.dataDescription = new BoundingBoxData(formats, suportedCrs);
        } else {
            this.dataDescription = new ComplexData(formats);
        }
    }

    public DataDescription getDataDescription() {
        return dataDescription;
    }

    /**
     * Sets the value of the dataDescription property.
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link BoundingBoxData }{@code >}
     *     {@link JAXBElement }{@code <}{@link LiteralDataType }{@code >}
     *     {@link JAXBElement }{@code <}{@link ComplexDataType }{@code >}
     *     {@link JAXBElement }{@code <}{@link DataDescription }{@code >}
     *
     */
    public void setDataDescription(DataDescription value) {
        this.dataDescription = value;
    }

    /**
     * Gets the value of the input property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the input property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInput().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link InputDescription }
     *
     *
     */
    public List<InputDescription> getInput() {
        if (input == null) {
            input = new ArrayList<>();
        }
        return this.input;
    }

    /**
     * Gets the value of the minOccurs property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public int getMinOccurs() {
        return minOccurs;
    }

    /**
     * Sets the value of the minOccurs property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setMinOccurs(Integer value) {
        this.minOccurs = value;
    }

    /**
     * Gets the value of the maxOccurs property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public int getMaxOccurs() {
        if (maxOccurs == null) {
            return 1;
        } else if ("unbounded".equals(maxOccurs)) {
            return Integer.MAX_VALUE;
        } else {
            return Integer.parseInt(maxOccurs);
        }
    }

    /**
     * Sets the value of the maxOccurs property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setMaxOccurs(String value) {
        this.maxOccurs = value;
    }

}
