package scoring.handlers;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;
import scoring.services.CreditScoreService;

import java.util.Map;

import static fi.iki.elonen.NanoHTTPD.IHTTPSession;
import static fi.iki.elonen.NanoHTTPD.MIME_PLAINTEXT;
import static fi.iki.elonen.NanoHTTPD.Response.Status;
import static fi.iki.elonen.router.RouterNanoHTTPD.UriResource;
import static scoring.ScoringServer.END_POINT_PATH_SCORE;
import static scoring.handlers.SessionUtils.convertCustomerFormData;

public class CreditScoreHandler extends NotImplementedHandler {

    private final static CreditScoreService creditScoreService = CreditScoreService.getInstance();

    @Override
    public NanoHTTPD.Response get(UriResource uriResource, Map<String, String> urlParams,
            IHTTPSession session) {
        try {
            var customer = convertCustomerFormData(session);
            var creditScore = creditScoreService.findCustomerCreditScore(customer, true);

            if (creditScore != null) {
                return NanoHTTPD.newFixedLengthResponse(Status.OK, MIME_PLAINTEXT,
                        "Customer score: " + creditScore.getCreditScore());
            } else {
                return NanoHTTPD.newFixedLengthResponse(Status.NOT_FOUND, MIME_PLAINTEXT,
                        "Unknown customer: " + customer.getFirstName() + " " + customer.getLastName());
            }
        } catch (NanoHTTPD.ResponseException ex) {
            return NanoHTTPD.newFixedLengthResponse(ex.getStatus(), NanoHTTPD.MIME_PLAINTEXT, ex.getMessage());
        } catch (IllegalStateException ex) {
            return NanoHTTPD.newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT,
                    "Sorry for inconvenience. We are working hard to fix the issue.");
        }
    }

    @Override
    public NanoHTTPD.Response post(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams,
            IHTTPSession session) {
        try {
            var customer = convertCustomerFormData(session);
            var creditScore = creditScoreService.findCustomerCreditScore(customer, false);
            if (creditScore == null) {
                creditScore = creditScoreService.performCustomerScoring(customer);
            }

            var response = NanoHTTPD.newFixedLengthResponse(Status.CREATED, MIME_PLAINTEXT,
                    "Customer score: " + creditScore.getCreditScore());
            response.addHeader("Location", END_POINT_PATH_SCORE + "/" + creditScore.getCustomerId());
            return response;
        } catch (NanoHTTPD.ResponseException ex) {
            return NanoHTTPD.newFixedLengthResponse(ex.getStatus(), NanoHTTPD.MIME_PLAINTEXT, ex.getMessage());
        } catch (IllegalStateException ex) {
            return NanoHTTPD.newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT,
                    "Sorry for inconvenience. We are working hard to fix the issue.");
        }
    }
}
