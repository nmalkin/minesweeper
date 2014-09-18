(ns minesweeper.results
  (:require [clojure.java.jdbc :as jdbc]
            [minesweeper.board :as board]
            [minesweeper.database :as database]))

(def score-query "
SELECT
    name
  , won
  , lost
  , won + lost total
  , CASE WHEN (won+lost) = 0 THEN -1 ELSE won/(won+lost) END percentage
FROM
( -- aggregate the wins and losses
  SELECT
      name
    , SUM(CASE WHEN status='win' THEN count ELSE 0 END) won
    , SUM(CASE WHEN status='lost' THEN count ELSE 0 END) lost
  FROM
  ( -- count how many games, in each state, we have
    SELECT
        games.name
      , games.status
      , count(*)
    FROM games
    JOIN
    ( -- find, and only use, the latest version
      SELECT name, MAX(version) max_version
      FROM games
      GROUP BY NAME
    ) latest
    ON games.name = latest.name AND games.version = latest.max_version
    GROUP BY games.name, games.status
  ) counted
  GROUP BY name
) aggregated
WHERE won + lost > ?
ORDER BY percentage DESC;
")

(defn all
  "Get results for players who have played more than the given number of games"
  [games]
  (jdbc/query database/spec [score-query games]))
