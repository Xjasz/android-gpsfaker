package com.delvedapps.fakegpscontroller;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.delvedapps.core.Singleton;
import com.delvedapps.core.reflection.injectors.Inject;
import com.delvedapps.core.reflection.listeners.Listen;
import com.delvedapps.core.reflection.listeners.Listener;
import com.delvedapps.core.reflection.messages.BackRequestMessage;
import com.delvedapps.core.reflection.messages.BackResponseMessage;
import com.delvedapps.core.reflection.messages.FragmentChangeMessage;
import com.delvedapps.core.reflection.models.BaseFragment;
import com.delvedapps.core.util.contstant.Constants;


public class FragmentNavigation extends BaseFragment {
    private final String TAG = getClass().getSimpleName();

    @Inject(id = R.id.ivRate)
    private ImageView ivRate;

    @Inject(id = R.id.ivContactUs)
    private ImageView ivContactUs;

    @Inject(id = R.id.ivLogout)
    private ImageView ivLogout;

    @Inject(id = R.id.ivDonate)
    private ImageView ivDonate;

    @Inject(id = R.id.ivInfo)
    private ImageView ivInfo;

    @Inject(id = R.id.ivShare)
    private ImageView ivShare;

    @Inject(id = R.id.ivHome)
    private ImageView ivHome;

    @Inject(id = R.id.rlShare)
    private void onSharePressed() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        String shareText = "";
        shareIntent.putExtra(Constants.SMS_BODY, shareText);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        shareIntent.setType(Constants.MIME_TYPE_PLAIN_TEXT);
        startActivity(Intent.createChooser(shareIntent, Constants.SHARE_TITLE));
    }

    @Inject(id = R.id.rlLogout)
    private void onLogoutPressed() {
        Listener.send(new BackResponseMessage<>(0));
    }

    @Inject(id = R.id.rlDonate)
    private void onDonatePressed() {
        Singleton.toastMessage("Google made us turn this off.");
    }

    @Inject(id = R.id.rlInfo)
    private void onInfoPressed() {
        Listener.send(new FragmentChangeMessage(new FragmentDummy(),
                R.id.container_Menufragment,
                R.anim.fade_in,
                R.anim.slideout_to_left));
        Listener.send(new FragmentChangeMessage(new FragmentMap(),
                R.id.container_MainFragment,
                R.anim.fade_in,
                R.anim.fade_out));
        Listener.send(new FragmentChangeMessage(new FragmentDummy(),
                R.id.container_SubFragment,
                R.anim.fade_in,
                R.anim.fade_out));
    }

    @Inject(id = R.id.rlHome)
    private void onHomePressed() {
        Listener.send(new FragmentChangeMessage(new FragmentDummy(),
                R.id.container_Menufragment,
                R.anim.fade_in,
                R.anim.slideout_to_left));
        Listener.send(new FragmentChangeMessage(new FragmentMap(),
                R.id.container_MainFragment,
                R.anim.fade_in,
                R.anim.fade_out));
        Listener.send(new FragmentChangeMessage(new FragmentDummy(),
                R.id.container_SubFragment,
                R.anim.fade_in,
                R.anim.fade_out));
    }

    @Inject(id = R.id.rlRate)
    private void onRatePressed() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(""));
        startActivity(intent);
    }


    @Inject(id = R.id.rlContactUs)
    private void onContactUsPressed() {
        String SUBJECT = getString(R.string.app_name) + " - Support Message";
        String[] SUPPORT_EMAILS = {""};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.putExtra(Intent.EXTRA_EMAIL, SUPPORT_EMAILS);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, SUBJECT);
        emailIntent.putExtra(Intent.EXTRA_TEXT, "\n\n\n\n\n\n" + Singleton.DEVICE_ID + "\n" + Singleton.APP_VERSION);
        emailIntent.setType(Constants.MIME_TYPE_PLAIN_TEXT);
        startActivity(emailIntent);

    }

    @Override
    public int getLayout() {
        return R.layout.fragment_navigationmenu;
    }

    @Listen(consumed = true)
    public void onMessageReceived(BackRequestMessage<Integer> request) {
        switch (request.object) {
            case 0:
                Listener.send(new FragmentChangeMessage(new FragmentDummy(),
                        R.id.container_Menufragment,
                        R.anim.fade_in,
                        R.anim.slideout_to_left));
                break;
        }
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
        ivRate.setColorFilter(Color.WHITE);
        ivContactUs.setColorFilter(Color.WHITE);
        ivLogout.setColorFilter(Color.WHITE);
        ivDonate.setColorFilter(Color.WHITE);
        ivInfo.setColorFilter(Color.WHITE);
        ivShare.setColorFilter(Color.WHITE);
        ivHome.setColorFilter(Color.WHITE);
    }

}