package com.nowandfuture.mod.core.common.gui.mygui;

public interface ChangeListener{
    void changed();
    void changed(int index);
    void changed(String id);

    abstract class ChangeEvent implements ChangeListener{
        public void changed(){}
        public void changed(int index){}
        public void changed(String id){}
    }
}
