(ns minesweeper.database
  (:require [clojure.java.jdbc :as jdbc]))

(def spec (or (System/getenv "DATABASE_URL")
              "postgresql://localhost:5432/minesweeper"))

(defn migrated?
  "Has the database been set up?"
  []
  (-> (jdbc/query spec
                  ["SELECT COUNT(1)
                   FROM information_schema.tables
                   WHERE table_name = ?"
                   "games"])
      first :count pos?))

(defn migrate
  "Set up the database, if necessary"
  []
  (when (not (migrated?))
    (println "Setting up database")
    (jdbc/db-do-commands
      spec
      (jdbc/create-table-ddl :games
                             [:id :serial "PRIMARY KEY"]
                             [:key "varchar(64)" "NOT NULL"]
                             [:name "varchar(128)" "NOT NULL"]
                             [:version "integer" "NOT NULL"]
                             [:status "varchar(16)" "NOT NULL"]
                             [:mines :text "NOT NULL"]
                             [:guesses :text "NOT NULL"]
                             [:created_at :timestamp
                              "NOT NULL" "DEFAULT CURRENT_TIMESTAMP"]))))
