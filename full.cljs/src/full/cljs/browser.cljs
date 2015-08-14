(ns full.cljs.browser
  "Browser Web API sugar."
  (:require [goog.json :as goog-json]))


;;; LOCATION


(defn redirect
      [url]
      (-> js/document
          .-location
          (set! url)))

(defn hash
      []
      (-> js/document
          .-location
          .-hash))


;;; LOCAL STORAGE


(defn set-item!
      "Set `key' in browser's localStorage to `val`."
      [key val]
      (let [v (goog-json/serialize (clj->js val))]
           (.setItem (.-localStorage js/window) key v)))


(defn get-item
      "Returns value of `key' from browser's localStorage."
      [key]
      (js->clj (goog-json/parse (.getItem (.-localStorage js/window) key))
               :keywordize-keys true))

(defn remove-item!
      "Remove the browser's localStorage value for the given `key`"
      [key]
      (.removeItem (.-localStorage js/window) key))
