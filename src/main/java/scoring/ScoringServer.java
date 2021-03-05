package scoring;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;
import org.apache.commons.lang3.concurrent.ConcurrentInitializer;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import scoring.handlers.CreditScoreHandler;

import java.io.IOException;

public class ScoringServer {

    public static final String END_POINT_PATH_SCORE = "/api/score";

    public static void main(String[] args) {

        final var server = new RouterNanoHTTPD(22800);
        server.addRoute(END_POINT_PATH_SCORE, CreditScoreHandler.class);
        server.setNotFoundHandler(RouterNanoHTTPD.Error404UriHandler.class);

        try {
            server.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        } catch (IOException ex) {
            System.err.println("Couldn't start server:\n" + ex);
            System.exit(-1);
        }

        System.out.println("Server started, Hit Enter to stop.\n");

        try {
            System.in.read();
        } catch (IOException ex) {
            // do nothing
        }

        server.stop();
        System.out.println("Server stopped.\n");
    }
}
