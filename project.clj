(defproject minesweeper "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.1.8"]
                 [ring/ring-jetty-adapter "1.3.1"]
                 [ring-basic-authentication "1.0.5"]
                 [markdown-clj "0.9.47"]
                 [de.ubercode.clostache/clostache "1.4.0"]
                 [org.clojure/java.jdbc "0.3.5"]
                 [org.postgresql/postgresql "9.3-1102-jdbc41"]]
  :plugins [[lein-ring "0.8.11"]]
  :main ^:skip-aot minesweeper.web
  :min-lein-version "2.0.0"
  :uberjar-name "minesweeper.jar"
  :ring {:handler minesweeper.web/app}
  :profiles
  {:uberjar {:aot :all}
   :dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}})
