package com.example.reconocimiento.sync;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.util.Log;

import com.example.reconocimiento.data.repository.impl.OutboxRepositoryImpl;
import com.example.reconocimiento.workers.OutboxSenderWorker;

import org.jspecify.annotations.NonNull;

public class NetworkWatcher {

    private static final String TAG = "NetworkWatcher";

    private final ConnectivityManager cm;
    private final OutboxRepositoryImpl repo;
    private final ConnectivityManager.NetworkCallback callback;

    private boolean registered = false;

    public NetworkWatcher(Context ctx, OutboxRepositoryImpl repo) {
        this.cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.repo = repo;
        this.callback = new ConnectivityManager.NetworkCallback() {
            @Override public void onAvailable(@NonNull Network network) {
                repo.kickSender();
            }
        };
    }

    /** Start listening for connectivity changes. */
    public void register() { cm.registerDefaultNetworkCallback(callback); }
    public void unregister() { cm.unregisterNetworkCallback(callback); }
}