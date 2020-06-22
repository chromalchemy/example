(ns example.client.app.db
  (:require
    [example.logic :as logic]
    [trident.util :as u]
    [rum.core]
    [javelin.core :refer [cell cell= defc defc=]]))

(defonce db (cell {}))

; same as (do (rum.core/cursor-in db [:sub-data]) ...)
(u/defcursors db
  sub-data [:sub-data])


(defn cursor-cell [map-cell k]
  (cell=
    (map-cell k) ; show value at key
    #(swap! map-cell ; update value at key
       assoc k %)))

(def data (cursor-cell db :sub-data))

(def uid (cell= (get-in data [:uid nil :uid])))
(def user-ref (cell= {:user/id uid}))
(def id->users (cell= (:users data)))
(def self (cell= (get id->users user-ref)))
(def email (cell= (:user/email self)))
(def signed-in (cell= (and (some? uid) (not= :signed-out uid))))

(def id->public-users (cell= (:public-users data)))
(def public-self (cell= (get id->public-users {:user.public/id uid})))
(def display-name (cell= (:display-name public-self)))

(def game
  (cell=
    (->> data
      :games
      vals
      (filter #(contains? (:users %) uid))
      first)))

(def game-id (cell= (:game/id game)))

(def participants (cell= (:users game)))
(def x (cell= (:x game)))
(def o (cell= (:o game)))
(def board (cell= (:board game)))

(def current-player (cell= (get game (logic/current-player game))))
(def winner (cell= (get game (logic/winner game))))
(def game-over (cell= (logic/game-over? game)))
(def draw (cell= (and game-over (not winner))))

(def biff-subs
  (cell=
    [; :uid is a special non-Crux query. Biff will respond
     ; with the currently authenticated user's ID.
     :uid
     (when signed-in
       [{:table :users
         :id user-ref}
        {:table :public-users
         :id {:user.public/id uid}}
        {:table :games
         :where [[:users uid]]}])
     (for [u (:users game)]
       {:table :public-users
        :id {:user.public/id u}})]))

(def subscriptions
  (cell=
    (->> biff-subs
      flatten
      (filter some?)
      (map #(vector :biff/sub %))
      set)))
