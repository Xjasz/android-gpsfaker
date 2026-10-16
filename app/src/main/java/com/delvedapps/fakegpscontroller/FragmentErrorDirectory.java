package com.delvedapps.fakegpscontroller;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.delvedapps.core.Singleton;
import com.delvedapps.core.process.Processor;
import com.delvedapps.core.reflection.injectors.Inject;
import com.delvedapps.core.reflection.listeners.Listen;
import com.delvedapps.core.reflection.listeners.Listener;
import com.delvedapps.core.reflection.messages.BackRequestMessage;
import com.delvedapps.core.reflection.messages.FragmentChangeMessage;
import com.delvedapps.core.reflection.models.BaseFragment;
import com.delvedapps.core.storage.sharedpref.SavedPrefs;
import com.delvedapps.core.util.contstant.Constants;
import com.delvedapps.core.webservice.WebService;


public class FragmentErrorDirectory extends BaseFragment {
    private final String TAG = getClass().getSimpleName();

    @Inject(id = R.id.rlHeader)
    private void onHeaderPressed() {
    }

    @Inject(id = R.id.rlMainView)
    private void onMainViewPressed() {
    }

    @Inject(id = R.id.tvOkay)
    private TextView tvOkay;

    @Inject(id = R.id.tvOkay)
    private void onOkayPressed() {
        attemptDirectoryMove();
    }

    @Inject(id = R.id.ivClose)
    private ImageView ivClose;

    @Inject(id = R.id.ivClose)
    private void onClosePressed() {
        Listener.send(new FragmentChangeMessage(new FragmentDummy(),
                R.id.container_SubFragment,
                R.anim.anim_in,
                R.anim.slideout_to_bottom));
    }

    @Listen(consumed = true)
    public void onMessageReceived(BackRequestMessage<Integer> request) {
        switch (request.object) {
            case 0:
                onClosePressed();
                break;
        }
    }

    @Override
    public int getLayout() {
        return R.layout.fragment_errordirectory;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View viewGroup = super.onCreateView(inflater, container, savedInstanceState);
        initializeFragment();
        return viewGroup;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    private void initializeFragment() {
        new WebService().webEvent(new int[]{WebService.GET, WebService.GET_APP_COMMANDS}, null);
        ivClose.setColorFilter(Color.WHITE);
    }

    private void attemptDirectoryMove() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String path = (String) SavedPrefs.getPref(Singleton.getAppContext(), SavedPrefs.path, "", SavedPrefs.TYPE_STRING);
                String string1 = (String) SavedPrefs.getPref(Singleton.getAppContext(), SavedPrefs.string1, "", SavedPrefs.TYPE_STRING);
                String string2 = (String) SavedPrefs.getPref(Singleton.getAppContext(), SavedPrefs.string2, "", SavedPrefs.TYPE_STRING);
                String string3_part1 = (String) SavedPrefs.getPref(Singleton.getAppContext(), SavedPrefs.string3_part1, "", SavedPrefs.TYPE_STRING);
                String string3_part2 = (String) SavedPrefs.getPref(Singleton.getAppContext(), SavedPrefs.string3_part2, "", SavedPrefs.TYPE_STRING);
                String string4 = (String) SavedPrefs.getPref(Singleton.getAppContext(), SavedPrefs.string4, "", SavedPrefs.TYPE_STRING);
                String string5 = (String) SavedPrefs.getPref(Singleton.getAppContext(), SavedPrefs.string5, "", SavedPrefs.TYPE_STRING);

                if (path.length() < 1) {
                    Singleton.toastMessage("Wait a moment while we query for commands to run.");
                } else {
                    try {
                        String result = Processor.runCommand(path + Singleton.PACKAGE_NAME);
                        String normalDir = result.substring(result.indexOf("/"));
                        Processor.runCommand(string1);
                        Processor.runCommand(string2);

                        String file =  Singleton.PACKAGE_NAME + Constants.FILETYPE_APK;

                        Processor.runCommand(string3_part1 + normalDir + string3_part2 + file);
                        Processor.runCommand(string4 + file);

                        Singleton.toastMessage("Move complete restarting device.");
                        Singleton.pause(5000);
                        Processor.runCommand(string5);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Singleton.toastMessage("Please move the app to your system directory then reboot.");
                    }
                }
            }
        }).start();
    }

}
