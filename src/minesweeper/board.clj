(ns minesweeper.board)

(def width 5)
(def height 5)
(def mine-count 2)

(defn new-posn [x y]
  {:x x :y y})

(defn random-posn
  "Return a random position on the board"
  []
  (new-posn (rand-int width)
            (rand-int height)))

(defn valid-posn?
  "Is the given position within the board?"
  [p]
  (let [x (:x p)
        y (:y p)]
    (and (>= x 0)
         (>= y 0)
         (< x width)
         (< y height))))

(defn neighbors?
  "Are the given points neighbors?"
  [p1 p2]
  (and (>= 1 (Math/abs (- (:x p1) (:x p2))))
       (>= 1 (Math/abs (- (:y p1) (:y p2))))))

(defn in?
  "Does the given sequence contain this element?"
  [seq element]
  (some #(= element %) seq))

(defn count-neighbors
  "How many members of this list neighbor this target?"
  [lst target]
  (count (filter #(neighbors? target %) lst)))

(defn random
  "A list of randomly chosen, non-overlapping positions"
  []
  (loop [remaining mine-count
         mines (list)]
    (if (= 0 remaining)
      mines
      (let [random (random-posn)]
        (if (in? mines random)
          (recur remaining mines)
          (recur (dec remaining) (conj mines random)))))))

(defn blank
  "Return an empty board"
  []
  [])

(defn serialize
  "Encode this board as a string"
  [board]
  (str board))

(defn deserialize
  "Decode the string representation of a board, returning a new board"
  [board-string]
  (read-string board-string))
