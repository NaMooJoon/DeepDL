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
package org.uddi.v3_service;

import java.math.BigInteger;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import org.uddi.repl_v3.ChangeRecord;
import org.uddi.repl_v3.ChangeRecordIDType;
import org.uddi.repl_v3.DoPing;
import org.uddi.repl_v3.HighWaterMarkVectorType;
import org.uddi.repl_v3.NotifyChangeRecordsAvailable;
import org.uddi.repl_v3.TransferCustody;

/**
 * This portType defines all of the UDDI replication operations.
 *
 * This class was generated by the JAX-WS RI. JAX-WS RI 2.1.5-b03- Generated
 * source version: 2.1
 *
 * <p class="MsoBodyText">UDDI Replication defines four APIs. The first two
 * presented here are used to perform replication and issue notifications. The
 * latter ancillary APIs provide support for other aspects of UDDI
 * Replication.</p>
 *
 * <p class="MsoBodyText" style="margin-left:1.0in;text-indent:-.25in"><span
 * style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New
 * Roman&quot;">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * </span></span>get_changeRecords</p>
 *
 * <p class="MsoBodyText" style="margin-left:1.0in;text-indent:-.25in"><span
 * style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New
 * Roman&quot;">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * </span></span>notify_changeRecordsAvailable</p>
 *
 * <p class="MsoBodyText" style="margin-left:1.0in;text-indent:-.25in"><span
 * style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New
 * Roman&quot;">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * </span></span>do_ping</p>
 *
 * <p class="MsoBodyText" style="margin-left:1.0in;text-indent:-.25in"><span
 * style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New
 * Roman&quot;">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * </span></span>get_highWaterMarks</p>
 */
@WebService(name = "UDDI_Replication_PortType", targetNamespace = "urn:uddi-org:api_v3_portType")
@XmlSeeAlso({
    org.uddi.custody_v3.ObjectFactory.class,
    org.uddi.repl_v3.ObjectFactory.class,
    org.uddi.subr_v3.ObjectFactory.class,
    org.uddi.api_v3.ObjectFactory.class,
    org.uddi.vscache_v3.ObjectFactory.class,
    org.uddi.vs_v3.ObjectFactory.class,
    org.uddi.sub_v3.ObjectFactory.class,
    org.w3._2000._09.xmldsig_.ObjectFactory.class,
    org.uddi.policy_v3.ObjectFactory.class,
    org.uddi.policy_v3_instanceparms.ObjectFactory.class
})
public interface UDDIReplicationPortType extends Remote {

