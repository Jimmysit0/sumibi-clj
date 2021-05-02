;; The actual namespace
(ns commands.poll
  (:require
   [discljord.messaging :as discord-rest]
   [clojure.string :as str]
   [clojure.edn :as edn]))

;; Get the application ID
(def config (edn/read-string (slurp "config.edn")))

;; Create the slash command
(defn slash-commands [conn]
  (discord-rest/create-global-application-command! conn (:application-id config) "poll" "Create a simple poll!" :options [{:type 3 :name "message" :description "Write the actuall poll!" :required true}]))

;; The command it self
(defn handle-interaction-event [rest-conn event-data]
  (let [{:keys [id token data]} event-data
        {:keys [options]} data
        {:keys [value]} (first options)
        response-text (str ":bar_chart:" " **"value"**")
        success? (deref (discord-rest/create-interaction-response! rest-conn id token 4 :data {:content response-text}))
        message-id (if success? (:id (deref (discord-rest/get-original-interaction-response! rest-conn (:application-id config) token))))]
    (if message-id
      (doseq [emoji-id ["👍" "👎" "🤷"]]
        (discord-rest/create-reaction! rest-conn (:channel-id event-data) message-id emoji-id)))))

                                                                                                             
