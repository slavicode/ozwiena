(ns ozwiena.core
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [compojure.core :refer [GET POST defroutes]]
            [ring.util.codec :refer [url-encode]]
            [ring.adapter.jetty :as jetty]
            [cheshire.core :as json]
            [clojure.java.io :as io]
            [clj-http.client :as client]
            [environ.core :refer [env]]
            [overtone.at-at :refer [every mk-pool]])
  (:import [java.net URLEncoder])
  (:use [ring.util.response]))

(def root (if (env :dev)
            "resources/dev/"
            "resources/public/"))

(defn render [file]
  (file-response file {:root root}))

(def bearer "AAAAAAAAAAAAAAAAAAAAAE9GbQAAAAAAchwcBFzh89utMKuXX1B9lBS0Pro%3Djg8c8bJchxBvwXa6zvgfPF4jmxnTrXmBE6eWEfzDjawSkZDxWk")

(defn- log [msg & vals]
  (let [line (apply format msg vals)]
    (locking System/out (println line))))

(defn encode-params [request-params]
  (let [encode #(URLEncoder/encode (str %) "UTF-8")
        coded (for [[n v] request-params] (str (encode (name n)) "=" (encode v)))]
    (apply str (interpose "&" coded))))

(defn twitter-search [query]
  (let [url (str "https://api.twitter.com/1.1/search/tweets.json?"
                 (encode-params query))]
    (client/get url
                {:headers {"Authorization" (str "Bearer " bearer)}
                 :as :json})))

(defn json-response [data]
  (-> (response (json/generate-string data))
      (content-type "aplication/json")
      (charset "UTF-8")))

(defroutes app-routes
  (GET "/" [] (render "index.html"))

  (GET "/tweets.json" [& params] (try
                                   (json-response (:body (twitter-search params)))
                                   (catch Exception e (-> (response (str e))
                                                          (status 500)))))

  (route/resources "/")
  (route/not-found "Page not found"))


(def app
  (-> #'app-routes
      (handler/api)))

(defn -main []
  (when (env :ping)
    (let [addr (env :ping)
          pool (mk-pool)]
      (every (* 60 1000)
             #(try
                   (log (str "Ping: " addr))
                   (client/get addr)
                   (catch Exception e (print e)))
             pool)))
  (let [port (Integer/parseInt (get (System/getenv) "PORT" "5000"))]
    (jetty/run-jetty app {:port port})))
