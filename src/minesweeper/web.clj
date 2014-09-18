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
            [minesweeper.game :as game])
  (:gen-class))

(defn parse-int
  "Try to parse given input as int, return -1 on failure"
  [s]
  (try
    (Integer/parseInt s)
    (catch NumberFormatException e -1)))

(defn format-response
  ""
  [handler]
  (fn [request]
    (let [response (handler request)]
      (cond
        (nil? response) {:status 400 :body "Bad Request"}
        (keyword? response) (name response)
        :else (str response)))))

(defn open
  "Handle a request to open a cell"
  [request]
  (let [params (:params request)
        game-id (:id params)
        x (-> params :x parse-int)
        y (-> params :y parse-int)
        my-guess (board/new-posn x y)]
    (if (board/valid-posn? my-guess)
      (game/open! game-id my-guess))))

(defn new-game
  "Handle a request for a new game"
  [request]
  (if-let [name (get-in request [:params :name])]
    (let [version (-> request :params :version parse-int)]
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
