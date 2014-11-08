(ns ozwiena.core
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [compojure.core :refer [GET POST defroutes]]
            [ring.util.codec :refer [url-encode]]
            [ring.adapter.jetty :as jetty]
            [cheshire.core :as json]
            [clojure.java.io :as io]
            [clojure.string :refer [join]]
            [clj-http.client :as client]
            [environ.core :refer [env]]
            [overtone.at-at :refer [every mk-pool]])
  (:import [java.net URLEncoder]
           [java.util Base64])
  (:use [ring.util.response]))

(def root (if (env :dev)
            "resources/dev/"
            "resources/public/"))

(defn render [file]
  (file-response file {:root root}))

(defn base64 [data]
  (-> Base64
      (. getEncoder)
      (.encode (.getBytes data))
      String.))

(defn get-bearer [key secret]
  (let [auth (base64 (str key ":" secret))]
    (-> (client/post "https://api.twitter.com/oauth2/token"
                     {:headers {"Authorization" (str "Basic " auth)}
                      :content-type "application/x-www-form-urlencoded;charset=UTF-8"
                      :body "grant_type=client_credentials"})
        :body
        (json/parse-string true)
        :access_token)))

(def bearer (atom ""))

(defn- log [msg & vals]
  (let [line (apply format msg vals)]
    (locking System/out (println line))))

(defn encode-params [request-params]
  (let [encode #(URLEncoder/encode (str %) "UTF-8")
        coded (for [[n v] request-params] (str (encode (name n)) "=" (encode v)))]
    (apply str (interpose "&" coded))))

(defn twitter-search [query]
  (let [url (str "https://api.twitter.com/1.1/search/tweets.json?"
                 (encode-params query))
        key (env :twitter-key)
        secret (env :twitter-secret)]
    (if (empty? @bearer)
      (swap! bearer (fn [_ e] e) (get-bearer key secret)))
    (client/get url
                {:headers {"Authorization" (str "Bearer " @bearer)}
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

  (GET "/ping" [] (str "Pong!"))

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
