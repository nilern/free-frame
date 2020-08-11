(ns free-frame.core
  (:require-macros [free-frame.core])
  (:require [reagent.core :as rg]
            [reagent.dom :as rdom]
            [reagent.ratom :as ratom]
            react
            [fell.core :as fell]
            [fell.eff :refer [Pure Impure]]
            [fell.queue :as q]
            [fell.state :as st]
            [fell.lift :refer [lift]]
            [taksi.core :as t]
            [cats.core :refer [mlet extract]]
            [cats.data :refer [Pair pair]]))

(def compiler (rg/create-compiler {:function-components true}))

(def context
  (let [context (react/createContext. nil)]
    (set! (.-displayName context) "FreeFrameContext")
    context))

(deftype Application [db subscriptions event-handlers effect-handler])

(defn create-application [db subscriptions event-handlers effect-handler]
  (Application. (rg/atom db) subscriptions event-handlers effect-handler))

(defn useApplication [] (react/useContext context))

(defn subscribe [^Application app [tag :as query]]
  (let [f (-> app .-subscriptions (get tag))
        db (.-db app)]
    (ratom/make-reaction #(f @db query))))

(defn dispatch [^Application app, event]
  (let [handle-event (-> app .-event_handlers (get (first event)))
        handle-effects (.-effect_handler app)]
    (->> event handle-event (handle-effects app))))

(declare resume-app-db)

(defn- resume-app-db* [app eff]
  ;; HACK:
  (loop [eff eff]
    (condp instance? eff
      Pure (lift (t/resolved (extract eff)))
      Impure (let [^Impure eff eff
                   request (.-request eff)
                   k (partial q/apply-queue (.-cont eff))]
               (condp instance? request
                 st/Get (recur (k @(.-db app)))
                 st/Set (mlet [:let [^st/Set request request]
                               _ (lift (t/task (fn [_ resolve]
                                                 (resolve (reset! (.-db app) (.-new_value request))))))]
                          (resume-app-db* app (k nil)))
                 (fell/weave eff (pair app nil) resume-app-db))))))

(defn- resume-app-db [^Pair suspension] (resume-app-db* (.-fst suspension) (.-snd suspension)))

(defn run-app-db [eff app] (resume-app-db* app eff))

(defn render [app component container]
  (rdom/render [:> (.-Provider context) {:value app} component] container compiler))
