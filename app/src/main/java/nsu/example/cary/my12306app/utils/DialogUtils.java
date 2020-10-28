package nsu.example.cary.my12306app.utils;

import android.content.DialogInterface;

import java.lang.reflect.Field;


public class DialogUtils {

    //设置对话框是否可关闭
    public static void setClosable(DialogInterface dialog,boolean b){
        try {
            Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
            field.setAccessible(true);
            field.set(dialog,b);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
