(defproject fullcontact/full.cljs.async "0.1.3-SNAPSHOT"
  :description "Extensions and helpers for cljs.core.async."

  :url "https://github.com/fullcontact/full.cljs"

  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo}

  :deploy-repositories [["releases" {:url "https://clojars.org/repo/" :creds :gpg}]]

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.48"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]]

  :plugins [[lein-cljsbuild "1.0.6"]]

  :hooks [leiningen.cljsbuild]
  
  :clean-targets ^{:protect false}["target" "resources/test/compiled.js"]

  :aliases {"autotest" ["do" "clean," "cljsbuild" "auto" "test"]}

  :cljsbuild {:test-commands {"test" ["phantomjs"
                                      "resources/test/test.js"
                                      "resources/test/test.html"]}
              :builds [{:id "test"
                        :source-paths ["test"]
                        :compiler {:output-to "resources/test/compiled.js"
                                   :optimizations :whitespace
                                   :pretty-print true}}]})
