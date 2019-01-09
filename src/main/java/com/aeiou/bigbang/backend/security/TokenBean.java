package com.aeiou.bigbang.backend.security;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for TokenBean complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TokenBean">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="loginName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="securityID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="geolocStatus" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@SuppressWarnings("serial")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TokenBean", propOrder = { "loginName", "securityID", "geolocStatus" })
public class TokenBean implements Serializable {

    public static String SECURITYID_NOT_AVAILABLE = "N/A";

    /** User's login */
    @XmlElement(required = true)
    private String loginName;
    /** Session id */
    private String securityID;

    public TokenBean(String loginName, String securityID) {
        this.loginName = loginName;
        this.securityID = securityID;
    }

    public TokenBean() {
    }

    /**
     * Gets the value of the loginName property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getLoginName() {
        return loginName;
    }

    /**
     * Sets the value of the loginName property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setLoginName(
            String value) {
        this.loginName = value;
    }

    /**
     * Gets the value of the securityID property.
     */
    public String getSecurityID() {
        return securityID;
    }

    /**
     * Sets the value of the securityID property.
     */
    public void setSecurityID(
            String value) {
        this.securityID = value;
    }

    @Override
    public String toString() {
        return "TokenBean [loginName=" + loginName + ", securityID=" + securityID + "]";
    }
}
