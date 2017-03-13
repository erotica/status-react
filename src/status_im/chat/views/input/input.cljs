(ns status-im.chat.views.input.input
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch]]
            [taoensso.timbre :as log]
            [status-im.accessibility-ids :as id]
            [status-im.components.react :refer [view
                                                text
                                                text-input
                                                icon
                                                touchable-highlight
                                                dismiss-keyboard!]]
            [status-im.chat.views.input.emoji :as emoji]
            [status-im.chat.views.input.suggestions :as suggestions]
            [status-im.chat.styles.input.input :as style]))

(defview commands-view []
  []
  [view style/commands-root
   [touchable-highlight {:on-press #(dispatch [:toggle-chat-ui-props :show-suggestions?])}
    [view
     [icon :input_list style/commands-list-icon]]]
   [view style/commands
    [text {:style style/command
           :font  :roboto-mono}
     "/browse"]
    [text {:style style/command
           :font  :roboto-mono}
     "/send"]]])

(defn input-view []
  (let [component         (r/current-component)
        set-layout-height #(r/set-state component {:height %})
        default-value     (subscribe [:get-chat-input-text])]
    (r/create-class
      {:reagent-render
       (fn []
         (let [{:keys [height]} (r/state component)]
           [view (style/input-root height)
            [text-input {:accessibility-label    id/chat-message-input
                         :blur-on-submit         true
                         :default-value          @default-value
                         :multiline              true
                         :on-blur                #(do (dispatch [:set-chat-ui-props :input-focused? false])
                                                      (set-layout-height 0))
                         :on-change-text         #(dispatch [:set-chat-input-text %])
                         :on-content-size-change #(let [h (-> (.-nativeEvent %)
                                                              (.-contentSize)
                                                              (.-height))]
                                                    (set-layout-height h))
                         :on-submit-editing      #(dispatch [:send-chat-message])
                         :on-focus               #(do (dispatch [:set-chat-ui-props :input-focused? true])
                                                      (dispatch [:set-chat-ui-props :show-emoji? false]))
                         :style                  style/input-view}]

            [touchable-highlight {:on-press #(do (dispatch [:toggle-chat-ui-props :show-emoji?])
                                                 (dismiss-keyboard!))}
             [view
              [icon :smile style/input-emoji-icon]]]]))})))

(defview container []
  [margin [:chat-input-margin]
   show-emoji? [:chat-ui-props :show-emoji?]
   show-suggestions? [:chat-ui-props :show-suggestions?]]
  [view
   (when show-suggestions?
     [suggestions/suggestions-view])
   [view {:style     (style/root margin)
          :on-layout #(let [h (-> (.-nativeEvent %)
                                  (.-layout)
                                  (.-height))]
                        (dispatch [:set-chat-ui-props :input-height h]))}
    [view style/container
     [commands-view]
     [input-view]]
    (when show-emoji?
      [emoji/emoji-view])]])