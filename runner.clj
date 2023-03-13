(ns runner)

(import '[java.time.format DateTimeFormatter]
        '[java.time LocalDateTime])

(require '[babashka.curl :as curl])
(require '[clojure.java.io :as io])
(require '[babashka.cli :as cli])
(require '[babashka.fs :as fs])
(require '[babashka.process :as process])
(require '[clojure.string :as str])
(require '[babashka.pods :as pods])
;; (require '[cheshire.core :as json])
(pods/load-pod 'retrogradeorbit/bootleg "0.1.9")

(require '[pod.retrogradeorbit.bootleg.utils :refer [convert-to]]
         '[pod.retrogradeorbit.hickory.select :as s])

(def today (LocalDateTime/now))
;; (def formatter (DateTimeFormatter/ofPattern "yyyy-MM-dd HH:mm:ss"))
(def this-year (.format today (DateTimeFormatter/ofPattern "yyyy")))
(def this-date (.format today (DateTimeFormatter/ofPattern "MM-dd")))
(def url-template "https://codingquest.io/api/puzzledata?puzzle=%s")
(def prompt-template "https://codingquest.io/problem/%s")
(def path-template "./%s/%s")

(defn parse-args-solution [args]
  (cli/parse-opts args {:alias {:y :year :d :date}
                        :exec-args {:year this-year
                                    :date this-date}}))

(defn parse-args-prompt [args]
  (cli/parse-opts args {:alias {:p :puzzle :y :year :d :date}
                        :require [:puzzle]
                        :exec-args {:year this-year
                                    :date this-date}}))

(defn parse-args-download [args]
  (cli/parse-opts args {:alias {:p :puzzle :y :year :d :date}
                        :require [:puzzle]
                        :exec-args {:year this-year
                                    :date this-date}}))

(defn download
  ([url filename] (download url filename "./"))
  ([url filename path]
   (when-not (fs/exists? path) (fs/create-dirs path))
   (io/copy
    (:body (curl/get url {:as :bytes}))
    (io/file (str path "/" filename)))))

(defn solution-runner
  ([] (solution-runner *command-line-args*))
  ([args]
   (when (empty? args) (throw (Exception. "Error: No args given")))
   (let [{year :year date :date} (parse-args-solution args)
         path (format path-template year date)]
     (-> (process/sh (str "bb " path "/main.clj"))
         (#(do (print (:out %))
               (when (not= (:exit %) 0) (print (:err %)))))))))

(defn prompt-runner
  ([] (prompt-runner *command-line-args*))
  ([args]
   (let [{puzzle :puzzle year :year date :date} (parse-args-download args)
         url (format prompt-template puzzle)
         path (format path-template year date)]
     (download url "README.md" path)
     (println "Prompt to:" (str path "/README.md")))
   (:body (curl/get args))))

(defn download-runner
  ([] (download-runner *command-line-args*))
  ([args]
   (when (empty? args) (throw (Exception. "Error: No args given")))
  ;;  (prn "Arguments given:" args)
   (let [{puzzle :puzzle year :year date :date} (parse-args-download args)
         url (format url-template puzzle)
         path (format path-template year date)]
     (download url "input.txt" path)
     (println "Downloaded to:" (str path "/input.txt")))))

#_(-> (prompt-runner "https://codingquest.io/problem/22")
      str/trim
      (convert-to :hickory)
      (#(s/select (s/class :Markdown) %))
      (convert-to :html)
      prn)
;; (prompt-runner '("-d" "03-06" "-p" "18"))
;; (download-runner '("-d" "03-09" "-p" "20"))
