package com.gzsll.hupu.presenter;

import android.content.Context;

import com.gzsll.hupu.api.forum.ForumApi;
import com.gzsll.hupu.bean.CollectResult;
import com.gzsll.hupu.bean.ThreadSchemaInfo;
import com.gzsll.hupu.helper.ToastHelper;
import com.gzsll.hupu.ui.view.ContentView;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by sll on 2016/3/9.
 */
public class ContentPresenter extends Presenter<ContentView> {

    @Inject
    ForumApi mForumApi;
    @Inject
    Context mContext;
    @Inject
    ToastHelper mToastHelper;


    @Inject
    @Singleton
    public ContentPresenter() {
    }


    private String tid;
    private String fid;
    private String pid;
    public int totalPage;
    public int currentPage = 1;


    public void onThreadInfoReceive(String tid, String fid, String pid, int page) {
        this.tid = tid;
        this.fid = fid;
        this.pid = pid;
        view.showLoading();
        loadContent(page);
    }

    private void loadContent(int page) {
        mForumApi.getThreadInfo(tid, fid, page, pid).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<ThreadSchemaInfo>() {
            @Override
            public void call(ThreadSchemaInfo threadSchemaInfo) {
                if (threadSchemaInfo != null) {
                    if (threadSchemaInfo.error != null) {
                        view.onError(threadSchemaInfo.error.text);
                    } else {
                        totalPage = threadSchemaInfo.pageSize;
                        view.renderContent(threadSchemaInfo.url);
                        view.isCollected(threadSchemaInfo.isCollected == 1);
                        view.renderShare(threadSchemaInfo.share.weibo, threadSchemaInfo.share.url);
                    }
                } else {
                    view.onError("加载失败");
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                view.onError("加载失败");
            }
        });
    }

    public void onReload() {
        loadContent(currentPage);
    }

    public void onRefresh() {
        loadContent(currentPage);
    }


    public void onPageNext() {
        currentPage++;
        if (currentPage >= totalPage) {
            currentPage = totalPage;
        }
        loadContent(currentPage);
    }

    public void onPagePre() {
        currentPage--;
        if (currentPage <= 1) {
            currentPage = 1;
        }
        loadContent(currentPage);
    }

    public void onPageSelected(int page) {
        loadContent(page);
    }


    public void addCollect() {
        mForumApi.addCollect(tid).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<CollectResult>() {
            @Override
            public void call(CollectResult collectResult) {
                if (collectResult != null && collectResult.result != null) {
                    mToastHelper.showToast(collectResult.result.msg);
                    view.isCollected(collectResult.result.status == 200);
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                mToastHelper.showToast("收藏失败");
            }
        });
    }


    public void delCollect() {
        mForumApi.delCollect(tid).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<CollectResult>() {
            @Override
            public void call(CollectResult collectResult) {
                if (collectResult != null && collectResult.result != null) {
                    mToastHelper.showToast(collectResult.result.msg);
                    view.isCollected(collectResult.result.status != 200);
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                mToastHelper.showToast("取消收藏失败");
            }
        });
    }


    public void copy(String stripped) {
        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(stripped);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData
                    .newPlainText("content", stripped);
            clipboard.setPrimaryClip(clip);
        }
        mToastHelper.showToast("复制成功");
    }


    @Override
    public void detachView() {

    }
}
