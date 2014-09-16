(ns minesweeper.game
  (:require [clojure.java.jdbc :as jdbc]
            [minesweeper.board :as board]
            [minesweeper.database :as database]))

(defn new!
  "Initiates a new game. Returns that game's ID."
  [name version]
  (let [game-id (str (java.util.UUID/randomUUID))]
    (jdbc/insert! database/spec
                  :games
                  {:key game-id
                   :name name
                   :version version
                   :status "active"
                   :mines (board/serialize (board/random))
                   :guesses (board/serialize (board/blank))})
    game-id))

(defn open!
  [game-id guess]
  (if-let [game (first (jdbc/query database/spec
                                   ["SELECT * FROM games WHERE key = ?" game-id]))]
    (let [status (-> game :status keyword)
          mines (-> game :mines board/deserialize)
          guesses (-> game :guesses board/deserialize)
          ; Save this guess, but only if we haven't seen it before.
          guesses (if (board/in? guesses guess)
                    guesses
                    (conj guesses guess))
          set-status! #(jdbc/update! database/spec :games
                                     {:status (name %)
                                      :guesses (board/serialize guesses)}
                                     ["key = ?" game-id])]

      (cond
        ; The game has already ended. Return its result.
        (not (= status :active))
        status

        ; Boom! The player lost.
        (board/in? mines guess)
        (do (set-status! :lost)
            :lost)

        ; Player revealed all mine-free squares. They win!
        (>= (count guesses) (- (* board/width board/height) board/mine-count))
        (do (set-status! :win)
            :win)

        ; Good job, player! No bombs here.
        :else
        (do (set-status! :active)
            (board/count-neighbors mines guess))))))

