package com.github.haocen2004.login_simulation.util;

import static com.github.haocen2004.login_simulation.util.Tools.openUrl;

import android.content.Context;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.LifecycleOwner;

import com.github.haocen2004.login_simulation.data.dialog.ButtonData;
import com.github.haocen2004.login_simulation.data.dialog.DialogData;
import com.github.haocen2004.login_simulation.data.dialog.DialogLiveData;

import java.util.List;

public class DialogHelper {
    private static DialogHelper INSTANCE;
    private final Context context;
    private static DialogLiveData dialogLiveData;
    private int currPos = 0;
    private boolean currClose = false;
    private boolean currShow = false;
    private DialogInterface dialogInterface;
    private final String TAG = "dialogHelper";
//    private static String blackListString;

    public DialogHelper(Context context) {
        this.context = context;

        Logger.i("dialogHelper", "loading...");

        dialogLiveData = DialogLiveData.getINSTANCE(context);

        eulaCheck();

        dialogLiveData.observe((LifecycleOwner) context, dialogData -> {

            for (DialogData dialogDatum : dialogData) {
                Logger.d(TAG, dialogData.indexOf(dialogDatum) + "|" + dialogDatum.getTitle());
            }
            if (currShow) {
                return;
            }
            if (currPos >= dialogData.size()) {
                currPos = dialogData.size() - 1;
            }
            showDialog(dialogData.get(currPos));

        });

    }

    public static DialogHelper getDialogHelper(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new DialogHelper(context);
        }
        return INSTANCE;
    }

    public void ref() {
        if (currClose) return;
        try {
            showDialog(dialogLiveData.getValue().get(currPos));
        } catch (Exception ignore) {
        }
    }

    public void next() {
        List<DialogData> dialogData = dialogLiveData.getValue();

        currPos++;
        if (dialogData.size() > currPos) {
            currShow = true;

            showDialog(dialogData.get(currPos));
        } else {
            currShow = false;
        }
    }

    private void eulaCheck() {
        if (!Tools.getBoolean(context, "show_eula", true)) {
            Logger.i(TAG, "eula checked, skip.");
            return;
        }

        DialogData dialogData = new DialogData("用户隐私协议",
                "概要\n" +
                        "不得用于商业用途。\n" +
                        "不得以此应用牟利。\n" +
                        "自行承担一切使用此应用造成的意外和风险。\n" +
                        "最终解释权归本软件作者所有。\n" +
                        "未尽之处，以下方链接「最终用户许可协议与隐私条款」为准。");
        dialogData.setNegativeButtonData(new ButtonData("打开隐私协议完整链接") {
            @Override
            public void callback(DialogHelper dialog) {
                openUrl("https://github.com/Haocen2004/bh3_login_simulation/blob/main/EULA.md", context);
            }
        });
        dialogData.setPositiveButtonData(new ButtonData("同意") {
            @Override
            public void callback(DialogHelper dialog) {
                super.callback(dialog);
                Tools.saveBoolean(context, "show_eula", false);
            }
        });
        dialogData.setNeutralButtonData(new ButtonData("不同意") {
            @Override
            public void callback(DialogHelper dialog) {
                System.exit(0);
            }
        });

        dialogLiveData.insertEulaDialog(dialogData);

        Logger.i(TAG, "show eula");
    }

    public void setCurrPos(int newPos) {
        currPos = newPos;
    }

    public int getCurrPos() {
        return currPos;
    }

    public void setCurrShow(boolean currShow) {
        this.currShow = currShow;
    }

    public DialogInterface getCurrDialog() {
        return dialogInterface;
    }

    private void showDialog(DialogData dialogData) {

        final AlertDialog.Builder normalDialog = new AlertDialog.Builder(context);
        normalDialog.setTitle(dialogData.getTitle());
        normalDialog.setMessage(dialogData.getMessage());
        ButtonData buttonData = dialogData.getNegativeButtonData();
        ButtonData buttonData2 = dialogData.getNeutralButtonData();
        ButtonData buttonData3 = dialogData.getPositiveButtonData();
        if (buttonData != null) {
            normalDialog.setNegativeButton(buttonData.getText(),
                    (dialog, which) -> {
                        dialogInterface = dialog;
                        buttonData.callback(this);
                    });
        }
        if (buttonData2 != null) {
            normalDialog.setNeutralButton(buttonData2.getText(),
                    (dialog, which) -> {
                        dialogInterface = dialog;
                        buttonData2.callback(this);
                    });
        }
        if (buttonData3 != null) {
            normalDialog.setPositiveButton(buttonData3.getText(),
                    (dialog, which) -> {
                        dialogInterface = dialog;
                        buttonData3.callback(this);
                    });
        }
        normalDialog.setCancelable(dialogData.isCancelable());
        normalDialog.show();
        currShow = true;
        currClose = false;
    }

    public void setCurrClose(boolean currClose) {
        this.currClose = currClose;
    }
}