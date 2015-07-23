(ns full.cljs.log
  (:require-macros [full.cljs.log :as log])
  (:require [clojure.string :as strs]))

(defn- format-log [args]
  (->> args
       (map #(if (string? %)
              % (pr-str %)))
       (strs/join " ")))

(defn enable-log-print!
  "Set *print-fn* to console.log"
  []
  (set! *print-newline* false)
  (set! *print-fn* (fn [& args] (.log js/console (format-log args)))))

(defn do-debug
  "Logs all arguments except the last one, evaluates last one, logs it with
   info loglevel and returns it's value."
  [& args]
  (log/debug (clojure.string/join ", " args))
  (last args))
