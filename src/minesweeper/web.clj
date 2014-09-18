(ns minesweeper.web
  (:require [clojure.java.io :refer [resource]]
            [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.params :refer [wrap-params]]
            [markdown.core :refer [md-to-html-string]]
            [clostache.parser :as clostache]
            [minesweeper.board :as board]
            [minesweeper.database :as database]
            [minesweeper.game :as game]
            [minesweeper.results :as results])
  (:gen-class))

(defn parse-int
  "Try to parse given input as int, return -1 on failure"
  [s]
  (try
    (Integer/parseInt s)
    (catch NumberFormatException e -1)))

(defn format-response
  "Wraps handler functions, ensuring that the values they return can be
  rendered and displayed.
  Nil responses are assumed to be the result of bad requests, resulting in
  an error message and a 400 status code."
  [handler]
  (fn [request]
    (let [response (handler request)]
      (cond
        (nil? response) {:status 400 :body "Bad Request"}
        (keyword? response) (name response)
        :else (str response)))))

(defn int-param
  "Retrieves an integer parameter from given request"
  [request param]
  (-> request :params param parse-int))

(defn leaderboard
  "Display the results"
  [request]
  (let [over (int-param request :over)
        raw-results (results/all over)]
    (clostache/render-resource "results.html" {:players raw-results})))

(defn open
  "Handle a request to open a cell"
  [request]
  (let [game-id (-> request :params :id)
        x (int-param request :x)
        y (int-param request :y)
        my-guess (board/new-posn x y)]
    (if (board/valid-posn? my-guess)
      (game/open! game-id my-guess))))

(defn new-game
  "Handle a request for a new game"
  [request]
  (if-let [name (get-in request [:params :name])]
    (let [version (int-param request :version)]
      (game/new! name version))))

(defn info
  "Handle a request for board information"
  [_]
  (str board/width "," board/height "," board/mine-count))

(defn index
  "Handle a request for the homepage by returning instructions"
  [_]
  (let [content (md-to-html-string (slurp (resource "README.md")))]
    (clostache/render-resource "index.html" {:text content})))

(defroutes app-routes
  (GET "/" [] index)
  (GET "/info" [] (format-response info))
  (ANY "/new"  [] (format-response new-game))
  (ANY "/open" [] (format-response open))
  (GET "/results" [] (format-response leaderboard))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      handler/site
      wrap-params))

(defn -main []
  (database/migrate)
  (let [port (Integer/parseInt (get (System/getenv) "PORT" "3000"))]
    (run-jetty app {:port port})))
