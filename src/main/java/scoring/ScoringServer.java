package scoring;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;
import scoring.handlers.ControlHandler;
import scoring.handlers.CreditScoreHandler;
import scoring.handlers.ScoringValueHandler;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ScoringServer {

    public static final String END_POINT_PATH_SHUTDOWN = "/shutdown";
    public static final String END_POINT_PATH_SCORE = "/api/score";
    public static final String END_POINT_PATH_SCORE_BY_ID = "/api/score/:id";

    public static final CountDownLatch SHUTDOWN_LATCH = new CountDownLatch(1);

    private static volatile NanoHTTPD instance;

    public static void main(String[] args) {

        final Runnable onJVMShutdown = () -> {
            if (instance == null) return;
            instance.stop();
            System.out.println("\nServer has been stopped.");
        };
        Runtime.getRuntime().addShutdownHook(new Thread(onJVMShutdown));

        final var server = new RouterNanoHTTPD(22800);
        server.addRoute(END_POINT_PATH_SHUTDOWN, ControlHandler.class);
        server.addRoute(END_POINT_PATH_SCORE, CreditScoreHandler.class);
        server.addRoute(END_POINT_PATH_SCORE_BY_ID, ScoringValueHandler.class);
        server.setNotFoundHandler(RouterNanoHTTPD.Error404UriHandler.class);

        try {
            server.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
            instance = server;
        } catch (IOException ex) {
            System.err.println("Couldn't start server:\n" + ex);
            System.exit(-1);
        }

        System.out.println("Server started, Hit Ctrl+C to stop.\n");

        try {
            SHUTDOWN_LATCH.await();
            TimeUnit.SECONDS.sleep(1);
            System.exit(0);
        } catch (InterruptedException ex) {
            // do nothing. jvm shutdown hook comes in to play.
        }
    }
}
