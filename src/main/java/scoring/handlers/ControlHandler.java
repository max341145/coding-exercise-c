package scoring.handlers;

import fi.iki.elonen.NanoHTTPD;

import java.util.Map;

import static fi.iki.elonen.NanoHTTPD.IHTTPSession;
import static fi.iki.elonen.router.RouterNanoHTTPD.UriResource;
import static scoring.ScoringServer.SHUTDOWN_LATCH;

public class ControlHandler extends NotImplementedHandler {

    @Override
    public NanoHTTPD.Response get(UriResource uriResource, Map<String, String> urlParams,
            IHTTPSession session) {

        SHUTDOWN_LATCH.countDown();
        return NanoHTTPD.newFixedLengthResponse("The server will be shutdown in 1 second.");
    }
}
