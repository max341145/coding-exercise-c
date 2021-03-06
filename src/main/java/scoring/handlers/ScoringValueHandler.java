package scoring.handlers;

import fi.iki.elonen.NanoHTTPD;
import scoring.services.CreditScoreService;

import java.util.Map;

import static fi.iki.elonen.NanoHTTPD.IHTTPSession;
import static fi.iki.elonen.NanoHTTPD.MIME_PLAINTEXT;
import static fi.iki.elonen.NanoHTTPD.Response.Status;
import static fi.iki.elonen.router.RouterNanoHTTPD.UriResource;
import static scoring.handlers.SessionUtils.getIntParameterOrThrow;

public class ScoringValueHandler extends NotImplementedHandler {

    private final static CreditScoreService creditScoreService = CreditScoreService.getInstance();

    @Override
    public NanoHTTPD.Response get(UriResource uriResource, Map<String, String> urlParams,
            IHTTPSession session) {
        try {
            var customerId = getIntParameterOrThrow("id", urlParams);
            var creditScore = creditScoreService.findCreditScoreById(customerId);

            if (creditScore != null) {
                return NanoHTTPD.newFixedLengthResponse(Status.OK, MIME_PLAINTEXT,
                        "Customer score: " + creditScore.getCreditScore());
            } else {
                return NanoHTTPD.newFixedLengthResponse(Status.NOT_FOUND, MIME_PLAINTEXT,
                        "Unknown id: " + customerId);
            }
        } catch (NanoHTTPD.ResponseException ex) {
            return NanoHTTPD.newFixedLengthResponse(ex.getStatus(), NanoHTTPD.MIME_PLAINTEXT, ex.getMessage());
        } catch (IllegalStateException ex) {
            return NanoHTTPD.newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT,
                    "Sorry for inconvenience. We are working hard to fix the issue.");
        }
    }
}
