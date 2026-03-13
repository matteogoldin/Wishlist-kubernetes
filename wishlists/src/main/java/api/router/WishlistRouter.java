package api.router;

import api.dto.WishlistDTO;
import api.service.WishlistService;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Optional;

public class WishlistRouter {

    private static final Logger LOGGER = LogManager.getLogger(WishlistRouter.class);

    private final WishlistService wishlistService;

    public WishlistRouter(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    public void registerRoutes(JavalinConfig config) {
        config.routes.get("/api/wishlists", this::getAll);
        config.routes.get("/api/wishlists/{id}", this::getById);
        config.routes.post("/api/wishlists", this::create);
        config.routes.delete("/api/wishlists/{id}", this::delete);

        config.routes.get("/health", ctx -> {
            LOGGER.debug("Health check requested");
            ctx.json(Map.of("status", "UP"));
        });
    }

    void getAll(Context ctx) {
        ctx.json(wishlistService.findAll());
    }

    void getById(Context ctx) {
        String id = ctx.pathParam("id");
        Optional<WishlistDTO> result = wishlistService.findById(id);
        if (result.isPresent()) {
            ctx.json(result.get());
        } else {
            ctx.status(404).json(Map.of("error", "Wishlist '" + id + "' not found"));
        }
    }

    void create(Context ctx) {
        try {
            WishlistDTO dto = ctx.bodyAsClass(WishlistDTO.class);
            WishlistDTO created = wishlistService.create(dto);
            ctx.status(201).json(created);
        } catch (IllegalArgumentException e) {
            ctx.status(409).json(Map.of("error", e.getMessage()));
        }
    }

    void delete(Context ctx) {
        String id = ctx.pathParam("id");
        try {
            wishlistService.delete(id);
            ctx.status(204);
        } catch (IllegalArgumentException e) {
            ctx.status(404).json(Map.of("error", e.getMessage()));
        }
    }
}