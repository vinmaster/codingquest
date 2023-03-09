(require '[clojure.string :as str]
         #_'[babashka.process :refer [shell sh process check]])

(def input-raw (slurp "./2023/03-08/input.txt"))
(def input-raw "
2 4 5 8 1 3 9 6 7
2 4 5 8 1 3 9 6 7
")

(defn parse-int [s] (Integer/parseInt s))
(defn split-whitespace [s] (str/split s #" "))

(def input
  (->> input-raw
       str/trim
       str/split-lines
       (map (comp (partial map parse-int) split-whitespace))))

(->> input
    ;;  (apply merge-with +)
    ;;  (into []) ;; list of pairs
    ;;  (map #(update % 1 (fn [n] (mod n 100))))
    ;;  (map second)
    ;;  (apply *)
     prn)
