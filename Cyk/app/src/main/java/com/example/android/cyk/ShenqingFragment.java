package com.example.android.cyk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.cyk.Adapter.Jiekuan_list_adapter;
import com.example.android.cyk.Model.BorrowModel;
import com.example.android.cyk.Model.TokenModel;
import com.google.gson.Gson;
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by qiao on 2017/7/3.
 */

public class ShenqingFragment extends Fragment implements AdapterView.OnItemClickListener {

    Context context;
    private TokenModel tokenModel;
    SharedPreferences sp;
    private BorrowModel borrowModel;
    private List<BorrowModel.borrowItem> items = new ArrayList<>();

    private OkHttpClient client = new OkHttpClient();

    private ListView listView;
    private Jiekuan_list_adapter adapter;
    private TwinklingRefreshLayout refreshLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.shenqing_layout, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sp = context.getSharedPreferences("tokenSP", 0);

        listView = getView().findViewById(R.id.shenqing_list_view);
        adapter = new Jiekuan_list_adapter(items, context);
        listView.setOnItemClickListener(this);

        refreshLayout = getView().findViewById(R.id.refresh);
        refreshLayout.setOnRefreshListener(new RefreshListenerAdapter() {
            @Override
            public void onRefresh(TwinklingRefreshLayout refreshLayout) {
                super.onRefresh(refreshLayout);
                refreshBorrowList();
            }
        });

        requestToken();

        requestBorrowList();
    }

    private void requestToken() {
        RequestBody body = new FormBody.Builder()
                .addEncoded("grant_type", "password")
                .addEncoded("client_id", "testclient")
                .addEncoded("client_secret", "testpass")
                .addEncoded("username", "1")
                .addEncoded("password", "1")
                .build();
        Request request = new Request.Builder()
                .url(getString(R.string.host_url)+"/oauth/token")
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Gson gson = new Gson();
                tokenModel = gson.fromJson(response.body().string(), TokenModel.class);
                Log.e("获取token", tokenModel.toString());
                requestRefreshToken();
            }
        });
    }

    private void requestRefreshToken() {
        RequestBody body = new FormBody.Builder()
                .addEncoded("grant_type", "refresh_token")
                .addEncoded("refresh_token", tokenModel.getRefresh_token())
                .addEncoded("username", "1")
                .addEncoded("password", "1")
                .addEncoded("client_id", "testclient")
                .addEncoded("client_secret", "testpass")
                .build();
        Request request = new Request.Builder()
                .url(getString(R.string.host_url)+"/oauth/refresh/token")
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                Gson gson = new Gson();
                TokenModel model = gson.fromJson(response.body().string(), TokenModel.class);
                Log.e("获取到刷新token", model.getAccess_token());
                sp.edit().putString("token", model.getAccess_token()).commit();
            }
        });
    }


    private void requestBorrowList() {
        long time = new Date().getTime();
        RequestBody body = new FormBody.Builder()
                .addEncoded("userId", "1")
                .addEncoded("access_token", sp.getString("token", ""))
                .addEncoded("timestamp", String.valueOf(time))
                .build();
        Request request = new Request.Builder()
                .url(getString(R.string.host_url)+"/project/list")
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Gson gson = new Gson();
                borrowModel = gson.fromJson(response.body().string(), BorrowModel.class);
                Log.e("借款列表", borrowModel.getData().toString());
                items = borrowModel.getData();
                adapter.setDataSource(items);
            }
        });
    }

    private void refreshBorrowList(){
        long time = new Date().getTime();
        RequestBody body = new FormBody.Builder()
                .addEncoded("userId", "1")
                .addEncoded("access_token", sp.getString("token", ""))
                .addEncoded("timestamp", String.valueOf(time))
                .build();
        Request request = new Request.Builder()
                .url(getString(R.string.host_url)+"/project/list")
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                refreshLayout.finishRefreshing();
//                Gson gson = new Gson();
//                borrowModel = gson.fromJson(response.body().string(), BorrowModel.class);
//                Log.e("借款列表", borrowModel.getData().toString());
//                adapter = new Jiekuan_list_adapter(borrowModel.getData(), context);
//                listView.setAdapter(adapter);
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (i != 0){
            return;
        }
        BorrowModel.borrowItem item = borrowModel.getData().get(i);
        Intent intent = new Intent(context, QuerenjiekuanActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("shenqingID", item.getId());
        intent.putExtras(bundle);
        startActivity(intent);
    }
}

