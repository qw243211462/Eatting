package dawizards.eatting.ui.activity;

import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.clans.fab.FloatingActionButton;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;
import dawizards.eatting.R;
import dawizards.eatting.bean.Comment;
import dawizards.eatting.bean.Food;
import dawizards.eatting.bean.User;
import dawizards.eatting.mvp.presenter.CommentPresenter;
import dawizards.eatting.mvp.presenter.FoodPresenter;
import dawizards.eatting.ui.adapter.CommentAdapter;
import dawizards.eatting.ui.adapter.event.LayoutState;
import dawizards.eatting.ui.base.ScrollActivity;
import dawizards.eatting.ui.customview.DividerItemDecoration;
import dawizards.eatting.util.IntentUtil;

/**
 * Created by WQH on 2016/8/3  19:12.
 */
public class ItemFoodActivity extends ScrollActivity {

    private static final String TAG = "ItemFoodActivity";

    @Bind(R.id.mTopPhoto)
    ImageView mTopPhoto;
    @Bind(R.id.food_schedule)
    FloatingActionButton mActionSchedule;
    @Bind(R.id.collapsingToolbarLayout)
    CollapsingToolbarLayout mCollapsingToolbarLayout;
    @Bind(R.id.rootView)
    View mRootView;

    Food mFoodItem;
    CommentAdapter mAdapter;
    CommentPresenter mCommentPresenter;
    FoodPresenter mFoodPresenter;
    BmobQuery<Comment> mBmobQuery;
    User currentUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCommentPresenter = new CommentPresenter(this);
        mFoodPresenter = new FoodPresenter(this);
        mFoodItem = (Food) getIntent().getSerializableExtra("itemFood");
        mAdapter = new CommentAdapter(this);
        currentUser = BmobUser.getCurrentUser(User.class);
        initView();
        initComments();
    }

    private void initView() {
        /*
         * Init title in Toolbar
         */
        mCollapsingToolbarLayout.setTitle(mFoodItem.name);
        /*
         * Init RecyclerView and Adapter.
         */
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mAdapter.setLoadState(LayoutState.GONE);
        /*
         * Init Action Menu.
         */
        if (currentUser == null || currentUser.getType() == User.UserType.CANTEEN) {
            mActionSchedule.setVisibility(View.GONE);
        }
        ImageLoader.getInstance().displayImage(mFoodItem.imageUrl, mTopPhoto);
    }

    private void initComments() {
        mBmobQuery = new BmobQuery<>();
        mBmobQuery.addWhereEndsWith("commentTo", mFoodItem.getObjectId());
        mCommentPresenter.queryBatch(mBmobQuery, new CommentLoadListener());
    }

    @Override
    protected int layoutId() {
        return R.layout.activity_item_food;
    }

    @Override
    public void onRefreshDelayed() {
        mCommentPresenter.queryBatch(mBmobQuery, new CommentLoadListener());
    }

    @Override
    public void onLoadMore(int toToLoadPage) {
    }

    private void showContent(List<Comment> data) {
        mAdapter.addAll(data);
        mRecyclerView.setAdapter(mAdapter);

    }


    @OnClick(R.id.post_comment)
    public void postComment() {
        if (currentUser == null) {
            new MaterialDialog.Builder(this)
                    .content("只有登陆之后才可以进行下面操作咯，是否前去登陆？")
                    .positiveText("前去登陆")
                    .negativeText("暂时不去")
                    .onPositive((dialog, which) -> IntentUtil.goToOtherActivity(ItemFoodActivity.this, LoginActivity.class))
                    .show();
            return;
        }

        IntentUtil.goToOtherActivity(this, PostCommentActivity.class, "itemFood", mFoodItem);
    }

    @OnClick(R.id.share)
    public void share() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain"); //MIME type
        intent.putExtra(Intent.EXTRA_SUBJECT, getTitle());
        intent.putExtra(Intent.EXTRA_TEXT, "我在" + getTitle() + "APP上吃到了一个好吃的菜" + mCollapsingToolbarLayout.getTitle() + "大家看来看看");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(Intent.createChooser(intent, getTitle()));
        }
    }

    @OnClick(R.id.food_schedule)
    public void schedule() {
        if (currentUser == null) {
            new MaterialDialog.Builder(this)
                    .content("只有登陆之后才可以进行下面操作咯，是否前去登陆？")
                    .positiveText("前去登陆")
                    .negativeText("暂时不去")
                    .onPositive((dialog, which) -> IntentUtil.goToOtherActivity(ItemFoodActivity.this, LoginActivity.class))
                    .show();
            return;
        }

        if (mFoodItem.isAttend(currentUser)) {
            Snackbar.make(mRootView, "您已经喜欢过这个菜品了", Snackbar.LENGTH_LONG).show();
        }
        mFoodItem.addAttend(currentUser);
        mFoodPresenter.update(mFoodItem, new FoodUpdateListener());
    }

    @OnClick(R.id.food_like)
    public void like() {
        if (currentUser == null) {
            new MaterialDialog.Builder(this)
                    .content("只有登陆之后才可以进行下面操作咯，是否前去登陆？")
                    .positiveText("前去登陆")
                    .negativeText("暂时不去")
                    .onPositive((dialog, which) -> IntentUtil.goToOtherActivity(ItemFoodActivity.this, LoginActivity.class))
                    .show();
            return;
        }

        if (mFoodItem.isLike(currentUser)) {
            Snackbar.make(mRootView, "您已经赞过这个菜品了", Snackbar.LENGTH_LONG).show();
        }
        mFoodItem.addLike(currentUser);
        mFoodPresenter.update(mFoodItem, new FoodUpdateListener());
    }

    private class CommentLoadListener extends FindListener<Comment> {
        @Override
        public void done(List<Comment> list, BmobException e) {
            if (e == null) {
                showContent(list);
            } else {
                Log.e(TAG, e.getMessage());
            }
        }
    }


    private class FoodUpdateListener extends UpdateListener {

        @Override
        public void done(BmobException e) {
            if (e == null) {
                Snackbar.make(mRootView, "操作成功", Snackbar.LENGTH_LONG).show();
            } else {
                Log.e(TAG, e.getMessage());
            }
        }
    }
}