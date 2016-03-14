package com.gzsll.hupu.presenter;

import com.gzsll.hupu.api.forum.ForumApi;
import com.gzsll.hupu.bean.Thread;
import com.gzsll.hupu.bean.ThreadListData;
import com.gzsll.hupu.bean.ThreadListResult;
import com.gzsll.hupu.helper.ToastHelper;
import com.gzsll.hupu.ui.view.ThreadRecommendView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by sll on 2016/3/9.
 */
public class ThreadRecommendPresenter extends Presenter<ThreadRecommendView> {


    @Inject
    ForumApi mForumApi;
    @Inject
    ToastHelper mToastHelper;


    private List<Thread> threads = new ArrayList<>();

    @Inject
    @Singleton
    public ThreadRecommendPresenter() {
    }


    private String lastTid = "";
    private String lastTamp = "";

    private Subscription mSubscription;

    public void onRecommendThreadsReceive() {
        view.showLoading();
        loadRecommendList(false);
    }


    private void loadRecommendList(final boolean clear) {
        mSubscription = mForumApi.getRecommendThreadList(lastTid, lastTamp).map(new Func1<ThreadListResult, List<Thread>>() {
            @Override
            public List<Thread> call(ThreadListResult result) {
                if (clear) {
                    threads.clear();
                }
                if (result != null && result.result != null) {
                    ThreadListData data = result.result;
                    lastTamp = data.stamp;
                    return addThreads(data.data);
                }
                return null;
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<List<Thread>>() {
            @Override
            public void call(List<Thread> threads) {
                if (threads != null) {
                    view.onRefreshing(false);
                    view.hideLoading();
                    view.renderThreads(threads);
                } else {
                    loadThreadError();
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                loadThreadError();
            }
        });
    }


    private void loadThreadError() {
        if (threads.isEmpty()) {
            view.onError("数据加载失败");
        } else {
            view.hideLoading();
            view.onRefreshing(false);
            mToastHelper.showToast("数据加载失败");
        }
    }

    private List<Thread> addThreads(List<Thread> threadList) {
        for (Thread thread : threadList) {
            if (!contains(thread)) {
                threads.add(thread);
            }
        }
        lastTid = threads.get(threads.size() - 1).tid;
        return threads;
    }


    private boolean contains(Thread thread) {
        boolean isContain = false;
        for (Thread thread1 : threads) {
            if (thread.tid.equals(thread1.tid)) {
                isContain = true;
                break;
            }
        }
        return isContain;
    }


    public void onRefresh() {
        lastTamp = "";
        lastTid = "";
        loadRecommendList(true);
    }


    public void onReload() {
        loadRecommendList(false);

    }

    public void onLoadMore() {
        loadRecommendList(false);
    }


    @Override
    public void detachView() {
        lastTamp = "";
        lastTid = "";
        if (mSubscription != null) {
            mSubscription.unsubscribe();
        }
        threads.clear();
    }

}
