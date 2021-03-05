package scoring.handlers;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import scoring.model.CreditScore;
import scoring.model.Customer;
import scoring.services.CreditScoreService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fi.iki.elonen.NanoHTTPD.IHTTPSession;
import static fi.iki.elonen.NanoHTTPD.MIME_PLAINTEXT;
import static fi.iki.elonen.NanoHTTPD.Response.Status;
import static fi.iki.elonen.router.RouterNanoHTTPD.UriResource;
import static scoring.ScoringServer.END_POINT_PATH_SCORE;

public class CreditScoreHandler extends RouterNanoHTTPD.NotImplementedHandler {

    private final static String CUSTOMER_ID_PARAM_NAME = "id";
    private final static String HTML_PAGE_ONLY_HEADER = "<html><body><h2>%s</h3></body></html>";

    @Override
    public String getText() {
        return String.format(HTML_PAGE_ONLY_HEADER, "Method not supported by the current api.");
    }

    @Override
    public NanoHTTPD.Response get(UriResource uriResource, Map<String, String> urlParams,
            IHTTPSession session) {
        final var creditScoreService = CreditScoreService.getInstance();

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
        final var creditScoreService = CreditScoreService.getInstance();

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

    private static Customer convertCustomerFormData(IHTTPSession session) throws NanoHTTPD.ResponseException {
        try {
            session.parseBody(new HashMap<>());
            var formData = session.getParameters();

            var result = new Customer();
            result.setFirstName(getParameterValueOrThrow("first-name", formData));
            result.setLastName(getParameterValueOrThrow("last-name", formData));
            var ageParameter = getParameterValueOrThrow("age", formData);
            result.setAge(Integer.parseInt(ageParameter));
            result.setDateOfBirth(getParameterValueOrThrow("date-of-birth", formData));
            var incomeParameter = getParameterValueOrThrow("income", formData);
            result.setAnnualIncome(Integer.parseInt(incomeParameter));

            return result;
        } catch (IOException ex) {
            throw new NanoHTTPD.ResponseException(Status.BAD_REQUEST, "Malformed request body.", ex);
        }
    }

    private static String getParameterValueOrThrow(String key, Map<String, List<String>> formData)
            throws NanoHTTPD.ResponseException {
        var parameterValues = formData.get(key);
        if (parameterValues == null || parameterValues.isEmpty()) {
            throw new NanoHTTPD.ResponseException(Status.BAD_REQUEST,
                    "Malformed customer information. Missing mandatory field " + key);
        }

        return parameterValues.get(0);
    }
}
