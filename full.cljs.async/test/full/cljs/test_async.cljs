(ns full.cljs.test-async
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cemerick.cljs.test :as t :refer-macros [deftest is done]]
            [full.cljs.async :refer [pmap>>] :refer-macros [go-try <? <<? <?*]]
            [cljs.core.async :refer [chan take! >! close!]]))


(deftest ^:async test-<<?
  (take! (go (let [ch (chan 2)]
               (>! ch "1")
               (>! ch "2")
               (close! ch)
               (<<? ch)))
         (fn [res]
           (is (= res ["1" "2"]))
           (done))))

(deftest ^:async test-go-try-<?
  (go
    (is (thrown? js/Error
          (<? (go-try
                (throw (js/Error.))))))
    (done)))

(deftest ^:async test-<?*
  (go
    (is (= (<?* [(go "1") (go "2")])
           ["1" "2"]))
    (is (= (<?* (list (go "1") (go "2")))
           ["1" "2"]))
    (is (thrown? js/Error
                 (<?* [(go "1") (go (js/Error))])))
    (done)))

(deftest ^:async test-pmap>>
  (go
    (is (= (->> (let [ch (chan)]
                  (go (doto ch (>! 1) (>! 2) close!))
                  ch)
                (pmap>> #(go (inc %)) 2)
                (<<?)
                (set))
           #{2 3}))
    (done)))