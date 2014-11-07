(defproject ozwiena "0.0.1-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.reader "0.8.8"]
                 [environ "1.0.0"]
                 ;; CLJ
                 [ring "1.3.1"]
                 [compojure "1.1.9"]
                 [cheshire "5.3.1"]
                 [clj-http "1.0.1"]
                 [overtone/at-at "1.2.0"]
                 ;; CLJS
                 [org.clojure/clojurescript "0.0-2322"]
                 [org.clojure/core.async "0.1.338.0-5c5012-alpha"]
                 [cljs-http "0.1.16"]
                 [secretary "1.2.1"]
                 [om "0.7.3"]
                 [prismatic/om-tools "0.3.6"]
                 [com.cognitect/transit-cljs "0.8.188"]]

  :min-lein-version "2.0.0"

  :main ozwiena.core
  :uberjar-name "ozwiena.jar"

  :plugins [[lein-cljsbuild "1.0.3"]
            [lein-ring "0.8.7"]
            [lein-pdo "0.1.1"]
            [lein-haml-sass "0.2.7-SNAPSHOT"]
            [lein-environ "1.0.0"]]

  :aliases {"dev" ["pdo"
                   "cljsbuild" "auto" "app,"
                   "sass" "auto,"
                   "ring" "server-headless"]}

  :profiles {:dev
             {:cljsbuild {:builds {:app
                                   {:compiler {:source-map true}}}}
              :env {:dev true
                    :twitter-key "TcqDkElM3wuSDC8MSxw08yGlF"
                    :twitter-secret "Lrtf84251KtoiLmEWPQL7OqN5W8F8ZLjw5IY9UkZWqToNZ5iIK"}}
             :uberjar
             {:env {:production true}
              :omit-source true
              :aot :all
              :cljsbuild {:builds {:app
                                   {:compiler {:optimizations :advanced
                                               :pretty-print false
                                               :output-wrapper false
                                               :preamble ["react/react.min.js"
                                                          "moment.min.js"
                                                          "emojione.min.js"]
                                               :closure-warnings
                                               {:externs-validation :off
                                                :non-standard-jsdoc :off}}}}}}}

  :hooks [leiningen.cljsbuild
          leiningen.sass]

  :ring {:handler ozwiena.core/app
         :init    ozwiena.core/init}

  :source-paths ["src/clj" "src/cljs" "externs/"]

  :cljsbuild {:builds {:app
                       {:source-paths ["src/cljs"]
                        :compiler {:output-to "resources/public/js/ozwiena.js"
                                   :externs ["react/externs/react.js"
                                             "externs/emojione.js"
                                             "externs/moment.js"]
                                   :optimizations :none
                                   :output-dir "resources/public/js/out"}}}}
  :sass {:src "resources/sass"
         :output-directory "resources/public/css"})
