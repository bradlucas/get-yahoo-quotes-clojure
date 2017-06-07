(ns get-yahoo-quotes.core
  (:require [clj-http.client :as client]
            [clojure.java.io :as io]
            [clojure.string :as str])
  (:import [org.apache.commons.lang3 StringEscapeUtils])
  (:gen-class))


(defn split-crumbstore0 [val]
  ;; # ,"CrumbStore": {"crumb":"FWP\u002F5EFll3U"
  ;; get the last field delineated by :
  ;; strip the quotes
  ;; fixup the unicode-escaped values
  (StringEscapeUtils/unescapeJava (str/replace (last (str/split val #":")) "\"" "")))

;; rebuild split-crumbstore to use a threading macro
;; https://clojuredocs.org/clojure.core/-%3E

(defn split-crumbstore [v]
  ;; # ,"CrumbStore": {"crumb":"FWP\u002F5EFll3U"
  ;; get the last field delineated by :
  ;; strip the quotes
  ;; fixup the unicode-escaped values
  (-> 
   (str/split v #":")
   last
   (str/replace "\"" "") 
   (StringEscapeUtils/unescapeJava)))


(defn find-crumbstore [lines]
  (first (filter #(str/includes? % "CrumbStore") lines)))


(defn get-page-data [symbol]
  (let [url (format "https://finance.yahoo.com/quote/%s/?p=%s" symbol symbol)
        page (client/get url)]
    (str/split (:body page) #"}")))


(defn get-crumb [symbol]
  (let [crumb (split-crumbstore (find-crumbstore (get-page-data symbol)))]
    ;; (println crumb)
    crumb))

(defn download-data [symbol crumb]
  ;; we should use a cookiestore so our requests pass the received cookie from the first page along with subsequent requests
  ;; @see https://github.com/dakrone/clj-http#cookies
  (binding [clj-http.core/*cookie-store* (clj-http.cookies/cookie-store)]

    ;; build the download url
    (let [filename (format "%s.csv" symbol)
          start_date 0
          end_date (System/currentTimeMillis)
          url (format "https://query1.finance.yahoo.com/v7/finance/download/%s?period1=%s&period2=%s&interval=1d&events=history&crumb=%s" symbol, start_date, end_date, (get-crumb symbol))]
      (println "--------------------------------------------------")
      (println (format "Downloading %s to %s" symbol filename))

      ;; @see https://stackoverflow.com/a/32745253
      (-> 
       (client/get url {:as :byte-array})
       (:body)
       (io/input-stream)
       (io/copy (io/file filename)))
      (println "--------------------------------------------------"))))


(defn run [symbols]
  (doseq [symbol symbols] 
    (download-data symbol (get-crumb symbol))))

(defn -main [& args]
  (if args
    (run args)
    (println "\nUsage: java -jar get-yahoo-quotes.jar SYMBOL\n\n")))
