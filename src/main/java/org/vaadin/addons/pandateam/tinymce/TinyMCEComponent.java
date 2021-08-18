package org.vaadin.addons.pandateam.tinymce;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ShadowRoot;
import com.vaadin.flow.function.SerializableConsumer;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;

import java.util.UUID;

@Tag("div")
@JsModule("./TinyMCEConnector.js")
@NpmPackage(value = "tinymce", version = "5.0.0")
public class TinyMCEComponent extends Div {

    private String id;
    private boolean initialContentSent;
    private String currentValue = "";
    private String rawConfig;
    JsonObject config = Json.createObject();
    private Element ta = new Element("div");

    /**
     * Creates a new TinyMce editor with shadowroot set or disabled. The shadow
     * root should be used if the editor is in used in Dialog component,
     * otherwise menu's and certain other features don't work. On the other
     * hand, the shadow root must not be on when for example used in inline
     * mode.
     *
     * @param shadowRoot true of shadow root hack should be used
     */
    public TinyMCEComponent(boolean shadowRoot) {
        setHeight("500px");
        ta.getStyle().set("height", "100%");
        if(shadowRoot) {
            ShadowRoot shadow = getElement().attachShadow();
            shadow.appendChild(ta);
        } else {
            getElement().appendChild(ta);
        }
    }

    public TinyMCEComponent() {
        this(false);
    }

    public void setEditorContent(String html) {
        this.currentValue = html;
        if (initialContentSent) {
            runBeforeClientResponse(ui -> getElement()
                    .callJsFunction("$connector.setEditorContent", html));
        } else {
            ta.setProperty("innerHTML", html);
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        id = UUID.randomUUID().toString();
        ta.setAttribute("id", id);
        ta.setProperty("innerHTML", currentValue);
        super.onAttach(attachEvent);
        injectTinyMceScript();
        initConnector();
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        initialContentSent = false;
        // save the current value to the dom element in case the component gets reattached
    }

    @SuppressWarnings("deprecation")
    private void initConnector() {
        this.initialContentSent = true;

        runBeforeClientResponse(ui -> {
            ui.getPage().executeJs("window.Vaadin.Flow.tinyMCEConnector.initLazy($0, $1, $2, $3)", rawConfig,
                    getElement(), ta, config);
        });
    }

    void runBeforeClientResponse(SerializableConsumer<UI> command) {
        getElement().getNode().runWhenAttached(ui -> ui
                .beforeClientResponse(this, context -> command.accept(ui)));
    }

    @ClientCallable
    private void updateValue(String htmlString) {
        this.currentValue = htmlString;
//        valueMapper.setModelValue(currentValue, true);
    }

    public String getCurrentValue() {
        return currentValue;
    }

    public void setConfig(String jsonConfig) {
        this.rawConfig = jsonConfig;
    }

    public TinyMCEComponent configure(String configurationKey, String value) {
        config.put(configurationKey, value);
        return this;
    }

    public TinyMCEComponent configure(String configurationKey, String... value) {
        JsonArray array = Json.createArray();
        for (int i = 0; i < value.length; i++) {
            array.set(i, value[i]);
        }
        config.put(configurationKey, array);
        return this;
    }


    public TinyMCEComponent configure(String configurationKey, boolean value) {
        config.put(configurationKey, value);
        return this;
    }

    public TinyMCEComponent configure(String configurationKey, double value) {
        config.put(configurationKey, value);
        return this;
    }

    /**
     * Replaces text in the editors selection (can be just a caret position).
     *
     * @param htmlString the html snippet to be inserted
     */
    public void replaceSelectionContent(String htmlString) {
        runBeforeClientResponse(ui -> getElement()
                .callJsFunction("$connector.replaceSelectionContent", htmlString));
    }

    /**
     * Injects actual editor script to the host page from the add-on bundle.
     * <p>
     * Override this with an empty implementation if you to use the cloud hosted
     * version, or own custom script if needed.
     */
    protected void injectTinyMceScript() {
//        getUI().get().getPage().addJavaScript("tinymce/tinymce.js");
    }

    public void focus() {
        runBeforeClientResponse(ui -> getElement()
                .callJsFunction("$connector.focus"));
    }

    @Override
    public void setEnabled(boolean enabled) {
//        Field.super.setEnabled(enabled);
        runBeforeClientResponse(ui -> getElement()
                .callJsFunction("$connector.setEnabled", enabled));
    }

//    @Override
    public void setReadOnly(boolean readOnly) {
//        Field.super.setReadOnly(readOnly);
        setEnabled(!readOnly);
    }


}
