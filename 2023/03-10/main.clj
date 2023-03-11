(require '[clojure.string :as str])

(def input-raw (slurp "./2023/03-10/input.txt"))
#_(def input-raw "
1 1 6 4
2 2 5 2
1 5 1 3
2 7 5 1
")

(defn parse-int [s] (Integer/parseInt s))
(defn split-whitespace [s] (str/split s #" "))

(defn vec2d
  "Return an x by y vector with all entries equal to val."
  [x y val]
  (vec (repeat y (vec (repeat x val)))))

(defn print-pixels [grid] (->> grid (map str/join) (str/join "\n") print))

(defn flip-pixel [grid x y]
  (let [pixel (get-in grid [y x])
        new-pixel (if (= pixel " ") "#" " ")]
    (update grid y #(update % x (constantly new-pixel)))))

(defn run-command [grid command]
  (let [[x y w h] command
        coords (for [x2 (range x (+ x w))
                     y2 (range y (+ y h))]
                 [x2 y2])]
    (reduce #(apply flip-pixel %1 %2) grid coords)))

(def grid (vec2d 50 10 " "))
(def input
  (->> input-raw
       str/trim
       str/split-lines
       (map (comp (partial map parse-int) split-whitespace))))

(->> input
     (reduce #(run-command %1 %2) grid)
     print-pixels)
