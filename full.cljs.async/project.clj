(defproject fullcontact/full.cljs.async "0.1.2"
  :description "Extensions and helpers for cljs.core.async."

  :url "https://github.com/fullcontact/full.cljs"

  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo}

  :deploy-repositories [["releases" {:url "https://clojars.org/repo/" :creds :gpg}]]

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.48"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]]

  :plugins [[lein-cljsbuild "1.0.6"]
            [com.cemerick/clojurescript.test "0.3.3"]]

  :hooks [leiningen.cljsbuild]

  :profiles {:dev {:plugins [[com.cemerick/clojurescript.test "0.3.3"]]}}

  :aliases {"autotest" ["do" "clean," "cljsbuild" "auto" "test"]}

  :cljsbuild {:test-commands {"test" ["phantomjs" :runner "target/test.js"]}
              :builds [{:id "test"
                        :notify-command ["phantomjs" :cljs.test/runner "target/test.js"]
                        :source-paths ["src" "test"]
                        :compiler {:output-to "target/test.js"
                                   :optimizations :whitespace
                                   :pretty-print true}}]
              }
  )
