(ns free-frame.event-loop
  (:require [free-frame.application :refer [Application]]
            [goog.async.nextTick]))

(def ^:private next-tick goog.async.nextTick)

(defn- handle-event [^Application app event]
  (let [tag (first event)]
    (if-some [handle-event (-> app .-event_handlers (get tag))]
      (let [handle-effects (.-effect_handler app)]
        (->> event handle-event (handle-effects app)))
      (js/console.error (str "free-frame: no event handler registered for: " tag)))))

(defn- handle-events [^Application app]
  (set! (.-running app) true)
  (try
    (dotimes [_ (count (.-event-queue app))]
      (let [event (peek (.-event-queue app))]
        (handle-event app event)
        (set! (.-event-queue app) (pop (.-event-queue app)))))
    (catch :default exn
      (set! (.-event-queue app) #queue [])
      (set! (.-running app) false)
      (throw exn)))
  (set! (.-running app) false))

(defn dispatch [^Application app event]
  (set! (.-event_queue app) (conj (.-event-queue app) event))
  (when-not (.-running app)
    (next-tick #(handle-events app))))
