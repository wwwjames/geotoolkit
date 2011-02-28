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
package org.geotoolkit.sml.xml.v100;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.geotoolkit.sml.xml.AbstractContact;
import org.geotoolkit.util.Utilities;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice minOccurs="0">
 *         &lt;group ref="{http://www.opengis.net/sensorML/1.0}ContactGroup"/>
 *         &lt;element ref="{http://www.opengis.net/sensorML/1.0}ContactList"/>
 *       &lt;/choice>
 *       &lt;attGroup ref="{http://www.opengis.net/gml}AssociationAttributeGroup"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 * @module pending
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "person",
    "responsibleParty",
    "contactList"
})
@XmlRootElement(name = "Contact")
public class Contact implements AbstractContact {

    @XmlElement(name = "Person")
    private Person person;
    @XmlElement(name = "ResponsibleParty")
    private ResponsibleParty responsibleParty;
    @XmlElement(name = "ContactList")
    private ContactList contactList;
    @XmlAttribute
    private List<String> nilReason;
    @XmlAttribute(namespace = "http://www.opengis.net/gml")
    private String remoteSchema;
    @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
    private String actuate;
    @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
    private String arcrole;
    @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
    private String href;
    @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
    private String role;
    @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
    private String show;
    @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
    private String title;
    @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
    private String type;

    public Contact() {

    }

    public Contact(final String role, final ResponsibleParty responsibleParty) {
        this.role             = role;
        this.responsibleParty = responsibleParty;
    }

    public Contact(final ResponsibleParty responsibleParty) {
        this.responsibleParty = responsibleParty;
    }

    public Contact(final AbstractContact contact) {
        if (contact != null) {
            this.actuate = contact.getActuate();
            this.arcrole = contact.getArcrole();
            if (contact.getContactList() != null) {
                this.contactList = new ContactList(contact.getContactList());
            }
            this.href = contact.getHref();
            if (contact.getPerson() != null) {
                this.person = new Person(contact.getPerson());
            }
            this.remoteSchema = contact.getRemoteSchema();
            if (contact.getResponsibleParty() != null) {
                this.responsibleParty = new ResponsibleParty(contact.getResponsibleParty());
            }
            this.role = contact.getRole();
            this.show = contact.getShow();
            this.title = contact.getTitle();
            this.type = contact.getType();
        }
    }
    
    /**
     * Gets the value of the person property.
     */
    public Person getPerson() {
        return person;
    }

    /**
     * Sets the value of the person property.
     */
    public void setPerson(final Person value) {
        this.person = value;
    }

    /**
     * Gets the value of the responsibleParty property.
    */
    public ResponsibleParty getResponsibleParty() {
        return responsibleParty;
    }

    /**
     * Sets the value of the responsibleParty property.
     */
    public void setResponsibleParty(final ResponsibleParty value) {
        this.responsibleParty = value;
    }

    /**
     * Gets the value of the contactList property.
     */
    public ContactList getContactList() {
        return contactList;
    }

    /**
     * Sets the value of the contactList property.
     */
    public void setContactList(final ContactList value) {
        this.contactList = value;
    }

    /**
     * Sets the value of the contactList property.
     */
    public void setContactList(final ResponsibleParty value) {
        this.contactList = new ContactList(value);
    }
    /**
     * Sets the value of the contactList property.
     */
    public void setContactList(final Person value) {
        this.contactList = new ContactList(value);
    }

    /**
     * Gets the value of the nilReason property.
     */
    public List<String> getNilReason() {
        if (nilReason == null) {
            nilReason = new ArrayList<String>();
        }
        return this.nilReason;
    }

    /**
     * Gets the value of the remoteSchema property.
     */
    public String getRemoteSchema() {
        return remoteSchema;
    }

    /**
     * Sets the value of the remoteSchema property.
     */
    public void setRemoteSchema(final String value) {
        this.remoteSchema = value;
    }

    /**
     * Gets the value of the actuate property.
     */
    public String getActuate() {
        return actuate;
    }

    /**
     * Sets the value of the actuate property.
     */
    public void setActuate(final String value) {
        this.actuate = value;
    }

    /**
     * Gets the value of the arcrole property.
     */
    public String getArcrole() {
        return arcrole;
    }

