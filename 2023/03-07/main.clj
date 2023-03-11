(require '[clojure.string :as str])

(def input-raw (slurp (str/replace *file* #"main.clj" "input.txt")))
#_(def input-raw "
30635
34132
46818
15895
37924
52364
31114
4040
6676
53800
")

(def input
  (->> input-raw
       str/trim
       str/split-lines
       (map read-string)))

(defn pad [n s] (str (str/join "" (repeat (- n (count s)) "0")) s))
(defn to-binary [n] (pad 16 (Integer/toString n 2)))
(defn to-int [s] (Integer/parseInt s 2))
(defn turn-off-bit [n s] (-> s (str/split #"") (update (- 16 n) (constantly "0")) (#(str/join "" %))))

(defn avg [coll]
  (float (/ (apply + coll) (count coll))))

(->> input
     (map to-binary)
     (filter (fn [s] (-> s frequencies (get \1) even?)))
     (map #(turn-off-bit 16 %))
     (map to-int)
     avg
     (Math/round)
     prn)
