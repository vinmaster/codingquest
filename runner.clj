(ns runner)

(import '[java.time.format DateTimeFormatter]
        '[java.time LocalDateTime])

(require '[babashka.curl :as curl])
(require '[clojure.java.io :as io])
(require '[babashka.cli :as cli])
(require '[babashka.fs :as fs])
;; (require '[babashka.process :refer [shell sh process check]])
(require '[babashka.process :as process])
(require '[clojure.string :as str])
;; (require '[cheshire.core :as json])

(def today (LocalDateTime/now))
;; (def formatter (DateTimeFormatter/ofPattern "yyyy-MM-dd HH:mm:ss"))
(def this-year (.format today (DateTimeFormatter/ofPattern "yyyy")))
(def this-date (.format today (DateTimeFormatter/ofPattern "MM-dd")))
(def url-template "https://codingquest.io/api/puzzledata?puzzle=%s")
(def path-template "./%s/%s")

(defn parse-args-solution [args]
  (cli/parse-opts args {:alias {:y :year :d :date}
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
  ;;  (prn "Arguments given:" args)
   (let [{year :year date :date} (parse-args-solution args)
         path (format path-template year date)]
    ;;  (process/sh "bb ./2023/03-07/main.clj")
     (-> (process/sh "bb" (str path "/main.clj"))
         (#(do (print (:out %))
               (when (not= (:exit %) 0) (print (:err %)))))))))

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

;; (download-runner '("-d" "03-09" "-p" "20"))
