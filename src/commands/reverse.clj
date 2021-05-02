;; The actual namespace
(ns commands.reverse
  (:require
   [discljord.messaging :as discord-rest]
   [clojure.string :as str]
   [clojure.edn :as edn]))

;; Get the application ID
(def config (edn/read-string (slurp "config.edn")))

;; Create the slash command
(defn slash-commands [conn]
  (discord-rest/create-global-application-command! conn (:application-id config) "reverse" "Reverses the provided text!" :options [{:type 3 :name "message" :description "The message you want to reverse" :required true}]))

;; The command it self
(defn handle-interaction-event [rest-conn event-data]
  (let [{:keys [id token data]} event-data
        {:keys [options]} data
        {:keys [value]} (first options)
        response-text (str "Your original input was: `" value "`, so your reversed input is: " "`"(str/reverse value)"`")]
    (discord-rest/create-interaction-response! rest-conn id token 4 :data {:content response-text})))
