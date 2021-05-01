;; Important stuff
(ns core
  (:require [clojure.edn :as edn]
            [clojure.core.async :refer [chan close!]]
            [discljord.messaging :as discord-rest]
            [discljord.connections :as discord-ws]
            [discljord.events :refer [message-pump!]]
            [commands.reverse :as reverse-command]
            [commands.palindrome :as palindrome-command]
            [commands.poll :as poll-command]))

(def state (atom nil))
(def bot-id (atom nil))
(def config (edn/read-string (slurp "config.edn")))

;; Events
(defmulti handle-event (fn [type _data] type))
(defmethod handle-event :default [_ _])

;; Commands
(defmethod handle-event :interaction-create
  [event-type event-data]
  (cond (= (:name (:data event-data)) "reverse")
        (reverse-command/handle-interaction-event (:rest (deref state)) event-data)
        (= (:name (:data event-data)) "palindrome")
        (palindrome-command/handle-interaction-event (:rest (deref state)) event-data)
        (= (:name (:data event-data)) "poll")
        (poll-command/handle-interaction-event (:rest (deref state)) event-data)))

;; Activity
(defmethod handle-event :ready
  [_ _]
  (discord-ws/status-update! (:gateway @state) :activity (discord-ws/create-activity :name (:playing config))))

;; Start the bot
(defn start-bot! [token & intents]
  (let [event-channel (chan 100)
        gateway-connection (discord-ws/connect-bot! token event-channel :intents (set intents))
        rest-connection (discord-rest/start-connection! token)]
    {:events  event-channel
     :gateway gateway-connection
     :rest    rest-connection}))

;; Stop the bot
(defn stop-bot! [{:keys [rest gateway events] :as _state}]
  (discord-rest/stop-connection! rest)
  (discord-ws/disconnect-bot! gateway)
  (close! events))

;; Main function
(defn setup []
  (reverse-command/slash-commands (:rest (deref state)))
  (palindrome-command/slash-commands (:rest (deref state)))
  (poll-command/slash-commands (:rest (deref state))))
 
(defn -main [& args]
  (reset! state (start-bot! (:token config) :guild-messages))
  (reset! bot-id (:id @(discord-rest/get-current-user! (:rest @state))))
  (try
    (setup)
    (message-pump! (:events @state) handle-event)
    (finally (stop-bot! @state))))
