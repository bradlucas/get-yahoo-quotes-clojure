(ns get-yahoo-quotes.commodity
  (:require [clj-http.client :as client]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [get-yahoo-quotes.core :as core])
  (:import [org.apache.commons.lang3 StringEscapeUtils]))

;; ----------------------------------------------------------------------------------------------------
;; ----------------------------------------------------------------------------------------------------

(def public-url "https://finance.yahoo.com/quote/")

(comment
"https://finance.yahoo.com/quote/CC=F"
 "https://finance.yahoo.com/quote/KCU20.NYB"
 "https://finance.yahoo.com/quote/C=F"
 "https://finance.yahoo.com/quote/RR=F"
 "https://finance.yahoo.com/quote/S=F"
 "https://finance.yahoo.com/quote/SB=F"
 "https://finance.yahoo.com/quote/W=F")



(def commodities {:cocoa "CC=F"          
                  :coffee "KCU20.NYB"
                  :corn "C=F"
                  :rice "RR=F"
                  :soybean "S=F"
                  :sugar "SB=F"
                  :wheat "W=F"})

(defn first-not-null [col]
  ;; return the first value that is not "null"
  (first (filter #(not (= "null" %)) col)))

(defn get-last-adj-close [row]
  (nth row  4))

(defn split-out-adj-close [rows]
  (map (fn [v] (get-last-adj-close v)) rows))

(defn get-data0 [symbol & days]
  (let [now (int (/ (.getTime (java.util.Date.)) 1000))  ;; @see https://stackoverflow.com/questions/17432032/how-do-i-get-a-unix-timestamp-in-clojure
        week 604800
        day 86400
        crumb (core/get-crumb symbol)
        start_date (- now (* day 7))
        end_date now
        url (format "https://query1.finance.yahoo.com/v7/finance/download/%s?period1=%s&period2=%s&interval=1d&events=history&crumb=%s" symbol, start_date, end_date, crumb)]
    (->
     (client/get url)
     (:body)
     (clojure.string/split #"\n")
     ;; rest                                        ;; remove header
     ;; reverse                                     ;; want to find the most recent valid adj-close (5th position)
     ;; split-out-adj-close
     ;; first-not-null
     )))

(defn get-data [symbol & days]
  (let [now (int (/ (.getTime (java.util.Date.)) 1000))  ;; @see https://stackoverflow.com/questions/17432032/how-do-i-get-a-unix-timestamp-in-clojure
        week 604800
        day 86400
        crumb (core/get-crumb symbol)
        start_date (- now (* day 7)) 
        end_date now
        url (format "https://query1.finance.yahoo.com/v7/finance/download/%s?period1=%s&period2=%s&interval=1d&events=history&crumb=%s" symbol, start_date, end_date, crumb)]
    (->
     (client/get url)
     (:body)
     (clojure.string/split #"\n")
     rest                                        ;; remove header
     reverse                                     ;; want to find the most recent valid adj-close (5th position)
     split-out-adj-close
     first-not-null)))

(defn get-last-adj-close [s]
  (nth (clojure.string/split s #",") 5))

(defn get-prices [fnc commodities]
  (map (fn [[k v]] {k (fnc v)}) commodities))


;; (map (fn [[k v]] {k v}) commodities))

(defn get-commodity-prices []
  (get-prices get-data commodities))

;; ----------------------------------------------------------------------------------------------------
;; ----------------------------------------------------------------------------------------------------

