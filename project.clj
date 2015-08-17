(defproject fullcontact/full.cljs.parent "0.1.2"
  :description "ClojureScript sugar (full.monty's little brother)."

  :url "https://github.com/fullcontact/full.cljs"

  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo}

  :plugins [[lein-sub "0.3.0"]
            [lein-set-version "0.4.1"]]

  :sub ["full.cljs"
        "full.cljs.async"]

  :release-tasks  [["vcs" "assert-committed"]
                   ["set-version"]
                   ["sub" "set-version"]
                   ["vcs" "commit"]
                   ["vcs" "tag"]
                   ["sub" "deploy"]
                   ["set-version" ":point"]
                   ["sub" "set-version" ":point"]
                   ["vcs" "commit"]
                   ["vcs" "push"]])