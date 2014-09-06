(ns minesweeper.game
  (:require [minesweeper.board :as board]))

(def games (atom {}))

(defn new-game!
  "Initiates a new game. Returns that game's ID."
  []
  (let [game-id (str (java.util.UUID/randomUUID))]
    (swap! games assoc game-id {:status :active
                                :guesses (list)
                                :my-board (board/random)})
    game-id))

(defn open!
  [game-id guess]
  (if-let [game (get @games game-id)]
    (let [{:keys [status guesses my-board]} game
          ; Save this guess, but only if we haven't seen it before.
          guesses (if (board/in? guesses guess)
                    guesses
                    (conj guesses guess))
          set-status! #(swap! games assoc game-id {:status %
                                                   :guesses guesses
                                                   :my-board my-board})]
      (cond
        ; The game has already ended. Return its result.
        (not (= status :active))
        status

        ; Boom! The player lost.
        (board/in? my-board guess)
        (do (set-status! :lost)
            :lost)

        ; Player revealed all mine-free squares. They win!
        (>= (count guesses) (- (* board/width board/height) board/mine-count))
        (do (set-status! :win)
            :win)

        ; Good job, player! No bombs here.
        :else
        (do (set-status! :active)
            (board/count-neighbors my-board guess))))))