    /**
     * The get_changeRecords message is used to initiate the replication of
     * change records from one node to another. The caller, who wishes to
     * receive new change records, provides as part of the message a high water
     * mark vector. This is used by the replication source node to determine
     * what change records satisfy the caller’s request. <p
     * class="MsoBodyText">More specifically, the recipient determines the
     * particular change records that are returned by comparing the originating
     * USNs in the caller’s high water mark vector with the originating USNs of
     * each of the changes the recipient has seen from others or generated by
     * itself.&nbsp; The recipient SHOULD only return change records that have
     * originating USNs that are greater than those listed in the
     * changesAlreadySeen highWaterMarkVector and less than the limit required
     * by either the responseLimitCount or the responseLimitVector.</p>
     *
     * <p class="MsoBodyText">In nodes that support pre-bundled replication
     * responses, the recipient of the get_changeRecords message MAY return more
     * change records than requested by the caller.&nbsp; In this scenario, the
     * caller MUST also be prepared to deal with such redundant changes where a
     * USN is less than the USN specified in the changesAlreadySeen
     * highWaterMarkVector. </p>
     *
     * <p class="MsoBodyText">The response to a get_changeRecords message is a
     * changeRecords element. Under all circumstances, all change records
     * returned therein by the message recipient MUST be returned sorted in
     * increasing order according to the recipient’s local USN.</p>
     * 
     * <p><b>A node that is ready to initiate replication of change records held
     * at another node within the registry uses the get_changeRecords message.  
     * Part of the message is a high water mark vector that contains for each 
     * node of the registry the originating USN of the most recent change record
     * that has been successfully processed by the invocating node. The effect 
     * of receiving a get_changeRecords message causes a node to return to the 
     * calling node change records it has generated locally and processed from 
     * other nodes constrained by the directives of the high water mark vector 
     * specified. As such, by invoking get_changeRecords a node obtains from its
     * adjacent node all change records (constrained by the high water mark 
     * vector) the adjacent node has generated locally or successfully processed
     * from other nodes participating in the replication topology. What 
     * constitutes an adjacent node is governed by the replication communication
     * graph. Replication topology is controlled via a Replication Configuration
     * Structure. Amongst other parameters, the Replication Configuration 
     * Structure identifies one unique URL to represent the replication point, 
     * soapReplicationURL, of each of the nodes of the registry. 
     * </b></p>
     *
     * @param responseLimitVector responseLimitCount or responseLimitVector: A
     * caller MAY place an upper bound on the number of change records he wishes
     * to receive in response to this message by either providing a integer
     * responseLimitCount, or, using responseLimitVector, indicating for each
     * node in the graph the first change originating there that he does not
     * wish to be returned.
     * @param requestingNode requestingNode: The requestingNode element provides
     * the identity of the calling node. This is the unique key for the calling
     * node and SHOULD be specified within the Replication Configuration
     * Structure.
     * @param changesAlreadySeen changesAlreadySeen: The changesAlreadySeen
     * element, if present, indicates changes from each node that the requestor
     * has successfully processed, and thus which should not be resent, if
     * possible.
     * @param responseLimitCount responseLimitCount or responseLimitVector: A
     * caller MAY place an upper bound on the number of change records he wishes
     * to receive in response to this message by either providing a integer
     * responseLimitCount, or, using responseLimitVector, indicating for each
     * node in the graph the first change originating there that he does not
     * wish to be returned.
     * @return returns java.util.List<org.uddi.repl_v3.ChangeRecord> A node will
     * respond with the corresponding changeRecords.
     * @throws DispositionReportFaultMessage, RemoteException Processing an
     * inbound replication message may fail due to a server internal error. The
     * common behavior for all error cases is to return an E_fatalError error
     * code. Error reporting SHALL be that specified by Section 4.8 – Success
     * and Error Reporting of this specification.
     */
          @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    @WebResult(name = "changeRecords", targetNamespace = "urn:uddi-org:repl_v3", partName = "body")
    @WebMethod(operationName = "get_changeRecords", action = "get_changeRecords")
    public org.uddi.repl_v3.ChangeRecords getChangeRecords(
        @WebParam(partName = "body", name = "get_changeRecords", targetNamespace = "urn:uddi-org:repl_v3")
        org.uddi.repl_v3.GetChangeRecords body
    ) throws DispositionReportFaultMessage,RemoteException;
    /*
 @WebMethod(operationName = "get_changeRecords", action = "get_changeRecords")
    @WebResult(name = "changeRecord", targetNamespace = "urn:uddi-org:repl_v3")
    @RequestWrapper(localName = "get_changeRecords", targetNamespace = "urn:uddi-org:repl_v3", className = "org.uddi.repl_v3.GetChangeRecords")
    @ResponseWrapper(localName = "changeRecords", targetNamespace = "urn:uddi-org:repl_v3", className = "org.uddi.repl_v3.ChangeRecords")
    public List<ChangeRecord> getChangeRecords(
        @WebParam(name = "requestingNode", targetNamespace = "urn:uddi-org:repl_v3")
        String requestingNode,
        @WebParam(name = "changesAlreadySeen", targetNamespace = "urn:uddi-org:repl_v3")
        HighWaterMarkVectorType changesAlreadySeen,
        @WebParam(name = "responseLimitCount", targetNamespace = "urn:uddi-org:repl_v3")
        BigInteger responseLimitCount,
        @WebParam(name = "responseLimitVector", targetNamespace = "urn:uddi-org:repl_v3")
        HighWaterMarkVectorType responseLimitVector)
        throws DispositionReportFaultMessage, RemoteException;
*/
    /**
     * <p class="MsoBodyText">Nodes can inform other nodes that they have new
     * change records available for consumption by replication by using this
     * message. This provides a proactive means through which replication can be
     * initiated, potentially reducing the latency of the dissemination of
     * changes throughout the set of UDDI nodes.&nbsp; The
     * notify_changeRecordsAvailable message is the predecessor to the
     * get_changeRecords message.</p>
     *
     * <p class="MsoBodyText">Each node MUST respond with the message defined
     * within the Section <a href="#_Ref8980611 ">7.4.2.3</a> <i>Returns</i>
     * when a valid notify_changeRecordsAvailable message is received.&nbsp;
     * </p>
     *
     * <p class="MsoBodyText">At an interval set by policy after the origination
     * of new change records within its node, a node SHOULD send this message to
     * each of the other nodes with which it is configured to communicate this
     * message according to the currently configured communication graph. It
     * SHOULD ignore any response (errors or otherwise) returned by such
     * invocations.</p>
     *
     * @param body <p class="MsoBodyText"
     * style="margin-left:1.0in;text-indent:-.25in"><span
     * style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New
     * Roman&quot;">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
     * </span></span><b><i>notifyingNode</i></b>: The parameter to this message
     * indicates that the notifyingNode has available the indicated set of
     * changes for request via get_changeRecords. </p>
     *
     * <p class="MsoBodyText" style="margin-left:1.0in;text-indent:-.25in"><span
     * style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New
     * Roman&quot;">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
     * </span></span><b><i>changesAvailable</i></b>: When sending the
     * notify_changeRecordsAvailable message, a node shall provide a high water
     * mark vector identifying what changes it knows to exist both locally and
     * on other nodes with which it might have had communications. Typically, no
     * communication graph restrictions are present for the
     * notify_changeRecordsAvailable message.&nbsp; In the event that the
     * sending node does not know the USN for a specific node within the
     * CommunicationGraph, the changesAvailable element MAY contain a
     * highWaterMark for that node with an unspecified nodeID element. </p>
     *
     * <span
     * style="font-size:10.0pt;font-family:Arial;letter-spacing:-.25pt"></span>
     * Success reporting SHALL be that specified by Section 4.8 –
     * Success and Error Reporting of this specification.
     * @throws DispositionReportFaultMessage, RemoteException Processing an
     * inbound replication message may fail due to a server internal error. The
     * common behavior for all error cases is to return an E_fatalError error
     * code. Error reporting SHALL be that specified by Section 4.8 – Success
     * and Error Reporting of this specification.
     */
    @WebMethod(operationName = "notify_changeRecordsAvailable", action = "notify_changeRecordsAvailable")
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public void notifyChangeRecordsAvailable(
        @WebParam(name = "notify_changeRecordsAvailable", targetNamespace = "urn:uddi-org:repl_v3", partName = "body")
        NotifyChangeRecordsAvailable body)
        throws DispositionReportFaultMessage,RemoteException;

