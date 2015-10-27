/**
 *
 * Copyright (C) 2002-2012 "SYSNET International, Inc."
 * support@sysnetint.com [http://www.sysnetint.com]
 *
 * This file is part of OpenEMPI.
 *
 * OpenEMPI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.openhie.openempi.openpixpdq.v3;

import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service;

/**
 * This class was generated by Apache CXF 2.7.0
 * 2012-12-07T08:37:12.208-05:00
 * Generated source version: 2.7.0
 * 
 */
@WebServiceClient(name = "PIXConsumer_Service", 
                  wsdlLocation = "wsdl/PIXPDQManager.wsdl",
                  targetNamespace = "urn:ihe:iti:pixv3:2007") 
@org.apache.cxf.feature.Features (features = "org.apache.cxf.ws.addressing.WSAddressingFeature")
public class PIXConsumerService extends Service
{
    public final static URL WSDL_LOCATION;

    public final static QName SERVICE = new QName("urn:ihe:iti:pixv3:2007", "PIXConsumer_Service");
    public final static QName PIXConsumerPortSoap12 = new QName("urn:ihe:iti:pixv3:2007", "PIXConsumer_Port_Soap12");
    static {
        URL url = PIXConsumerService.class.getResource("/wsdl/PIXConsumer.wsdl");
        if (url == null) {
            java.util.logging.Logger.getLogger(PIXConsumerService.class.getName())
                .log(java.util.logging.Level.INFO, 
                     "Can not initialize the default wsdl from {0}", "wsdl/PIXPDQManager.wsdl");
        }       
        WSDL_LOCATION = url;
    }

    public PIXConsumerService(URL wsdlLocation) {
        super(wsdlLocation, new QName("urn:ihe:iti:pixv3:2007", "PIXConsumer_Service"));
    }

    public PIXConsumerService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public PIXConsumerService() {
        super(WSDL_LOCATION, SERVICE);
    }
    
    /**
     *
     * @return
     *     returns PIXConsumerPortType
     */
    @WebEndpoint(name = "PIXConsumer_Port_Soap12")
    public PIXConsumerPortType getPIXConsumerPortSoap12() {
        return super.getPort(PIXConsumerPortSoap12, PIXConsumerPortType.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns PIXConsumerPortType
     */
    @WebEndpoint(name = "PIXConsumer_Port_Soap12")
    public PIXConsumerPortType getPIXConsumerPortSoap12(WebServiceFeature... features) {
        return super.getPort(PIXConsumerPortSoap12, PIXConsumerPortType.class, features);
    }

}