(ns simpleexample.core
  (:require [free-frame.core :as fr :refer [defnc update-app-db]]
            [free-frame.application :as app]
            [cats.core :refer [mlet]]
            [taksi.core :as t]
            [clojure.string :as str]))

;;;; # Model

(defn initial-db []
  {:time (js/Date.)
   :time-color "#f34"})

;;;; # Subscriptions

(def subscriptions
  {:time-color (fn [db _] (:time-color db))
   :time (fn [db _] (:time db))})

;;;; # Events

(def event-handlers
  {:timer (fn [[_ new-time]] (update-app-db assoc :time new-time))

   :time-color-change (fn [[_ new-color]] (update-app-db assoc :time-color new-color))})

;;;; # Effects

(defn handle-effects [app eff]
  (->> (fr/run-app-db eff app)
       fr/run-task
       (t/fork (fn [err] (throw (js/Error. err)))
               (fn [res] (assert (nil? res))))))

;;;; # Views

(defnc clock [app]
  (let [time-string (-> @(fr/subscribe app [:time]) .toTimeString (str/split " ") first)]
    [:div.example-clock {:style {:color @(fr/subscribe app [:time-color])}}
     time-string]))

(defnc color-input [app]
  [:div.color-input
   "Time color: "
   [:input {:type "text"
            :value @(fr/subscribe app [:time-color])
            :on-change #(fr/dispatch app [:time-color-change (-> % .-target .-value)])}]])

(defn ui []
  [:div
   [:h1 "Hello world, it is now"]
   [clock]
   [color-input]])

;;;; # Initialization

(defn dispatch-timer-event [app]
  (let [now (js/Date.)]
    (fr/dispatch app [:timer now])))

(defn render [app]
  (fr/render app [ui] (js/document.getElementById "app")))

(defn ^:export run []
  (let [app (app/create (initial-db) subscriptions event-handlers handle-effects)]
    (js/setInterval (fn [] (dispatch-timer-event app)) 1000)
    (render app)))
