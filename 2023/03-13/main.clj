(require '[clojure.string :as str])
(require '[clojure.math :refer [round]])

(def input-raw (slurp (str/replace *file* #"main.clj" "input.txt")))
#_(def input-raw "
-3600 -3600 1 1
-3601 -3600 1 1
3608 -3600 -1 1
3607 -3600 -1 1
3608 -3600 -1 1
-359 0 0.1 0
-359 1 0.1 0
-358 2 0.1 0
0 1830 0 -0.5
1 1830 0 -0.5
7 186 0 -0.05
185 7 -0.05 0
185 6 -0.05 0
6 -3600 0 1
2 4 0 0
")

(def input
  (->> input-raw
       str/trim
       str/split-lines
       (map #(map parse-double (str/split % #" ")))))
(defn vec2d
  "Return an x by y vector with all entries equal to val."
  [x y val]
  (vec (repeat y (vec (repeat x val)))))
(def start-time 3600)
(def end-time (+ start-time 60))
;; example is 8x8. real input is 100x100
(def bound (if (> (count input) 20) 100 8))
(defn in-bound? [n] (< -1 n bound))
(def grid (vec2d bound bound "."))
(defn print-grid [grid] (->> grid (map str/join) (str/join "\n") print))
;; time is in seconds
(defn position-at-time [x y dx dy t]
  [(+ x (* dx t)) (+ y (* dy t))])
(defn mark-grid [grid coord]
  (let [rounded-coord (map round coord)]
    (if (every? in-bound? rounded-coord)
      (update-in grid rounded-coord (constantly "X"))
      grid)))

(->> input
     (mapcat (fn [[x y dx dy]]
               (map #(position-at-time x y dx dy %)
                    (range start-time end-time))))
     (reduce mark-grid grid)
    ;;  print-grid
     (map-indexed (fn [x row] (map-indexed (fn [y val] [[x y] val]) row)))
     (apply concat)
     (filter #(= (second %) "."))
     ffirst
     (str/join ":")
     prn)
