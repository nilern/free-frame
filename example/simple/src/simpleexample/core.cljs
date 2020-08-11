(ns simpleexample.core
  (:require [free-frame.core :as fr]
            [cats.core :refer [mlet]]
            [fell.state :as st]
            [fell.lift :as lift]
            [taksi.core :as t]
            [kissabussi.core :as k]
            [clojure.string :as str]))

;;;; # Model

(defn initial-db []
  {:time (js/Date.)
   :time-color "#f34"})

;;;; # Events

(def event-handlers
  {:timer (fn [[_ new-time]]
            (mlet [db st/get]
              (st/set (assoc db :time new-time))))
   :time-color-change (fn [[_ new-color]]
                        (mlet [db st/get]
                          (st/set (assoc db :time-color new-color))))})

;;;; # Effects

(defn handle-effects [app eff]
  (->> (fr/run-app-db eff app)
       (lift/run k/context)
       (t/fork (fn [err] (throw (js/Error. err)))
               (fn [res] (assert (nil? res))))))

;;;; # Views

(defn clock []
  (let [app (fr/useApplication)
        time-string (-> @(.-db app) :time .toTimeString (str/split " ") first)]
    [:div.example-clock {:style {:color (:time-color @(.-db app))}}
     time-string]))

(defn color-input []
  (let [app (fr/useApplication)]
    [:div.color-input
     "Time color: "
     [:input {:type "text"
              :value (:time-color @(.-db app))
              :on-change #(fr/dispatch app [:time-color-change (-> % .-target .-value)])}]]))

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
  (let [app (fr/create-application (initial-db) event-handlers handle-effects)]
    (js/setInterval (fn [] (dispatch-timer-event app)) 1000)
    (render app)))
