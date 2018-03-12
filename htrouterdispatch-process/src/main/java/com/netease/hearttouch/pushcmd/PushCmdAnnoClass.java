package com.netease.hearttouch.pushcmd;

import javax.lang.model.element.TypeElement;

/**
 * Created by zyl06 on 20/06/2017.
 */
public class PushCmdAnnoClass {
    /**被标注的类的类型信息*/
    public TypeElement typeElement;
    /**被标注的类的对应的命令*/
    public String cmd;

    public PushCmdAnnoClass(TypeElement typeElement, String cmd) {
        this.typeElement = typeElement;
        this.cmd = cmd;
    }
}
