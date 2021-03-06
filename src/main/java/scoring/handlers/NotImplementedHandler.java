package scoring.handlers;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

import java.util.Map;

public class NotImplementedHandler implements RouterNanoHTTPD.UriResponder {

    private final static String HTML_PAGE_ONLY_HEADER = "<html><body><h2>%s</h3></body></html>";

    @Override
    public NanoHTTPD.Response get(RouterNanoHTTPD.UriResource uriResource,
            Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {

        var msg = String.format(HTML_PAGE_ONLY_HEADER, "Method not supported by the current api.");
        return NanoHTTPD.newFixedLengthResponse(msg);
    }

    @Override
    public NanoHTTPD.Response put(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams,
            NanoHTTPD.IHTTPSession session) {
        return get(uriResource, urlParams, session);
    }

    @Override
    public NanoHTTPD.Response post(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams,
            NanoHTTPD.IHTTPSession session) {
        return get(uriResource, urlParams, session);
    }

    @Override
    public NanoHTTPD.Response delete(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams,
            NanoHTTPD.IHTTPSession session) {
        return get(uriResource, urlParams, session);
    }

    @Override
    public NanoHTTPD.Response other(String method, RouterNanoHTTPD.UriResource uriResource,
            Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        return get(uriResource, urlParams, session);
    }
}