    /**
     * This UDDI API message provides the means by which the current existence
     * and replication readiness of a node may be obtained.
     *
     * @param body
     * @return returns java.lang.String The response to this message must
     * contain the operatorNodeID element of the pinged node.
     * @throws DispositionReportFaultMessage, RemoteException Processing an
     * inbound replication message may fail due to a server internal error. The
     * common behavior for all error cases is to return an E_fatalError error
     * code. Error reporting SHALL be that specified by Section 4.8 – Success
     * and Error Reporting of this specification.
     */
    @WebMethod(operationName = "do_ping", action = "do_ping")
    @WebResult(name = "operatorNodeID", targetNamespace = "urn:uddi-org:repl_v3", partName = "body")
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public String doPing(
        @WebParam(name = "do_ping", targetNamespace = "urn:uddi-org:repl_v3", partName = "body")
        DoPing body)
        throws DispositionReportFaultMessage, RemoteException;

    /**
     * This UDDI API message provides a means to obtain a list of highWaterMark
     * element containing the highest known USN for all nodes in the replication
     * graph.
     *
     * @return returns java.util.List<org.uddi.repl_v3.ChangeRecordIDType> <p
     * class="MsoBodyText">A highWaterMarks element is returned that contains a
     * list of highWaterMark elements listing the highest known USN for all
     * nodes in the replication communication graph. See Section <a
     * href="#_Ref52863431 ">7.2.4</a> <i>High Water Mark Vector</i> for
     * details.</p>
     *
     * <p class="MsoBodyText"><img
     * src="http://uddi.org/pubs/uddi-v3.0.2-20041019_files/image129.gif"
     * border="0" height="88" width="349"></p>
     *
     * <p class="MsoBodyText">If the highest originatingUSN for a specific node
     * within the registry is not known, then the responding node MUST return a
     * highWaterMark for that node with an originatingUSN of 0 (zero).</p>
     *
     * <p class="codeSample">&lt;highWaterMark&gt;</p>
     *
     * <p class="codeSample">&nbsp;&nbsp; &lt;nodeID&gt;…&lt;/nodeID&gt;</p>
     *
     * <p class="codeSample">&nbsp;&nbsp;
     * &lt;originatingUSN&gt;<b>0</b>&lt;/originatingUSN&gt;</p>
     *
     * <p class="codeSample">&lt;/highWaterMark&gt;</p>
     * @throws DispositionReportFaultMessage, RemoteException Processing an
     * inbound replication message may fail due to a server internal error. The
     * common behavior for all error cases is to return an E_fatalError error
     * code. Error reporting SHALL be that specified by Section 4.8 – Success
     * and Error Reporting of this specification.
     */
    @WebMethod(operationName = "get_highWaterMarks", action = "get_highWaterMarks")
    @WebResult(name = "highWaterMark", targetNamespace = "urn:uddi-org:repl_v3")
    @RequestWrapper(localName = "get_highWaterMarks", targetNamespace = "urn:uddi-org:repl_v3", className = "org.uddi.repl_v3.GetHighWaterMarks")
    @ResponseWrapper(localName = "highWaterMarks", targetNamespace = "urn:uddi-org:repl_v3", className = "org.uddi.repl_v3.HighWaterMarkVectorType")
    public List<ChangeRecordIDType> getHighWaterMarks()
        throws DispositionReportFaultMessage, RemoteException;

