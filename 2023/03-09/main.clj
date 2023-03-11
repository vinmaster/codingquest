(require '[clojure.string :as str])

(def input-raw (slurp (str/replace *file* #"main.clj" "input.txt")))
#_(def input-raw "
55550000005800f754686973206973206120746573742e205468697320697320
55550000005801f06120746573742e205468616e6b796f752e20202020202020
")

(defn parse-hex [s] (Integer/parseInt s 16))

(def structure [[:header 4] [:sender 8] [:sequence 2] [:checksum 2] [:message 48]])

(defn create-chunks [lengths]
  (->> lengths
       (reductions (fn [acc n] [(last acc) (+ n (last acc))]) [0])
       rest))

(defn chunk-str [s chunks] (map #(subs s (nth % 0) (nth % 1)) chunks))

(defn zip-vecs [& colls] (apply map vector colls))

(defn vec->map [v] (into (sorted-map) v))

(defn partition-str [n s] (map (partial apply str) (partition n s)))

(def input
  (->> input-raw
       str/trim
       str/split-lines
       (map (fn [s]
              (apply hash-map
                     (interleave
                      (map first structure)
                      (->> (map second structure)
                           create-chunks
                           (chunk-str s))))
              #_(vec->map
                 (zip-vecs
                  (map first structure)
                  (->> (map second structure)
                       create-chunks
                       (chunk-str s))))))))

(defn process [packets]
  (let [header-pred #(= (:header %) "5555")
        sender-pred #(->> packets (map :sender) frequencies (sort-by vals) ffirst (= (:sender %)))
        checksum-pred #(= (parse-hex (:checksum %))
                          (->> (partition-str 2 (:message %))
                               (map parse-hex)
                               (apply +)
                               ((fn [sum] (mod sum 256)))))]
    (->> packets
         (filter (every-pred header-pred sender-pred checksum-pred))
         (sort-by (comp parse-hex :sequence))
         (mapcat :message)
         (partition-str 2)
         (map (comp char parse-hex))
         (apply str)
         str/trimr)))

(prn (process input))
