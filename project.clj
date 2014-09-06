(defproject minesweeper "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.1.8"]
                 [ring/ring-jetty-adapter "1.3.1"]
                 [markdown-clj "0.9.47"]]
  :plugins [[lein-ring "0.8.11"]]
  :main ^:skip-aot minesweeper.web
  :min-lein-version "2.0.0"
  :uberjar-name "minesweeper.jar"
  :ring {:handler minesweeper.web/app}
  :profiles
  {:uberjar {:aot :all}
   :dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}})
