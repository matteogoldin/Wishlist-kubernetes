package app;

import api.router.WishlistRouter;
import api.service.WishlistService;
import daos.WishlistDAO;
import io.javalin.Javalin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Entry point for the REST application.
 * Starts Javalin, wires the layers and registers the routes.
 *
 * CLI configurable parameters:
 *   --persistence-unit  JPA persistence unit name  (default: wishlists-pu)
 *   --port              HTTP port to listen on      (default: 8080)
 *
 * Environment variable overrides (used in Docker/Kubernetes containers):
 *   PORT         HTTP port (overrides --port)
 *   DB_URL       full JDBC URL (e.g. jdbc:mysql://mysql-svc:3306/wishlists)
 *   DB_USER      database username
 *   DB_PASSWORD  database password
 */
@Command(
    name = "wishlist-rest",
    mixinStandardHelpOptions = true,
    description = "Starts the Wishlist REST API (Javalin + JPA/Hibernate)"
)
public class WishlistRestApp implements Callable<Void> {

    private static final Logger LOGGER = LogManager.getLogger(WishlistRestApp.class);

    @Option(names = {"--persistence-unit"}, description = "Persistence Unit JPA name")
    private String persistenceUnit = "wishlists-pu";

    @Option(names = {"--port"}, description = "HTTP port (default: 8080; overridden by PORT env var)")
    private int port = 8080;

    public static void main(String[] args) {
        new CommandLine(new WishlistRestApp()).execute(args);
    }

    @Override
    public Void call() {
        // PORT env var overrides the CLI argument (Docker/Kubernetes friendly)
        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.isBlank()) {
            try {
                port = Integer.parseInt(envPort.trim());
            } catch (NumberFormatException e) {
                LOGGER.warn("Invalid PORT env var '{}', using default {}", envPort, port);
            }
        }

        LOGGER.info("Starting Wishlist REST API on port {} with PU '{}'", port, persistenceUnit);

        // ── override DB properties from environment variables ───────────────────
        Map<String, String> overrides = buildDbOverrides();

        // ── layer wiring ────────────────────────────────────────────────────────
        WishlistDAO     dao     = new WishlistDAO(persistenceUnit, overrides);
        WishlistService service = new WishlistService(dao);
        WishlistRouter  router  = new WishlistRouter(service);

        // ── Javalin 7 setup: routes declared inside config ──────────────────────
        Javalin app = Javalin.create(config -> {
            config.bundledPlugins.enableDevLogging();
            router.registerRoutes(config);
        }).start(port);

        LOGGER.info("Wishlist REST API ready at http://0.0.0.0:{}", port);

        // Shutdown hook: gracefully stops Javalin when the process terminates
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutting down Wishlist REST API...");
            app.stop();
        }));

        return null;
    }

    /**
     * Reads DB_URL, DB_USER, DB_PASSWORD from environment variables.
     * Returns an empty map if not defined: the PU will use the default
     * values from persistence.xml.
     */
    private Map<String, String> buildDbOverrides() {
        Map<String, String> props = new HashMap<>();
        String dbUrl      = System.getenv("DB_URL");
        String dbUser     = System.getenv("DB_USER");
        String dbPassword = System.getenv("DB_PASSWORD");

        if (dbUrl != null && !dbUrl.isBlank()) {
            props.put("jakarta.persistence.jdbc.url", dbUrl);
        }
        if (dbUser != null && !dbUser.isBlank()) {
            props.put("jakarta.persistence.jdbc.user", dbUser);
        }
        if (dbPassword != null && !dbPassword.isBlank()) {
            props.put("jakarta.persistence.jdbc.password", dbPassword);
        }
        return props;
    }
}
