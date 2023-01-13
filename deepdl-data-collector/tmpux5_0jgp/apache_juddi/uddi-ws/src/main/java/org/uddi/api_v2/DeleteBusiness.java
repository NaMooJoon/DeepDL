/*
 * Copyright 2001-2008 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.uddi.api_v2;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for delete_business complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="delete_business">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:uddi-org:api_v2}authInfo"/>
 *         &lt;element ref="{urn:uddi-org:api_v2}businessKey" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="generic" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "delete_business", propOrder = {
    "authInfo",
    "businessKey"
})
public class DeleteBusiness {

    @XmlElement(required = true)
    protected String authInfo;
    @XmlElement(required = true)
    protected List<String> businessKey;
    @XmlAttribute(name = "generic", required = true)
    protected String generic;

    /**
     * Gets the value of the authInfo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAuthInfo() {
        return authInfo;
    }

    /**
     * Sets the value of the authInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAuthInfo(String value) {
        this.authInfo = value;
    }

    /**
     * Gets the value of the businessKey property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the businessKey property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBusinessKey().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getBusinessKey() {
        if (businessKey == null) {
            businessKey = new ArrayList<String>();
        }
        return this.businessKey;
    }

    /**
     * Gets the value of the generic property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGeneric() {
        return generic;
    }

    /**
     * Sets the value of the generic property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGeneric(String value) {
        this.generic = value;
    }

}
