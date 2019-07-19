/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2019
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

package org.geotoolkit.eop.xml.v100;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour EarthObservationEquipmentPropertyType complex type.
 *
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 *
 * <pre>
 * &lt;complexType name="EarthObservationEquipmentPropertyType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://earth.esa.int/eop}EarthObservationEquipment"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EarthObservationEquipmentPropertyType", propOrder = {
    "earthObservationEquipment"
})
public class EarthObservationEquipmentPropertyType {

    @XmlElement(name = "EarthObservationEquipment", required = true)
    protected EarthObservationEquipmentType earthObservationEquipment;

    /**
     * Obtient la valeur de la propriété earthObservationEquipment.
     *
     * @return
     *     possible object is
     *     {@link EarthObservationEquipmentType }
     *
     */
    public EarthObservationEquipmentType getEarthObservationEquipment() {
        return earthObservationEquipment;
    }

    /**
     * Définit la valeur de la propriété earthObservationEquipment.
     *
     * @param value
     *     allowed object is
     *     {@link EarthObservationEquipmentType }
     *
     */
    public void setEarthObservationEquipment(EarthObservationEquipmentType value) {
        this.earthObservationEquipment = value;
    }

}