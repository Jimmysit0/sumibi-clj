;; The actual namespace
(ns commands.palindrome
  (:require
   [discljord.messaging :as discord-rest]
   [clojure.string :as str]
   [clojure.edn :as edn]))

;; Get the application ID
(def config (edn/read-string (slurp "config.edn")))

;; Create the slash command
(defn slash-commands [conn]
  (discord-rest/create-global-application-command! conn (:application-id config) "palindrome" "Check if the message is a palindrome!" :options [{:type 3 :name "message" :description "The message you want to check if it's a palindrome (ignores capital letters and spaces)" :required true}]))

;; The command it self
(defn handle-interaction-event [rest-conn event-data]
  (let [{:keys [id token data]} event-data
        {:keys [options]} data
        {:keys [value]} (first options)
        lowercased (str/lower-case value)
        reversed (str/reverse lowercased)
        lnospaces (str/split lowercased #"\s+")
        rnospaces (str/split reversed #"\s+")
        response-text (if (= rnospaces lnospaces)
                        (str "Your original input was: `" value "`, so your reversed input is: " "`"(str/reverse value)"`." " That message is a palindrome!")
                        (str "Your original input was: `" value "`, so your reversed input is: " "`"(str/reverse value)"`." " That message is not a palindrome... :("))]
    (discord-rest/create-interaction-response! rest-conn id token 4 :data {:content response-text})))

