(ns full.cljs.test-async
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [full.cljs.async :refer [pmap>>] :refer-macros [<? <<? <<! <?* go-try go-try> alt? go-loop-try go-loop-try> go-for]]
            [cljs.core.async :refer [chan close! take! >! into]]
            [cljs.test :refer-macros [deftest is async run-tests]]))

(enable-console-print!)

(defn identity-chan
  [x]
  (let [c (chan 1)]
    (go (>! c x)
        (close! c))
    c))


(deftest test-identity-chan
  (async done
    (go
      (is (= (<! (identity-chan 42)) 42))
      (done))))


(deftest test-<?
  (async done
         (go
           (is (= (<? (identity-chan 42)) 42))
           (done))))

(deftest test-<<!
  (async done
         (go
           (is (= ["1" "2"]
                  (<!
                   (go
                     (<<!
                      (let [ch (chan 2)]
                        (>! ch "1")
                        (>! ch "2")
                        (close! ch)
                        ch))))))
           (done))))

(deftest test-<<?
  (async done
         (go
           (is (= ["1" "2"]
                  (<! (go (<<? (let [ch (chan 2)]
                                 (>! ch "1")
                                 (>! ch "2")
                                 (close! ch)
                                 ch))))))
           (done))))


(deftest test-alt?
  (async done
         (let [c (identity-chan 42)]
           (go
             (is (= [42 :foo]
                    (alt? (identity-chan 42)
                          ([c] [c :foo]))))
             (done)))))

(deftest test-go-try-<?
  (async done
         (go
           (is (thrown? js/Error
                        (<? (go-try
                             (throw (js/Error.))))))
           (done))))

(deftest test-<?*
  (async done
         (go
           (is (= (<?* [(go "1") (go "2")])
                  ["1" "2"]))
           (is (= (<?* (list (go "1") (go "2")))
                  ["1" "2"]))
           (is (thrown? js/Error
                        (<?* [(go "1") (go (js/Error))])))
           (done))))

(deftest test-pmap>>
  (async done
         (go
           (is (= (->> (let [ch (chan)]
                         (go (doto ch (>! 1) (>! 2) close!))
                         ch)
                       (pmap>> #(go (inc %)) 2)
                       (<<?)
                       (set))
                  #{2 3}))
           (done))))

(deftest test-go-try>
  (let [err-chan (chan)]
    (async done
           (go
             (is (thrown? js/Error
                          (do
                            (go-try> err-chan (let [ch (chan 2)]
                                                (>! ch "1")
                                                (>! ch (js/Error.))
                                                (close! ch)
                                                (<<? ch)))
                            (<? err-chan))))
             (done)))))

(deftest test-go-loop-try
  (async done
         (go
           (is (thrown? js/Error
                        (<?
                         (go-loop-try
                          [ch (chan 2)
                           inputs ["1" "2"]]
                          (when-not (empty? inputs)
                            (>! ch (first inputs))
                            (throw (js/Error.))
                            (recur ch (rest inputs)))))))
           (done))))


(deftest test-go-loop-try>
  (let [err-chan (chan)]
    (async done
           (go
             (is (thrown? js/Error
                          (do
                            (go-loop-try>
                             err-chan
                             [c0 (chan 2)
                              inputs ["1" "2"]]
                             (when-not (empty? inputs)
                               (>! c0 (first inputs))
                               (throw (js/Error.))
                               (recur c0 (rest inputs))))
                            (<? err-chan))))
             (done)))))


(deftest test-go-for
  (async done
         (go
           (is (= [[0 4] [1 4] [2 4] [3 4]]
                  (<!
                   (into []
                         (go-for [x (range 10)
                                  :let [y (<! (go 4))]
                                  :while (< x y)]
                                 [x y])))))
           (done))))

(run-tests)

(defn ^:export run [])

