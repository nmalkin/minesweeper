(ns minesweeper.web
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.util.response :refer [redirect redirect-after-post]]
            [minesweeper.board :as board]
            [minesweeper.game :as game])
  (:gen-class))

(defn parse-int
  "Try to parse given input as int, return -1 on failure"
  [s]
  (try
    (Integer/parseInt s)
    (catch NumberFormatException e -1)))

(defn guess [request]
  (let [params (:query-params request)
        x (parse-int (get params "x"))
        y (parse-int (get params "y"))
        game-id (get params "id")
        my-guess (board/new-posn x y)]
    (if (board/valid-posn? my-guess)
      (let [result (game/open! game-id my-guess)]
        (if (keyword? result)
          (name result)
          (str result)))
      {:status 400
       :body "Bad Request"})))

(defn info [_]
  (str board/width "," board/height "," board/mine-count))

(defroutes app-routes
  (GET "/" [] "Minesweeper")
  (GET "/info" [] info)
  (GET "/new" [] (game/new-game!))
  (GET "/guess" [] guess)
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      handler/site
      wrap-params))

(defn -main []
  (let [port (Integer/parseInt (get (System/getenv) "PORT" "3000"))]
    (run-jetty app {:port port})))
