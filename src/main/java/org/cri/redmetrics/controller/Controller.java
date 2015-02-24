package org.cri.redmetrics.controller;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.cri.redmetrics.Server;
import org.cri.redmetrics.csv.CsvEntityConverter;
import org.cri.redmetrics.csv.CsvResponseTransformer;
import org.cri.redmetrics.dao.EntityDao;
import org.cri.redmetrics.json.JsonConverter;
import org.cri.redmetrics.model.Entity;
import org.cri.redmetrics.model.ResultsPage;
import org.cri.redmetrics.util.RouteHelper;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.*;
import java.util.stream.Collectors;

import static spark.Spark.*;

public abstract class Controller<E extends Entity, DAO extends EntityDao<E>> {

    public static final String basePath = "/v1/";
    public static final long defaultListCount = 50;
    public static final long maxListCount = 200;

    // Minimal wrapper class around an entity ID
    private class IdWrapper {
        IdWrapper(UUID id) { this.id = id; }
        public UUID id;
    }

    protected final String path;
    protected final DAO dao;
    protected final JsonConverter<E> jsonConverter;
    /*protected final CsvResponseTransformer<E> csvResponseTransformer;*/
    protected final RouteHelper routeHelper;


    Controller(String path, DAO dao, JsonConverter<E> jsonConverter, CsvEntityConverter<E> csvEntityConverter) {
        this.path = basePath + path;
        this.dao = dao;
        this.jsonConverter = jsonConverter;
        /*this.csvResponseTransformer = new CsvResponseTransformer<E>(csvEntityConverter);*/
        this.routeHelper = new RouteHelper(jsonConverter, new CsvResponseTransformer<E>(csvEntityConverter));
    }

    public final void publish() {
        publishGeneric();
        publishSpecific();
    }

    protected UUID idFromUrl(Request request) {
        String idParam = request.params(":id");
        return Entity.parseId(idParam);
    }

    protected UUID idFromQueryParam(Request request, String key) {
        String idParam = request.queryParams(key);
        return Entity.parseId(idParam);
    }

    private void publishGeneric() {
        // POST
        Route postRoute = (request, response) -> {
            // Is it a list or a single entity?
            JsonElement jsonElement = new JsonParser().parse(request.body());
            if(jsonElement.isJsonArray()) {
                Collection<E> entities = jsonConverter.parseCollection(request.body());
                for(E entity : entities) {
                    beforeCreation(entity, request, response);
                    create(entity);
                }

                // Return created status and list of entity IDs
                response.status(201);
                return entities.stream().map(e -> new IdWrapper(e.getId())).toArray();
            } else if(jsonElement.isJsonObject()) {
                E entity = jsonConverter.parse(request.body());
                beforeCreation(entity, request, response);
                create(entity);
                response.header("Location", path + "/" + entity.getId());

                response.status(201); // Created
                return entity;
            } else {
                throw new IllegalArgumentException("Expecting a JSON array or object");
            }
        };

        routeHelper.publishRouteSet(RouteHelper.HttpVerb.POST, path, postRoute);


        // GET

        Route getByIdRoute = (request, response) -> {
            E entity = read(idFromUrl(request));
            if (entity == null) halt(404);
            return entity;
        };

        routeHelper.publishRouteSet(RouteHelper.HttpVerb.GET, path + "/:id", getByIdRoute);


        Route listRoute = (Request request, Response response) -> {
            // Figure out how many entities to return
            long page = request.queryMap("page").hasValue() ? request.queryMap("page").longValue() : 1;
            long perPage = Long.min(maxListCount, request.queryMap("perPage").hasValue() ? request.queryMap("perPage").longValue() : defaultListCount);
            ResultsPage<E> resultsPage = list(request, page, perPage);

            // Send the pagination headers
            response.header("X-Total-Count", Long.toString(resultsPage.total));
            response.header("X-Page-Count", Long.toString(1 + resultsPage.total / perPage));
            response.header("X-Per-Page-Count", Long.toString(perPage));
            response.header("X-Page-Number", Long.toString(page));
            response.header("Link", makeLinkHeaders(request, resultsPage));

            // Return the actual results (to be converted to JSON)
            return resultsPage;
        };

        routeHelper.publishRouteSet(RouteHelper.HttpVerb.GET, path, listRoute);


        // PUT

        Route putRoute = (request, response) -> {
            E entity = jsonConverter.parse(request.body());
            UUID id = idFromUrl(request);
            if (entity.getId() != null && !entity.getId().equals(id)) {
                throw new IllegalArgumentException("IDs in URL and body do not match");
            } else {
                entity.setId(id);
            }
            return update(entity);
        };

        routeHelper.publishRouteSet(RouteHelper.HttpVerb.PUT, path + "/:id", putRoute);


        // DELETE

        Route deleteRoute = (request, response) -> dao.delete(idFromUrl(request));

        routeHelper.publishRouteSet(RouteHelper.HttpVerb.DELETE, path + "/:id", deleteRoute);


        // OPTIONS
        // Always return empty response with CORS headers
        // TODO: options shouldn't return a particular content type
        Route optionsRoute = (request, response) -> { return "{}"; };

        routeHelper.publishRouteSet(RouteHelper.HttpVerb.OPTIONS, path, optionsRoute);
        routeHelper.publishRouteSet(RouteHelper.HttpVerb.OPTIONS, path + "/:id", optionsRoute);
    }

    protected E create(E entity) {
        return dao.create(entity);
    }

    protected E read(UUID id) {
        return dao.read(id);
    }

    protected E update(E entity) {
        return dao.update(entity);
    }

    protected ResultsPage<E> list(Request request, long page, long perPage) { return dao.list(page, perPage); }

    protected void publishSpecific() {
    }

    protected void beforeCreation(E entity, Request request, Response response) {
    }

    String makeLinkHeaders(Request request, ResultsPage<E> resultsPage) {
        final String prefix = Server.hostName + path + "/";

        // Get non-page based parameters
        String baseParameters = request.queryParams().stream()
                .filter(paramName -> !paramName.equals("page") && !paramName.equals("perPage"))
                .map(paramName -> paramName + "=" + request.queryParams(paramName))
                .collect(Collectors.joining("&"));
        String baseUrl = prefix + "?" + baseParameters;

        // Page numbers are 1-based
        ArrayList<String> linkHeaderArray = new ArrayList<String>();
        if (resultsPage.page > 1) {
            // Add first header
            linkHeaderArray.add(baseUrl + "&page=0&perPage=" + resultsPage.perPage + "; rel=first");
            // Add previous header
            linkHeaderArray.add(baseUrl + "&page=" + (resultsPage.page - 1) + "&perPage=" + resultsPage.perPage + "; rel=prev");
        }

        long lastPage = 1 + resultsPage.total / resultsPage.perPage;
        if (resultsPage.page < lastPage) {
            // Add next header
            linkHeaderArray.add(baseUrl + "&page=" + (resultsPage.page + 1) + "&perPage=" + resultsPage.perPage + "; rel=next");
            // Add last header
            linkHeaderArray.add(baseUrl + "&page=" + lastPage + "&perPage=" + resultsPage.perPage + "; rel=last");
        }

        return linkHeaderArray.stream().collect(Collectors.joining(", "));
    }
}
