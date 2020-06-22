(ns example.client.app
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
    [clojure.pprint :refer [pprint]]
    [cljs.core.async :refer [<!]]
    [biff.client :as bc]
    [example.client.app.components :as c]
    [example.client.app.db :as db]
    [example.client.app.mutations :as m]
    [example.client.app.system :as s]
    [rum.core :as rum]
    [javelin.core :as j :refer [defc defc= cell cell=] :include-macros true]
    [hoplon.core :as h :refer [defelem for-tpl case-tpl when-tpl div] :include-macros true]
    [hoplon.jquery]))

(def myval (cell 2000))

(defn replace-elem! [target-id new-elem]
  (let [target-elem (.getElementById js/document target-id)
        parent (.-parentElement target-elem)]
    (.replaceChild parent (new-elem) target-elem)))

(defelem myapp []
  (div :id "app"
       :click #(swap! myval inc)
       (cell= (str myval))))

(defn ^:export mount []
  (replace-elem! "app" myapp))
  ;(rum/mount  (js/document.querySelector "#app")))
;;; mount (c/main) instead

(defn ^:export init []
  (reset! s/system
    (bc/init-sub {:handler m/api
                  :sub-data db/sub-data
                  :subscriptions db/subscriptions}))
  (mount))

(comment
  (-> (m/api-send [:example/echo {:foo "bar"}])
    <!
    pprint
    go))
