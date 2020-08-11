(ns free-frame.application
  (:require [reagent.core :as rg]))

(deftype Application [db subscriptions
                      ^:mutable running, ^:mutable event-queue
                      event-handlers effect-handler])

(defn create [db subscriptions event-handlers effect-handler]
  (Application. (rg/atom db) subscriptions false #queue [] event-handlers effect-handler))
