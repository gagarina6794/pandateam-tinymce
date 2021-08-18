package org.vaadin.addons.pandateam.tinymce;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("")
public class View extends Div {

    public View() {
        TinyMCEComponent tinyMCEComponent = new TinyMCEComponent();
        add(tinyMCEComponent);
    }
}