    /**
     * Invoked by the target node in a custody transfer operation in response to
     * transfer_entities, this API is used by the custodial node to ensure that
     * permission has been granted to transfer custody of the entities that the
     * target publisher has requested. The transfer_custody API is in the
     * replication namespace since it is sent from one node to another node in a
     * registry using replication.
     *
     * @param body <p class="MsoBodyText"
     * style="margin-left:1.0in;text-indent:-.25in"><span
     * style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New
     * Roman&quot;">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
     * </span></span><b><i>transferToken</i></b>: Required argument obtained
     * from the custodial node via a call to get_transferToken by the publisher
     * requesting a transfer of custody. The transferToken contains an opaque
     * token, an expiration date, and the identity of the custodial node.&nbsp;
     * The transferToken represents permission to transfer the entities that
     * have been identified via a prior call to the get_transferToken API. The
     * custodial node MUST verify that the transferToken has not expired and
     * that the businessKey and tModelKey elements that the target publisher has
     * provided in transfer_entities are allowed to be transferred as captured
     * in the transfer token’s opaqueToken.</p>
     *
     * <p class="MsoBodyText" style="margin-left:1.0in;text-indent:-.25in"><span
     * style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New
     * Roman&quot;">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
     * </span></span><b><i>keyBag</i></b>: One or more uddiKeys associated with
     * businessEntity or tModel entities that the target publisher is requesting
     * ownership of at the target node in the registry. The set of keys must be
     * the same as the set of keys in the keyBag of the get_transferToken API
     * call from which the given transferToken was once obtained.</p>
     *
     * <p class="MsoBodyText" style="margin-left:1.0in;text-indent:-.25in"><span
     * style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New
     * Roman&quot;">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
     * </span></span><b><i>transferOperationalInfo</i></b>: Required argument.
     * The accepting publisher’s authorizedName and the accepting node’s nodeID
     * are provided on input to the relinquishing custodial node to allow it to
     * update the operationalInfo associated with the entities whose custody is
     * being transferred. The authorizedName and nodeID elements are both
     * required. The accepting node’s nodeID is obtained via the Replication
     * Configuration structure as described in Section <a href="#_Ref8979701
     * ">7.5.2</a> <i>Configuration of a UDDI Node – operator element</i>. The
     * authorizedName is obtained from the call to transfer_entities by the
     * requesting publisher.</p>
     * 
     * <p class="MsoBodyText">The custodial node must verify that it has
     * granted permission to transfer the entities identified and that this
     * permission is still valid.&nbsp; This operation is comprised of two
     * steps:</p>
     *
     * <p class="MsoBodyText"
     * style="margin-left:1.0in;text-indent:-.25in">1.<span style="font:7.0pt
     * &quot;Times New Roman&quot;">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
     * </span>Verification that the transferToken was issued by it, that it has
     * not expired, that it represents the authority to transfer no more and no
     * less than those entities identified by the businessKey and tModelKey
     * elements and that all these entities are still valid and not yet
     * transferred. The transferToken is invalidated if any of these conditions
     * are not met.</p>
     *
     * <p class="MsoBodyText"
     * style="margin-left:1.0in;text-indent:-.25in">2.<span style="font:7.0pt
     * &quot;Times New Roman&quot;">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
     * </span>If the conditions above are met, the custodial node will prevent
     * any further changes to the entities identified by the businessKey and
     * tModelKey elements identified. The entity will remain in this state until
     * the replication stream indicates it has been successfully processed via
     * the replication stream.&nbsp; </p>
     *
     * <p class="MsoBodyText">Upon successful verification of the custody
     * transfer request by the custodial node, an empty message is returned by
     * it indicating the success of the request and acknowledging the custody
     * transfer.&nbsp; </p>
     *
     * <p class="MsoBodyText">Following the issue of the empty message, the
     * custodial node will submit into the replication stream a
     * changeRecordNewData providing in the operationalInfo, the nodeID
     * accepting custody of the datum and the authorizedName of the publisher
     * accepting ownership. The acknowledgmentRequested attribute of this change
     * record MUST be set to "true".</p>
     *
     * <p class="MsoBodyText">Finally, the custodial node invalidates the
     * transferToken in order to prevent additional calls of the
     * transfer_entities API.</p>
     * @throws DispositionReportFaultMessage, RemoteException <p
     * class="MsoBodyText">If an error occurs in processing this API call, a
     * dispositionReport structure MUST be returned to the caller in a SOAP
     * Fault. See Section <a href="#_Ref8979747 ">4.8</a> <i>Success and Error
     * Reporting.&nbsp; </i>In addition to the errors common to all APIs, the
     * following error information is relevant here:</p>
     *
     * <p class="MsoBodyText" style="margin-left:1.0in;text-indent:-.25in"><span
     * style="font-family:Symbol">·<span style="font:7.0pt &quot;Times New
     * Roman&quot;">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
     * </span></span><b>E_transferNotAllowed</b>: signifies that the transfer of
     * one or more entities has been rejected by the custodial node.&nbsp;
     * Reasons for rejection include expiration of the transferToken and
     * attempts to transfer a set of entities that does not match the one
     * represented by the transferToken. The reason for rejecting the custody
     * transfer SHOULD be clearly indicated in the error text.<a
     * name="_Toc528997532"></a><a name="_Toc525464292"></a><a
     * name="_Toc535517200"></a></p>
     *
     * <p class="MsoBodyText" style="margin-left:1.0in;text-indent:-.25in"><a
     * name="_Toc42047326"><span style="font-family:Symbol">·<span
     * style="font:7.0pt &quot;Times New
     * Roman&quot;">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
     * </span></span><b>E_invalidKeyPassed</b>: signifies that one of the
     * <i>uddiKey</i> values passed for entities to be transferred did not match
     * with any known businessKey or tModelKey values. The key and element or
     * attribute that caused the problem SHOULD be clearly indicated in the
     * error text.</a></p>
     *     
* <h3><a name="_Toc45095949">Security Configuration for
     * transfer_custody</a></h3>
     *
     * <p class="MsoBodyText">The use of mutual authentication of UDDI nodes in
     * conjunction with the transfer_custody API is RECOMMENDED. This MAY be
     * achieved using mutual X.509v3 certificate-based authentication as
     * described in the Secure Sockets Layer (SSL) 3.0 protocol.&nbsp; SSL 3.0
     * with mutual authentication is represented by the tModel
     * uddi-org:mutualAuthenticatedSSL3 as described within Section <a
     * href="#_Ref8980795 ">11.3.2</a> <i>Secure Sockets Layer Version 3 with
     * Mutual Authentication</i>.</p>
     */
    @WebMethod(operationName = "transfer_custody", action = "transfer_custody")
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public void transferCustody(
        @WebParam(name = "transfer_custody", targetNamespace = "urn:uddi-org:repl_v3", partName = "body")
        TransferCustody body)
        throws DispositionReportFaultMessage, RemoteException;
}