(ns minesweeper.web
  (:require [clojure.java.io :refer [resource]]
            [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.util.response :refer [redirect redirect-after-post]]
            [markdown.core :refer [md-to-html-string]]
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

(defn open [request]
  (let [params (:params request)
        x (parse-int (get params :x))
        y (parse-int (get params :y))
        game-id (get params :id)
        my-guess (board/new-posn x y)]
    (if (board/valid-posn? my-guess)
      (let [result (game/open! game-id my-guess)]
        (if (keyword? result)
          (name result)
          (str result)))
      {:status 400
       :body "Bad Request"})))

(defn new-game
  [request]
  (if-let [name (get-in request [:params :name])]
    (game/new! name)
    "Name required"))

(defn info [_]
  (str board/width "," board/height "," board/mine-count))

(defn index [_]
  (let [template (slurp (resource "index.html"))
        content (md-to-html-string (slurp (resource "README.md")))]
    (clojure.string/replace template "{{text}}" content)))

(defroutes app-routes
  (GET "/" [] index)
  (GET "/info" [] info)
  (ANY "/new" [] new-game)
  (ANY "/open" [] open)
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
