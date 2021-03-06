package scoring.handlers;

import fi.iki.elonen.NanoHTTPD;
import scoring.model.Customer;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SessionUtils {

    private SessionUtils() { }

    public static Customer convertCustomerFormData(NanoHTTPD.IHTTPSession session) throws NanoHTTPD.ResponseException {
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
            throw new NanoHTTPD.ResponseException(NanoHTTPD.Response.Status.BAD_REQUEST, "Malformed request body.", ex);
        }
    }

    private static String getParameterValueOrThrow(String key, Map<String, List<String>> formData)
            throws NanoHTTPD.ResponseException {
        var parameterValues = formData.get(key);
        if (parameterValues == null || parameterValues.isEmpty()) {
            throw new NanoHTTPD.ResponseException(NanoHTTPD.Response.Status.BAD_REQUEST,
                    "Malformed customer information. Missing mandatory field " + key);
        }

        return parameterValues.get(0);
    }

    public static int getIntParameterOrThrow(String key, Map<String, String> parameters)
            throws NanoHTTPD.ResponseException {
        try {
            return Integer.parseInt(parameters.get(key));
        } catch (NumberFormatException ex) {
            throw new NanoHTTPD.ResponseException(NanoHTTPD.Response.Status.BAD_REQUEST,
                    "Malformed request. Parameter " + key + " is missing or not of int format.");
        }
    }
}
