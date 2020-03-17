package com.nowandfuture.mod.core.common.gui.mygui.api;

public interface IChangeListener {
    void changed();
    void changed(int index);
    void changed(String id);

    abstract class IChangeEvent implements IChangeListener {
        public void changed(){}
        public void changed(int index){}
        public void changed(String id){}
    }
}
