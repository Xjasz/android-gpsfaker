package com.delvedapps.fakegpscontroller;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.delvedapps.core.Singleton;
import com.delvedapps.core.reflection.injectors.Inject;
import com.delvedapps.core.reflection.listeners.Listen;
import com.delvedapps.core.reflection.messages.BackRequestMessage;
import com.delvedapps.core.reflection.models.BaseFragment;
import com.delvedapps.core.storage.sharedpref.SavedPrefs;


public class FragmentUpdate extends BaseFragment {
    private final String TAG = getClass().getSimpleName();

    @Inject(id = R.id.rlHeader)
    private void onHeaderPressed() {
    }

    @Inject(id = R.id.rlMainView)
    private void onMainViewPressed() {
    }

    @Inject(id = R.id.tvUpdate)
    private TextView tvUpdate;

    @Inject(id = R.id.tvUpdate)
    private void onUpdatePressed() {
        String url = (String) SavedPrefs.getPref(Singleton.getAppContext(), SavedPrefs.url, "", SavedPrefs.TYPE_STRING);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    @Inject(id = R.id.tvHeader)
    private TextView tvHeader;

    @Inject(id = R.id.tvSubHeader)
    private TextView tvSubHeader;

    @Inject(id = R.id.tvInformation)
    private TextView tvInformation;

    @Listen(consumed = true)
    public void onMessageReceived(BackRequestMessage<Integer> request) {
        switch (request.object) {
            case 0:
                request.respond(0);
                break;
        }
    }

    @Override
    public int getLayout() {
        return R.layout.fragment_update;
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
        String title = (String) SavedPrefs.getPref(Singleton.getAppContext(), SavedPrefs.title, "", SavedPrefs.TYPE_STRING);
        String message1 = (String) SavedPrefs.getPref(Singleton.getAppContext(), SavedPrefs.message1, "", SavedPrefs.TYPE_STRING);
        String message2 = (String) SavedPrefs.getPref(Singleton.getAppContext(), SavedPrefs.message2, "", SavedPrefs.TYPE_STRING);

        tvHeader.setText(title);
        tvSubHeader.setText(message1);
        tvInformation.setText(message2);
    }
}
