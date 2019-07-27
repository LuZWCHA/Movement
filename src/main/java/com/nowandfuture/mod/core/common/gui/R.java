package com.nowandfuture.mod.core.common.gui;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextComponentTranslation;

public interface R {
    class id{
        //-------------------------------------constructor gui-------------------------------------------------------------
        public static final String text_constructor_btn_lock_lock_id = "movement.constructor.lock";
        public static final String text_constructor_btn_lock_unlock_id = "movement.constructor.unlock";
        public static final String text_constructor_btn_create_create_id = "movement.constructor.create";
        public static final String text_constructor_btn_resizex_add_id = "movement.constructor.x_add";
        public static final String text_constructor_btn_resizex_sub_id = "movement.constructor.x_sub";
        public static final String text_constructor_btn_resizey_add_id = "movement.constructor.y_add";
        public static final String text_constructor_btn_resizey_sub_id = "movement.constructor.y_sub";
        public static final String text_constructor_btn_resizez_add_id = "movement.constructor.z_add";
        public static final String text_constructor_btn_resizez_sub_id = "movement.constructor.z_sub";

        //------------------------------------movement module gui------------------------------------------------------------
        public static final String text_module_cmb_mode_rcy_id = "movement.module.combo.mode.rcy";
        public static final String text_module_cmb_mode_rcy_reserve_id = "movement.module.combo.mode.reserve";
        public static final String text_module_cmb_mode_one_id = "movement.module.mode.one";
        public static final String text_module_cmb_mode_one_rcy_id = "movement.module.mode.onercy";
        public static final String text_module_cmb_mode_stop_id = "movement.module.mode.stop";

        public static final String text_module_lab_value_x_id = "movement.module.value.x";
        public static final String text_module_lab_value_y_id = "movement.module.value.y";
        public static final String text_module_lab_value_z_id = "movement.module.value.z";
        public static final String text_module_lab_value_scale_id = "movement.module.value.scale";
        public static final String text_module_lab_value_angle_id = "movement.module.value.angle";

        public static final String text_module_lab_total_time_id = "movement.module.lab.totaltime";
        public static final String text_module_btn_start_id = "movement.module.btn.start";
        public static final String text_module_btn_stop_id = "movement.module.btn.stop";
        public static final String text_module_btn_apply_id = "movement.module.btn.apply";
        public static final String text_module_btn_export_id = "movement.module.btn.export";
    }

    static TextComponentTranslation name1(String id){
        return new TextComponentTranslation(id);
    }

    static String name(String id){
        return I18n.format(id);
    }
}
