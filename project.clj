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
                 ;; CLJS
                 [org.clojure/clojurescript "0.0-2322"]
                 [org.clojure/core.async "0.1.338.0-5c5012-alpha"]
                 [cljs-http "0.1.16"]
                 [secretary "1.2.1"]
                 [om "0.7.3"]
                 [prismatic/om-tools "0.3.6"]
                 [com.cognitect/transit-cljs "0.8.188"]]

  :min-lein-version "2.5.0"

  :uberjar-name "ozwiena.jar"

  :plugins [[lein-cljsbuild "1.0.3"]
            [lein-ring "0.8.7"]
            [lein-pdo "0.1.1"]
            [lein-haml-sass "0.2.7-SNAPSHOT"]
            [lein-environ "1.0.0"]]

  :aliases {"dev" ["pdo"
                   "cljsbuild" "auto" "dev,"
                   "sass" "auto,"
                   "ring" "server-headless"]
            "uberjar" ["do"
                       "cljsbuild" "once" "release,"
                       "sass" "once,"
                       "uberjar"]}

  :profiles {:uberjar {:env {:production true}
                       :omit-source true
                       :aot :all}}

  :ring {:handler ozwiena.core/app
         :init    ozwiena.core/init}

  :source-paths ["src/clj"]

  :cljsbuild {:builds {:dev
                       {:source-paths ["src/cljs"]
                        :compiler {:output-to "resources/public/js/ozwiena.js"
                                   :output-dir "resources/public/js/out"
                                   :optimizations :none
                                   :source-map true
                                   :externs ["react/externs/react.js"]}}
                       :release
                       {:source-paths ["src/cljs"]
                        :compiler {:output-to "resources/public/js/ozwiena.js"
                                   :source-map "resources/public/js/ozwiena.js.map"
                                   :output-dir "resources/public/js"
                                   :optimizations :advanced
                                   :pretty-print false
                                   :output-wrapper false
                                   :preamble ["react/react.min.js"]
                                   :externs ["react/externs/react.js"]
                                   :closure-warnings
                                   {:non-standard-jsdoc :off}}}}}
  :sass {:src "resources/sass"
         :output-directory "resources/public/css"})
