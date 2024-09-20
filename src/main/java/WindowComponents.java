package src.main.java;

import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;

public class WindowComponents implements ComponentListener {
    @Override
    public void componentMoved(ComponentEvent event) {

    }

    @Override
    public void componentHidden(ComponentEvent event) {

    }

    @Override
    public void componentResized(ComponentEvent event) {
        Main.width = event.getComponent().getBounds().getSize().width;
        Main.height = event.getComponent().getBounds().getSize().height;
    }

    @Override
    public void componentShown(ComponentEvent event) {

    }
}
