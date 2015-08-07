(defproject fullcontact/full.cljs "0.1.2-SNAPSHOT"
  :description "ClojureScript sugar - logging, browser API's etc."

  :url "https://github.com/fullcontact/full.cljs"

  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo}

  :deploy-repositories [["releases" {:url "https://clojars.org/repo/" :creds :gpg}]]

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.48"]
                 [cljs-ajax "0.3.13"]])
