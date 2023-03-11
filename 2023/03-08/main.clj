(require '[clojure.string :as str])

(def input-raw (slurp (str/replace *file* #"main.clj" "input.txt")))
#_(def input-raw "
2 4 5 8 1 3 9 6 7
2 4 5 8 1 3 9 6 7
")
(def winning-bits
  [448 56 7 ;; horizontals 
   292 146 73 ;; verticals
   273 84 ;; diagonals
   ])

(defn s->int [s] (Integer/parseInt s))
(defn binary-list->int [coll] (Integer/parseInt (apply str coll) 2))
(defn split-whitespace [s] (str/split s #" "))
(defn in? "true if coll contains x" [coll x] (some #(= x %) coll))
(defn print-board [board] (->> (partition 3 board) (map str/join) (str/join "\n") print))
(defn get-moves-with-player [moves]
  (->> (interleave (repeat "X") (repeat "O"))
       (interleave moves)
       (partition 2)))
(defn make-move [board move player] (update board (dec move) (constantly player)))
(defn get-game-result [board moves-with-player]
  (loop [board board
         moves (rest moves-with-player)
         cur (first moves-with-player)]
    (if (nil? cur)
      :tie
      (let [new-board (apply make-move board cur)
            x-bits (binary-list->int (map #(if (= % "X") 1 0) new-board))
            o-bits (binary-list->int (map #(if (= % "O") 1 0) new-board))]
        (cond
          (some #(= % (bit-and % x-bits)) winning-bits) :x
          (some #(= % (bit-and % o-bits)) winning-bits) :o
          :else (recur new-board
                       (rest moves)
                       (first moves)))))))

(def input
  (->> input-raw
       str/trim
       str/split-lines
       (map (comp (partial map s->int) split-whitespace))))
(def board (vec (repeat 9 " ")))

(->> input
     (map #(assoc {} (get-game-result board (get-moves-with-player %)) 1))
     (apply merge-with +)
     vals
     (apply *)
     prn)

#_(->> input first
       get-moves-with-player
    ;;  (get-game-result board)
    ;;  prn
       (reduce #(apply make-move %1 %2) board)
       print-board)
