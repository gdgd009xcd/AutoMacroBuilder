package org.zaproxy.zap.extension.automacrobuilder.mdepend;

import org.zaproxy.zap.extension.automacrobuilder.InterfaceClientRequest;
import org.zaproxy.zap.extension.automacrobuilder.PRequest;
import org.zaproxy.zap.extension.automacrobuilder.PRequestResponse;
import org.zaproxy.zap.extension.automacrobuilder.ParmGenMacroTrace;


import java.io.IOException;
import java.nio.charset.Charset;


public class ClientRequest implements InterfaceClientRequest {
    private static org.apache.logging.log4j.Logger LOGGER4J =
            org.apache.logging.log4j.LogManager.getLogger();
    /**
     * send client HttpRequest
     *
     * @param request
     * @return
     */
    @Override
    public PRequestResponse clientRequest(ParmGenMacroTrace pmt, PRequest request) {
        return pmt.clientHttpRequest(request, pmt.getSequenceEncode());
    }


}