    /**
     * Sets the value of the arcrole property.
     */
    public void setArcrole(final String value) {
        this.arcrole = value;
    }

    /**
     * Gets the value of the href property.
     */
    public String getHref() {
        return href;
    }

    /**
     * Sets the value of the href property.
     */
    public void setHref(final String value) {
        this.href = value;
    }

    /**
     * Gets the value of the role property.
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the value of the role property.
     */
    public void setRole(final String value) {
        this.role = value;
    }

    /**
     * Gets the value of the show property.
     */
    public String getShow() {
        return show;
    }

    /**
     * Sets the value of the show property.
     */
    public void setShow(final String value) {
        this.show = value;
    }

    /**
     * Gets the value of the title property.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     */
    public void setTitle(final String value) {
        this.title = value;
    }

    /**
     * Gets the value of the type property.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     */
    public void setType(final String value) {
        this.type = value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[Contact]").append("\n");
        if (person != null) {
            sb.append("person: ").append(person).append('\n');
        }
        if (responsibleParty != null) {
            sb.append("responsibleParty: ").append(responsibleParty).append('\n');
        }
        if (contactList != null) {
            sb.append("contactList: ").append(contactList).append('\n');
        }
        if (nilReason != null) {
            sb.append("nilReason:").append('\n');
            for (String k : nilReason) {
                sb.append("nilReason: ").append(k).append('\n');
            }
        }
        if (remoteSchema != null) {
            sb.append("remoteSchema: ").append(remoteSchema).append('\n');
        }
        if (actuate != null) {
            sb.append("actuate: ").append(actuate).append('\n');
        }
        if (arcrole != null) {
            sb.append("arcrole: ").append(arcrole).append('\n');
        }
        if (href != null) {
            sb.append("href: ").append(href).append('\n');
        }
        if (role != null) {
            sb.append("role: ").append(role).append('\n');
        }
        if (show != null) {
            sb.append("show: ").append(show).append('\n');
        }
        if (title != null) {
            sb.append("title: ").append(title).append('\n');
        }
        if (type != null) {
            sb.append("type: ").append(type).append('\n');
        }
        return sb.toString();
    }

    /**
     * Verify if this entry is identical to specified object.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }

        if (object instanceof Contact) {
            final Contact that = (Contact) object;
            return Utilities.equals(this.actuate,      that.actuate)       &&
                   Utilities.equals(this.arcrole,      that.arcrole)       &&
                   Utilities.equals(this.href,         that.href)          &&
                   Utilities.equals(this.nilReason,    that.nilReason)     &&
                   Utilities.equals(this.remoteSchema, that.remoteSchema)  &&
                   Utilities.equals(this.role,         that.role)          &&
                   Utilities.equals(this.show,         that.show)          &&
                   Utilities.equals(this.title,        that.title)         &&
                   Utilities.equals(this.contactList,  that.contactList)   &&
                   Utilities.equals(this.responsibleParty, that.responsibleParty) &&
                   Utilities.equals(this.person,       that.person)        &&
                   Utilities.equals(this.type,         that.type);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 13 * hash + (this.person != null ? this.person.hashCode() : 0);
        hash = 13 * hash + (this.responsibleParty != null ? this.responsibleParty.hashCode() : 0);
        hash = 13 * hash + (this.contactList != null ? this.contactList.hashCode() : 0);
        hash = 13 * hash + (this.nilReason != null ? this.nilReason.hashCode() : 0);
        hash = 13 * hash + (this.remoteSchema != null ? this.remoteSchema.hashCode() : 0);
        hash = 13 * hash + (this.actuate != null ? this.actuate.hashCode() : 0);
        hash = 13 * hash + (this.arcrole != null ? this.arcrole.hashCode() : 0);
        hash = 13 * hash + (this.href != null ? this.href.hashCode() : 0);
        hash = 13 * hash + (this.role != null ? this.role.hashCode() : 0);
        hash = 13 * hash + (this.show != null ? this.show.hashCode() : 0);
        hash = 13 * hash + (this.title != null ? this.title.hashCode() : 0);
        hash = 13 * hash + (this.type != null ? this.type.hashCode() : 0);
        return hash;
    }


}
