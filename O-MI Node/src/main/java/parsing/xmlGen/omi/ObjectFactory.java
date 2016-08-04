//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.06.24 at 01:40:38 PM MSK 
//


package parsing.xmlGen.omi;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the parsing.xmlGen.omi package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Msg_QNAME = new QName("omi.xsd", "msg");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: parsing.xmlGen.omi
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link OmiEnvelope }
     * 
     */
    public OmiEnvelope createOmiEnvelope() {
        return new OmiEnvelope();
    }

    /**
     * Create an instance of {@link ReadRequest }
     * 
     */
    public ReadRequest createReadRequest() {
        return new ReadRequest();
    }

    /**
     * Create an instance of {@link WriteRequest }
     * 
     */
    public WriteRequest createWriteRequest() {
        return new WriteRequest();
    }

    /**
     * Create an instance of {@link ResponseListType }
     * 
     */
    public ResponseListType createResponseListType() {
        return new ResponseListType();
    }

    /**
     * Create an instance of {@link CancelRequest }
     * 
     */
    public CancelRequest createCancelRequest() {
        return new CancelRequest();
    }

    /**
     * Create an instance of {@link RequestBaseType }
     * 
     */
    public RequestBaseType createRequestBaseType() {
        return new RequestBaseType();
    }

    /**
     * Create an instance of {@link RequestResultType }
     * 
     */
    public RequestResultType createRequestResultType() {
        return new RequestResultType();
    }

    /**
     * Create an instance of {@link ReturnType }
     * 
     */
    public ReturnType createReturnType() {
        return new ReturnType();
    }

    /**
     * Create an instance of {@link NodesType }
     * 
     */
    public NodesType createNodesType() {
        return new NodesType();
    }

    /**
     * Create an instance of {@link IdType }
     * 
     */
    public IdType createIdType() {
        return new IdType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Object }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "omi.xsd", name = "msg")
    public JAXBElement<Object> createMsg(Object value) {
        return new JAXBElement<Object>(_Msg_QNAME, Object.class, null, value);
    }

}
