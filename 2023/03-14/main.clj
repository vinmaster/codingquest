(require '[clojure.string :as str])

(def input-raw (slurp (str/replace *file* #"main.clj" "input.txt")))
#_(def input-raw "
fruit
3,3 2,5 7,7 6,0
moves
DDDRRRDDLLLDRRRRRRRDD
")

(def input
  (->> input-raw
       str/trim
       str/split-lines
       ((fn [[_ fs _ ms]]
          {:fruits (map (fn [coord] (map parse-long (str/split coord #","))) (str/split fs #" "))
           :moves (str/split ms #"")}))))
(defn vec2d
  "Return an x by y vector with all entries equal to val."
  [x y val]
  (vec (repeat y (vec (repeat x val)))))
(def start-time 3600)
(def end-time (+ start-time 60))
;; example is 8x8. real input is 100x100
(def bound (if (> (count (:moves input)) 30) 20 8))
(def grid (vec2d bound bound "."))
(defn print-state [state]
  (->> state
       ((fn [state] (assoc state :grid grid)))
       ((fn [state]
          (let [[fruit-x fruit-y] (nth (:fruits input) (:fruit-index state))]
            (-> state
                (update-in [:grid fruit-y fruit-x] (constantly "F"))
                ((fn [state]
                   (reduce (fn [state [snake-x snake-y]] (update-in state [:grid snake-y snake-x] (constantly "S")))
                           state
                           (:snake state))))
                :grid))))
       (map str/join)
       (str/join "\n")
       print)
  (println)
  (println "Score:" (:score state)))
(defn tap->> [f x] (f x) x)
(defn butlastv [coll] (vec (butlast coll)))
(def state {:score 0 :snake [[0 0]] :fruit-index 0 :move-index 0 :game-over false})
(defn spawn-fruit [state] (update state :fruit-index inc))
(defn get-new-coord [[x y] dir]
  (case dir
    "L" [(- x 1) y]
    "R" [(+ x 1) y]
    "U" [x (- y 1)]
    "D" [x (+ y 1)]))
(defn in-bound? [[x y]] (and (< -1 x bound) (< -1 y bound)))
(defn in? "true if coll contains x" [coll x] (some #(= x %) coll))
(defn move-snake [state]
  (if (>= (:move-index state) (count (:moves input)))
    (update state :game-over (constantly true))
    (let [snake (:snake state)
          head (first snake)
          new-coord (get-new-coord head (nth (:moves input) (:move-index state)))
          has-fruit? (= new-coord (nth (:fruits input) (:fruit-index state)))
          eat-self? (in? snake new-coord)]
      (if (or (not (in-bound? new-coord)) eat-self?)
        (update state :game-over (constantly true))
        (if has-fruit?
          (-> state
              (update :snake (fn [snake] (into [new-coord] snake)))
              (update :move-index inc)
              (update :score #(+ % 101))
              (spawn-fruit))
          (-> state
              (update :snake (fn [snake] (into [new-coord] (butlastv snake))))
              (update :move-index inc)
              (update :score inc)))))))

#_(print-state
   (loop [state state]
     (let [new-state (move-snake state)]
       (if (:game-over new-state)
         new-state
         (recur new-state)))))

(->> state
     (iterate move-snake)
     (drop-while (complement :game-over))
     first
     print-state)
