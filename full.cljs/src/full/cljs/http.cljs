(ns full.cljs.http
  (:require [cljs.core.async :refer [chan put! close!]]
            [ajax.core :refer [ajax-request
                               raw-response-format
                               json-request-format
                               json-response-format]]
            [full.cljs.log :as log]))

(defn req>
  [{:keys [url method params headers request-format response-format]
    :or {method :get
         response-format (raw-response-format)}}]
  (let [ch (chan 1)]
    (ajax-request {:uri url
                   :method method
                   :params params
                   :headers headers
                   :format request-format
                   :response-format response-format
                   :handler (fn [[ok res]]
                              (if ok
                                (put! ch res)
                                (put! ch (js/Error. res)))
                              (close! ch))})
    ch))


(defn json-req>
  [req]
  (-> req
      (assoc :request-format (json-request-format)
             :response-format (json-response-format {:keywords? true}))
      (req>)))