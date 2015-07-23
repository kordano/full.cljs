(ns full.cljs.log)

(defmacro trace [& args]
  `(.trace js/console (full.cljs.log/format-log [~@args])))

(defmacro debug [& args]
  `(.debug js/console (full.cljs.log/format-log [~@args])))

(defmacro info [& args]
  `(.info js/console (full.cljs.log/format-log [~@args])))

(defmacro warn [& args]
  `(.warn js/console (full.cljs.log/format-log [~@args])))

(defmacro error [& args]
  `(.error js/console (full.cljs.log/format-log [~@args])))
