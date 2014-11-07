(ns ozwiena.core
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require [goog.events :as events]
            [clojure.string :refer [join]]
            [cljs.core.async :refer [put! <! >! chan timeout]]
            [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [cljs-http.client :as http]
            [goog.net.XhrIo :as xhr]))

;; Lets you do (prn "stuff") to the console
(enable-console-print!)

(def app-state
  (atom {:query "#oźwiena"
         :tweets []}))

(defn uri [uri params]
  (let [encode js/encodeURIComponent
        coded (for [[n v] params] (str (encode (name n)) "=" (encode v)))]
    (str uri "?" (join "&" coded))))

(defn fetch-tweets [query]
  (let [succ (chan)]
    (xhr/send (uri "/tweets.json" {:q query
                                   :result_type "newest"})
              #(put! succ (-> % .-target .getResponseText js/JSON.parse)))
    succ))

(defn reload-tweets [app query]
  (let [query (fetch-tweets query)]
    (go (let [result (<! query)]
          (om/update! app [:tweets] (.-statuses result))))))

(defcomponent tweet-image [image owner]
  (render [_]
          (dom/div {:class "image"}
                   (if image
                     (dom/img {:src (aget image 0 "media_url_https")})))))

(defn author [name]
  (dom/div {:class "author"}
           (dom/a {:href (str "https://twitter.com/" name)}
                  (str "@" name))))

(defcomponent tweet-view [tweet owner]
  (render [_]
          (let [user (aget tweet "user")
                text (aget tweet "text")
                media (aget tweet "entities" "media")]
            (dom/div {:class "tweet"}
                     (dom/div {:class "avatar"}
                              (dom/img {:src (aget user "profile_image_url_https")}))
                     (dom/div {:class "content"}
                              (author (aget user "screen_name"))
                              (dom/div {:class "text"}
                                       text)
                              (om/build tweet-image
                                        media))))))

(defn query-change [app query]
  (prn (str "Change: " query))
  (if (not= (:query @app) query)
    (om/transact! app :query (fn [] query))))

(defcomponent query-view [app owner]
  (init-state [_]
              {:update (chan)})
  (will-mount [_]
              (let [update (om/get-state owner :update)]
                (go (loop []
                      (let [value (<! update)]
                        (prn (str "Search for " value))
                        (om/transact! app :query (fn [] value))
                        (reload-tweets app value))))))
  (render-state [_ {:keys [update]}]
                (dom/h1 {:content-editable true
                         :on-blur #(put! update (aget % "target" "innerHTML"))}
                        (:query app))))


(defcomponent ozwiena-app [app owner]
  (will-mount [_]
              (reload-tweets app (:query app))
              (js/setInterval (fn [] (reload-tweets app (:query @app))) (* 30 1000)))
  (render [_]
          (dom/div
            (om/build query-view app)
            (om/build-all tweet-view (:tweets app)))))

(om/root ozwiena-app
         app-state
         {:target (.getElementById js/document "app")})
