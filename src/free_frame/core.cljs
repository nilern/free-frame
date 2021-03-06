(ns free-frame.core
  (:require-macros [free-frame.core])
  (:require [free-frame.application :refer [Application]]
            [free-frame.event-loop :as event-loop]
            [reagent.core :as rg]
            [reagent.dom :as rdom]
            [reagent.ratom :as ratom]
            react
            [fell.core :as fell :refer [weave pure request-eff]]
            [fell.eff :as eff :refer [Pure Impure]]
            [fell.queue :as q]
            [fell.state :as st]
            [fell.lift :as lift]
            [taksi.core :as t]
            [kissabussi.core :as k]
            [cats.core :refer [mlet extract]]
            [cats.data :refer [Pair pair]]))

(def compiler (rg/create-compiler {:function-components true}))

(def context
  (let [context (react/createContext nil)]
    (set! (.-displayName context) "FreeFrameContext")
    context))

(defn- useApplication [] (react/useContext context))

(defn subscribe [^Application app [tag :as query]]
  (if-some [f (-> app .-subscriptions (get tag))]
    (let [db (.-db app)]
      (ratom/make-reaction #(f @db query)))
    (js/console.error (str "free-frame: no subscription handler registered for: " tag))))

(def dispatch event-loop/dispatch)

(let [{:keys [lift run]} (lift/make :taksi)]
  (def lift-task lift)
  (defn run-task [eff] (run eff k/context)))

(def app-db-label :free-frame/app-db)

(def get-app-db (request-eff [app-db-label (st/Get.)]))

(defn set-app-db [value*] (request-eff [app-db-label (st/Set. value*)]))

(defn update-app-db [f & args] (eff/-flat-map get-app-db (fn [db] (set-app-db (apply f db args)))))

(declare resume-app-db)

(defn- resume-app-db* [app eff]
  ;; HACK:
  (loop [eff eff]
    (condp instance? eff
      Pure (lift-task (t/resolved (extract eff)))
      Impure (let [^Impure eff eff
                   [request-label request] (.-request eff)
                   k (partial q/apply-queue (.-cont eff))]
               (if (= request-label app-db-label)
                 (condp instance? request
                   st/Get (recur (k @(.-db app)))
                   st/Set (mlet [:let [^st/Set request request]
                                 _ (lift-task (t/task (fn [_ resolve]
                                                        (resolve (reset! (.-db app) (.-new_value request))))))]
                            (resume-app-db* app (k nil))))
                 (fell/weave eff (pair app nil) resume-app-db))))))

(defn- resume-app-db [^Pair suspension] (resume-app-db* (.-fst suspension) (.-snd suspension)))

(defn run-app-db [eff app] (resume-app-db* app eff))

(defn render [app component container]
  (rdom/render [:> (.-Provider context) {:value app} component] container compiler))
