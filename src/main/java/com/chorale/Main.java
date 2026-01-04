package com.chorale;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class Main {
    public static void main(String[] args){
        // creer l'instance vertx
        Vertx vertx = Vertx.vertx();

        // configuration de la connexion MySQL
        MySQLConnectOptions connectOptions = new MySQLConnectOptions()
                .setPort(3306)
                .setHost("localhost")
                .setDatabase("chorale_db")
                .setUser("root")
                .setPassword("");

        // configuration du Pool de connexion
        PoolOptions poolOptions = new PoolOptions()
                .setMaxSize(5);

        // creer le pool MySQL
        MySQLPool client = MySQLPool.pool(vertx, connectOptions, poolOptions);

        // creer le routeur
        Router router = Router.router(vertx);

        // activer CORS
        router.route().handler(io.vertx.ext.web.handler.CorsHandler.create(".*.")
                .allowedMethod(io.vertx.core.http.HttpMethod.GET)
                .allowedMethod(io.vertx.core.http.HttpMethod.POST)
                .allowedMethod(io.vertx.core.http.HttpMethod.PUT)
                .allowedMethod(io.vertx.core.http.HttpMethod.DELETE)
                .allowedHeader("Content-Type")
                .allowedHeader("Authorization"));

        // route 1: page d'accueil
        router.get("/").handler(ctx -> {
            ctx.response()
                    .putHeader("Content-Type", "text/plain")
                    .end("Bienvenue sur l'API de gestion de la chorale Sainte Rita");
        });

        // route 2: liste des chants
        router.get("/api/songs").handler(ctx -> {
            ctx.response()
                    .putHeader("Content-Type", "text/plain")
                    .end("{\"message\": \"Liste des membres ici\"}");
        });

        // route 3: liste des membres
        router.get("/api/members").handler(ctx -> {
            client.query("SELECT userId, firstName, lastName, email, phone, voicePart, role FROM users")
                    .execute()
                    .onSuccess(rows -> {
                        JsonArray members = new JsonArray();
                        rows.forEach(row -> {
                            JsonObject member = new JsonObject()
                                    .put("id", row.getLong("userId"))
                                    .put("firstname", row.getString("firstName"))
                                    .put("lastname", row.getString("lastName"))
                                    .put("email", row.getString("email"))
                                    .put("phone", row.getString("phone"))
                                    .put("voicePart", row.getString("voicePart"))
                                    .put("role", row.getString("role"));
                            members.add(member);
                        });
                        ctx.response()
                                .putHeader("Content-Type", "application/json")
                                .end(members.encodePrettily());
                    })
                    .onFailure(err -> {
                        ctx.response()
                                .setStatusCode(500)
                                .end("Erreur: " + err.getMessage());
                    });
        });

        // route 4: liste des evenements
        router.get("/api/events").handler(ctx -> {
            ctx.response()
                    .putHeader("Content-Type", "text/plain")
                    .end("{\"message\": \"Liste des evenements\"}");
        });

        // creer le serveur avec le routeur
        HttpServer server = vertx.createHttpServer();
        server.requestHandler(router);

        // demarrage du serveur
        server.listen(8080)
                .onSuccess(s -> {
                    System.out.println("Serveur demarre sur http://localhost:8080");
                    System.out.println("Routes disponibles:");
                    System.out.println(" - GET http://localhost:8080/");
                    System.out.println(" - GET http://localhost:8080/api/members");
                    System.out.println(" - GET http://localhost:8080/api/songs");
                    System.out.println(" - GET http://localhost:8080/api/events");
                })
                .onFailure(err -> {
                    System.out.println("Erreur: " + err.getMessage());
                });
    }
}
