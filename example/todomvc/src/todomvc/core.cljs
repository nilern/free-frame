(ns todomvc.core
  (:require [free-frame.core :as fr]
            [free-frame.application :as app]
            [reagent.dom :as rdom]
            [todomvc.db :as db]
            [todomvc.subs :as subs]
            [todomvc.views :as views]
            [todomvc.events :as events]
            [todomvc.effects :as effects]))

(defn create-app [] (app/create db/initial subs/subscriptions events/handlers effects/handle))

(defn render [app] (fr/render app [views/todo-app] (js/document.getElementById "app")))

(defn ^:dev/after-load reset []
  (rdom/unmount-component-at-node (js/document.getElementById "app"))
  (render (create-app)))

(defn ^:export run []
  (render (create-app)))
