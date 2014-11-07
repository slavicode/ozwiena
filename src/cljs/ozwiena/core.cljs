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
         :tweets []
         :delay 15}))

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
          (om/update! app [:tweets] (aget result "statuses"))))))

(defn handle-enter [e]
  (when (== (.-which e) 13)
    (let [event (js/CustomEvent. "blur")]
      (.dispatchEvent (.-target e) event))
    (.preventDefault e)))

(defn parse-time [string]
  (js/moment string "... MMM DD hh:mm:ss Z YYYY"))

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
                created (parse-time (aget tweet "created_at"))
                text (aget tweet "text")
                media (aget tweet "entities" "media")]
            (dom/div {:class "tweet"}
                     (dom/div {:class "avatar"}
                              (dom/img {:src (aget user "profile_image_url_https")}))
                     (dom/div {:class "content"}
                              (dom/div {:class "metadata"}
                                       (author (aget user "screen_name"))
                                       (dom/div {:class "created"}
                                                (.fromNow created)))
                              (dom/div {:class "text"
                                        :dangerously-set-innerHTML #js {:__html (js/emojione.unicodeToImage text)}})
                              (om/build tweet-image
                                        media))))))

(defcomponent tweets [app owner]
  (render [_]
          (dom/div {:class "tweets"}
                   (om/build-all tweet-view (:tweets app)))))

(defcomponent info [app owner]
  (init-state [_]
              {:update (chan)})
  (will-mount [_]
              (let [update (om/get-state owner :update)]
                (go (loop []
                      (let [value (<! update)]
                        (prn (str "Search for " value))
                        (om/transact! app :query (fn [] value))
                        (reload-tweets app value))
                      (recur)))))
  (render-state [_ {:keys [update]}]
                (dom/div {:class "info"}
                         (dom/h1 {:content-editable true
                                  :on-blur #(put! update (aget % "target" "innerHTML"))
                                  :on-key-down handle-enter}
                                 (:query app))
                         (dom/div {:class "copyrights"}
                                  "Copyrights 2014 © by "
                                  (dom/a {:href "https://github.com/slavicode"}
                                         "SlaviCode")))))


(defcomponent ozwiena [app owner]
  (will-mount [_]
              (reload-tweets app (:query app))
              (js/setInterval (fn [] (reload-tweets app (:query @app)))
                              (* (:delay app) 1000)))
  (render [_]
          (dom/div {:class "app"}
                   (om/build info app)
                   (om/build tweets app))))

(om/root ozwiena
         app-state
         {:target (.getElementById js/document "app")})
