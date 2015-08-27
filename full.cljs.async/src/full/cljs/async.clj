(ns full.cljs.async
  (:require [cljs.core.async.macros :refer [go go-loop]]))

(defmacro <?
  "Same as core.async <! but throws an exception if the channel returns a
  throwable object. Also will not crash if channel is nil."
  [ch]
  `(full.cljs.async/throw-if-throwable
    (let [ch# ~ch]
      (when ch#
        (cljs.core.async/<! ch#)))))

(defmacro alt?
  "Same as core.async alt! but throws an exception if the channel returns a
  throwable object."
  [& clauses]
  `(full.cljs.async/throw-if-throwable (cljs.core.async.macros/alt! ~@clauses)))

(defmacro alts?
  "Same as core.async alts! but throws an exception if the channel returns a
  throwable object."
  [ports]
  `(let [[val# port#] (cljs.core.async/alts! ~ports)]
     [(full.cljs.async/throw-if-throwable val#) port#]))


(defmacro go-try
  "Asynchronously executes the body in a go block. Returns a channel which
  will receive the result of the body when completed or an exception if one
  is thrown."
  [& body]
  `(cljs.core.async.macros/go (try ~@body (catch js/Error e# e#))))

(defmacro go-try>
  "Same as go-try, but puts errors directly on a channel and returns
  nil on the resulting channel."
  [err-chan & body]
  `(cljs.core.async.macros/go
     (try
       ~@body
       (catch js/Error e#
         (cljs.core.async/>! ~err-chan e#)))))

(defmacro go-retry
  [{:keys [retries delay error-fn]
    :or {error-fn nil, retries 5, delay 1}} & body]
  `(let [error-fn# ~error-fn]
     (go-loop
       [retries# ~retries]
       (let [res# (try ~@body (catch js/Error e# e#))]
         (if (and (instance? js/Error res#)
                  (or (not error-fn#) (error-fn# res#))
                  (> retries# 0))
           (do
             (cljs.core.async/<! (cljs.core.async/timeout (* ~delay 1000)))
             (recur (dec retries#)))
           res#)))))

(defmacro <<!
  "Takes multiple results from a channel and returns them as a vector.
  The input channel must be closed."
  [ch]
  `(let [ch# ~ch]
     (cljs.core.async/<! (cljs.core.async/into [] ch#))))

(defmacro <<?
  "Takes multiple results from a channel and returns them as a vector.
  Throws if any result is an exception."
  [ch]
  `(->> (<<! ~ch)
        (map full.cljs.async/throw-if-throwable)
        ; doall to check for throwables right away
        (doall)))

(defmacro <!*
  "Takes one result from each channel and returns them as a collection.
  The results maintain the order of channels."
  [chs]
  `(let [chs# ~chs]
     (loop [chs# chs#
            results# []]
       (if-let [head# (first chs#)]
         (->> (cljs.core.async/<! head#)
              (conj results#)
              (recur (rest chs#)))
         results#))))

(defmacro <?*
  "Takes one result from each channel and returns them as a collection.
  The results maintain the order of channels. Throws if any of the
  channels returns an exception."
  [chs]
  `(let [chs# ~chs]
     (loop [chs# chs#
            results# []]
       (if-let [head# (first chs#)]
         (->> (<? head#)
              (conj results#)
              (recur (rest chs#)))
         results#))))

(defmacro go-loop-try
  "Returns result of the loop or a throwable in case of an exception."
  [bindings & body]
  `(go-try (loop ~bindings ~@body) ))

(defmacro go-loop-try>
  "Put throwables arising in the go-loop on an error channel."
  [err-chan bindings & body]
  `(cljs.core.async.macros/go
     (try
       (loop ~bindings
         ~@body)
       (catch js/Error e#
         (cljs.core.async/>! ~err-chan e#)))))

(defmacro ^{:private true} assert-args
   [& pairs]
   `(do (when-not ~(first pairs)
          (throw (IllegalArgumentException.
                  (str (first ~'&form) " requires " ~(second pairs) " in " ~'*ns* ":" (:line (meta ~'&form))))))
        ~(let [more (nnext pairs)]
           (when more
             (list* `assert-args more)))))

(defmacro go-for
   "List comprehension adapted from clojure.core 1.7. Takes a vector of
  one or more binding-form/collection-expr pairs, each followed by
  zero or more modifiers, and yields a channel of evaluations of
  expr. It is eager on all but the outer-most collection. TODO

  Collections are iterated in a nested fashion, rightmost fastest, and
  nested coll-exprs can refer to bindings created in prior
  binding-forms.  Supported modifiers are: :let [binding-form expr
  ...],
   :while test, :when test. If a top-level entry is nil, it is skipped
  as it cannot be put on channel.

  (<! (cljs.core.async/into [] (go-for [x (range 10) :let [y (<! (go 4))] :while (< x y)] [x y])))"
   {:added "1.0"}
   [seq-exprs body-expr]
   (assert-args
    (vector? seq-exprs) "a vector for its binding"
    (even? (count seq-exprs)) "an even number of forms in binding vector")
   (let [to-groups (fn [seq-exprs]
                     (reduce (fn [groups [k v]]
                               (if (keyword? k)
                                 (conj (pop groups) (conj (peek groups) [k v]))
                                 (conj groups [k v])))
                             [] (partition 2 seq-exprs)))
         err (fn [& msg] (throw (IllegalArgumentException. ^String (apply str msg))))
         emit-bind (fn emit-bind [res-ch [[bind expr & mod-pairs]
                                          & [[_ next-expr] :as next-groups]]]
                     (let [giter (gensym "iter__")
                           gxs (gensym "s__")
                           do-mod (fn do-mod [[[k v :as pair] & etc]]
                                    (cond
                                      (= k :let) `(let ~v ~(do-mod etc))
                                      (= k :while) `(when ~v ~(do-mod etc))
                                      (= k :when) `(if ~v
                                                     ~(do-mod etc)
                                                     (recur (rest ~gxs)))
                                      (keyword? k) (err "Invalid 'for' keyword " k)
                                      next-groups
                                      `(let [iterys# ~(emit-bind res-ch next-groups)
                                             fs# (<? (iterys# ~next-expr))]
                                         (if fs#
                                           (concat fs# (<? (~giter (rest ~gxs))))
                                           (recur (rest ~gxs))))
                                      :else `(let [res# ~body-expr]
                                               (when res# (cljs.core.async/>! ~res-ch res#))
                                               (<? (~giter (rest ~gxs))))
                                      #_`(cons ~body-expr (<? (~giter (rest ~gxs))))))]
                       `(fn ~giter [~gxs]
                          (go-try
                           (loop [~gxs ~gxs]
                             (let [~gxs (seq ~gxs)]
                               (when-let [~bind (first ~gxs)]
                                 ~(do-mod mod-pairs))))))))
         res-ch (gensym "res_ch__")]
     `(let [~res-ch (cljs.core.async/chan)
            iter# ~(emit-bind res-ch (to-groups seq-exprs))]
        (cljs.core.async.macros/go (try (<? (iter# ~(second seq-exprs)))
                 (catch js/Error e#
                   (cljs.core.async/>! ~res-ch e#))
                 (finally (cljs.core.async/close! ~res-ch))))
        ~res-ch)))
